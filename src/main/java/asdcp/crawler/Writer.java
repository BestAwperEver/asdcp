package asdcp.crawler;

import static java.lang.Class.forName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.javatuples.KeyValue;

public class Writer implements Runnable {
	private static Connection mysql_conn;
	private static boolean table_created = false;
	
	public Writer() {}

	private void initDBConnetion() {
		try {
			forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		String username = "testuser";
		String password = "test";

		// Create Connection
		Properties info = new Properties();
		info.put("user", username);
		info.put("password", password);
		info.put("autoReconnect", "true");
		info.put("useUnicode", "true");
		info.put("characterEncoding", "utf8");

		try {
			Writer.mysql_conn = DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info);
			if (Writer.table_created == false) {
				Writer.table_created = true;
				Statement stmt = Writer.mysql_conn.createStatement();
				try {
					Double a = Crawler.sync.poll(30, TimeUnit.SECONDS);
					if (a == null) {
						throw new InterruptedException("WTF???");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					stmt.close();
					mysql_conn.close();
					e.printStackTrace();
				}
				stmt.execute("DROP TABLE IF EXISTS " + Crawler.getTableName());
				stmt.execute("CREATE TABLE IF NOT EXISTS " + Crawler.getTableName()
				+ " (url VARCHAR(256), content MEDIUMTEXT CHARACTER SET utf16, PRIMARY KEY (url))");
				stmt.execute("set character set utf8");
				stmt.execute("set names utf8");
				stmt.close();
			}
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	@Override
	public void run() {
		initDBConnetion();
		try {
			if (mysql_conn == null || mysql_conn.isClosed()) return;
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		try (PreparedStatement stmt = mysql_conn.prepareStatement(
				"INSERT INTO " + Crawler.getTableName() + " (url, content) values (?, ?)"
						+ "ON DUPLICATE KEY UPDATE url=url");
				) {
			
			mysql_conn.setAutoCommit(false);
			
			int i = 0;
			while (true) {
				KeyValue<String, String> kv = null;

				kv = Crawler.links_content.poll(30, TimeUnit.SECONDS);
				
				if (kv != null) {
					stmt.setString(1, kv.getKey());
					stmt.setString(2, kv.getValue());
					stmt.addBatch();
					++i;
				} else break;
				
				if (i % 1000 == 0) {
					stmt.executeBatch();
					mysql_conn.commit();
				}
			}
			
			if (i % 1000 != 0) {
				stmt.executeBatch();
				mysql_conn.commit();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			try {
				mysql_conn.commit();
				mysql_conn.setAutoCommit(true);
				mysql_conn.close();
			} catch (SQLException ex) {
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
	}

//	public void writeToDatabase() {
//		initDBConnetion();
//		try {
//			addUrlsToDatabase();
//			Writer.mysql_conn.close();
//		} catch (SQLException ex) {
//			System.out.println("SQLException: " + ex.getMessage());
//			System.out.println("SQLState: " + ex.getSQLState());
//			System.out.println("VendorError: " + ex.getErrorCode());
//		}
//	}

//	@Deprecated
//	private void addUrlToDatabase(String url) throws SQLException {
//		Statement stmt = mysql_conn.createStatement();
//		stmt.executeUpdate("insert into " + crwlr.getTableName() + " value ('" + url + "')");
//		stmt.close();
//	}
//
//	@Deprecated
//	private void addUrlsToDatabase() throws SQLException {
//		Statement stmt = mysql_conn.createStatement();
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("insert into " + crwlr.getTableName() + " values ('");
//
//		int count = 1;
//		final int N = 10000;
//
//		for (String url : links) {
//			sb.append(url);
//			if (count % N == 0 || count == links.size()) {
//				sb.append("')");
//				String query = sb.toString();
//
//
//
//				sb.setLength(0);
//				sb.append("insert into " + table_name + " values ('");
//			} else
//				sb.append("'),('");
//			++count;
//		}
//
//		// stmt.executeUpdate("insert into " + table_name + " values ('";
//		// + url +
//		//
//		// "')");
//		stmt.close();
//	}

}
