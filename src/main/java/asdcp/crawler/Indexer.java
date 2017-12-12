package asdcp.crawler;

import java.sql.*;
import java.util.*;

import org.tartarus.snowball.ext.russianStemmer;

import static java.lang.Class.forName;

public class Indexer {

	public void createIndexFromDb() {
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

		String dataTableName = "spbu_ru_100_24";
		String docIdTableName = "doc_id";
		String wordTableName = "word";
		String wordDocTableName = "word_doc";
		// String selectQuery = "select url, content from " + dataTableName + " WHERE
		// url LIKE \"spbu.ru%\" limit 100";

		int docId = 1;

		Map<Integer, Set<Integer>> invertedFile = new HashMap<>();
		Map<String, Integer> wordIdMap = new HashMap<>();
		//String splitReg = ",?:?;?!?\\.?\\??[\\u00A0\\s]+,?:?;?!?\\.?\\??";
		// String splitReg = "[^\\p{L}'-]+";
		String matchReg = "(\\p{L}+|(\\p{L}+-?){1,3})";
		int batchSize = 10000;

		try {
			try (Connection connection =
					DriverManager.getConnection(
							"jdbc:mysql://radagast.asuscomm.com:3306/testdb",
							info)) {
				
				connection.setAutoCommit(false);

				PreparedStatement docIdInsertStatement = connection
						.prepareStatement("INSERT INTO " + docIdTableName + " (doc_id, url) VALUE (?, ?)");
				PreparedStatement wordInsertStatement = connection
						.prepareStatement("INSERT INTO " + wordTableName + " (word_id, word) VALUE (?, ?)");
				PreparedStatement wordDocInsertStatement = connection
						.prepareStatement("INSERT INTO " + wordDocTableName + " (word_id, doc_id) VALUE (?, ?)");

				PreparedStatement selectStatement = connection
						.prepareStatement("SELECT url, content from " + dataTableName + " limit ?,?");

				try (Statement statement = connection.createStatement()) {
					statement.execute("DROP TABLE IF EXISTS " + wordDocTableName);
					statement.execute("DROP TABLE IF EXISTS " + wordTableName);
					statement.execute("DROP TABLE IF EXISTS " + docIdTableName);

					statement
							.execute("CREATE TABLE IF NOT EXISTS " + docIdTableName + " (" + "doc_id INTEGER NOT NULL, "
									+ "url VARCHAR(256) NOT NULL, "
									+ "PRIMARY KEY (doc_id), "
									+ "FOREIGN KEY (url) "
									+ "REFERENCES " + dataTableName + " (url) "
									+ "ON DELETE CASCADE" + ")");

					statement.execute("CREATE TABLE IF NOT EXISTS " + wordTableName + " ("
							+ "word_id INTEGER NOT NULL, "
							+ "word VARCHAR(60) CHARACTER SET utf16 NOT NULL, "
							+ "PRIMARY KEY (word_id)" + ")");

					statement.execute("CREATE TABLE IF NOT EXISTS " + wordDocTableName + " ("
							+ "word_id INTEGER NOT NULL, " + "doc_id INTEGER NOT NULL, "
							+ "PRIMARY KEY (word_id, doc_id), "
							+ "FOREIGN KEY (word_id) "
							+ "REFERENCES "	+ wordTableName + " (word_id) "
							+ "ON DELETE CASCADE, "
							+ "FOREIGN KEY (doc_id) "
							+ "REFERENCES " + docIdTableName + " (doc_id) "
							+ "ON DELETE CASCADE" + ")");

					statement.execute("set character set utf8");
					statement.execute("set names utf8");

					int counter_doc = 0;
					int wordId = 1;

					ResultSet resultSetCount = statement.executeQuery("select count(*) from " + dataTableName);
					resultSetCount.next();
					
					int index = 0;
					int count = resultSetCount.getInt(1);
					int step = 1000;

					resultSetCount.close();

					// TEST
//					step = 500;
//					count = 970;

					selectStatement.setInt(2, step);
					
					while (index < count) {

						selectStatement.setInt(1, index);
						index += step;
						ResultSet resultSet = selectStatement.executeQuery();

						while (resultSet.next()) {
							String url = resultSet.getString(1);
							String text = resultSet.getString(2);
							docIdInsertStatement.setInt(1, docId);
							docIdInsertStatement.setString(2, url);
							docIdInsertStatement.addBatch();
							++counter_doc;

							if (counter_doc % batchSize == 0) {
								docIdInsertStatement.executeBatch();
								connection.commit();
								System.out.println("commited " + counter_doc + " documents");
							}

							// TO DO
							// String[] splittedText = text.split(splitReg);
							StringTokenizer st = new StringTokenizer(text);
							russianStemmer stemmer = new russianStemmer();
							String word = null;

							while (st.hasMoreTokens()) {
								// for (String word : splittedText) {
								// TO DO
								// word = word.toLowerCase();
								stemmer.setCurrent(st.nextToken());
								stemmer.stem();
								word = stemmer.getCurrent().toLowerCase();

								// TO DO
								if (word.matches(matchReg) && word.length() > 2) {

									if (wordIdMap.containsKey(word) == false) {

										++wordId;

										invertedFile.put(wordId, new HashSet<>());
										wordIdMap.put(word, wordId);

										wordInsertStatement.setInt(1, wordId);
										wordInsertStatement.setString(2, word.substring(0,
												word.length() < 60 ? word.length() : 60));
										wordInsertStatement.addBatch();

										if (wordId % batchSize == 0) {
											wordInsertStatement.executeBatch();
											connection.commit();
											System.out.println("commited " + wordId + " words");
										}

										invertedFile.get(wordId).add(docId);

									} else {
										invertedFile.get(wordIdMap.get(word)).add(docId);
									}

								}

							}
							++docId;
						}
						if (counter_doc % batchSize != 0) {
							docIdInsertStatement.executeBatch();
							connection.commit();
							System.out.println("commited " + counter_doc + " documents");
						}
						if (wordId % batchSize != 0) {
							wordInsertStatement.executeBatch();
							connection.commit();
							System.out.println("commited " + wordId + " words");
						}
					}

					int counter_pair = 0;

					// invertedFile.get(12).toArray()

					for (int word_id : invertedFile.keySet()) {

						Set<Integer> docIdSet = invertedFile.get(word_id);
						for (int id : docIdSet) {
							++counter_pair;

							wordDocInsertStatement.setInt(1, word_id);
							wordDocInsertStatement.setInt(2, id);
							wordDocInsertStatement.addBatch();

							if (counter_pair % batchSize == 0) {
								wordDocInsertStatement.executeBatch();
								connection.commit();
								System.out.println("commited " + counter_pair + " pairs");
							}
						}
					}

					if (counter_pair % batchSize != 0) {
						wordDocInsertStatement.executeBatch();
						connection.commit();
						System.out.println("commited " + counter_pair + " pairs");
					}

				} finally {

					docIdInsertStatement.close();
					wordInsertStatement.close();
					selectStatement.close();
					wordDocInsertStatement.close();

					connection.commit();
					connection.setAutoCommit(true);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

	}

	public static void main(String[] args) {
		Indexer indexer = new Indexer();
		long startTime = System.currentTimeMillis();
		indexer.createIndexFromDb();
		long timeSpent = System.currentTimeMillis() - startTime;
		System.out.println("Indexing time: " + timeSpent);
	}
}
