package asdcp.crawler;

import static java.lang.Class.forName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.tartarus.snowball.ext.englishStemmer;

public class Searcher {
	
	protected Connection mysql_conn;
	
	protected englishStemmer stemmer = new englishStemmer();

	public void initDBConnetion() {
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
			mysql_conn = DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public List<String> getDocumentsByWord_Test(String word) {
		List<String> urls = null;
				
		stemmer.setCurrent(word);
		stemmer.stem();
		word = stemmer.getCurrent();
		
//		try (PreparedStatement selectStatement = mysql_conn
//				.prepareStatement("select url from word_test, word_doc_test, doc_id_test"
//						+ "where word_test.word = '?' "
//						+ "and word_test.word_id = word_doc_test.word_id "
//						+ "and word_doc_test.doc_id = doc_id_test.doc_id");){
			
			
			//selectStatement.setString(1, word);
			
		try (Statement stmt = mysql_conn.createStatement()) {
			
			ResultSet resultSet = stmt.executeQuery("select url from word_test, word_doc_test, doc_id_test "
					+ "where word_test.word = '" + word + "' "
					+ "and word_test.word_id = word_doc_test.word_id "
					+ "and word_doc_test.doc_id = doc_id_test.doc_id");
			
			//ResultSet resultSet = selectStatement.executeQuery();
			
			if (!resultSet.isBeforeFirst()) {
				return null;
			}
			
			urls = new LinkedList<String>();
			
			while (resultSet.next()) {
				urls.add(resultSet.getString(1));
			}
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

		return urls;		
	}

}
