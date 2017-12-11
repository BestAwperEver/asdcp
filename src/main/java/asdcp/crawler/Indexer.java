package asdcp.crawler;

import java.sql.*;
import java.util.*;

import static java.lang.Class.forName;

public class Indexer {

    public void createIndexFromDb(){
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

        String docIdTableName = "doc_id";
        String wordTableName = "word";
        String wordDocTableName = "word_doc";
        String selectQuery = "select url, content from spbu_ru_100_24 WHERE url LIKE \"spbu.ru%\" limit 100";
        int docId = 1;

        Map<String, Set<Integer>> invertedFile = new HashMap<>();
        Map<String, Integer> wordIdMap = new HashMap<>();
        String splitReg = ",?:?;?!?\\.?\\??[\\u00A0\\s]+,?:?;?!?\\.?\\??";
        //String splitReg = "[^\\p{L}'-]+";
        String matchReg = "(\\p{L}+|(\\p{L}+-?){1,3})";
        int batchSize = 10;

        try {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info)){
                try (Statement statement = connection.createStatement()){
                    statement.execute("DROP TABLE IF EXISTS " + docIdTableName);
                    statement.execute("CREATE TABLE IF NOT EXISTS " + docIdTableName
                            + " (url VARCHAR(256), doc_id SMALLINT, PRIMARY KEY (url))");

                    statement.execute("DROP TABLE IF EXISTS " + wordTableName);
                    statement.execute("CREATE TABLE IF NOT EXISTS " + wordTableName
                            + " (word_id INTEGER, word TEXT CHARACTER SET utf16, PRIMARY KEY (word_id))");

                    statement.execute("DROP TABLE IF EXISTS " + wordDocTableName);
                    statement.execute("CREATE TABLE IF NOT EXISTS " + wordDocTableName
                            + " (id INTEGER AUTO_INCREMENT, word_id INTEGER, doc_id INTEGER, PRIMARY KEY (id))");

                    statement.execute("set character set utf8");
                    statement.execute("set names utf8");

                    ResultSet resultSet = statement.executeQuery(selectQuery);
                    PreparedStatement docIdInsertStatement = connection.prepareStatement("INSERT INTO " + docIdTableName + " (url, doc_id) VALUES (?, ?)");
                    PreparedStatement wordInsertStatement = connection.prepareStatement("INSERT INTO " + wordTableName + " (word_id, word) VALUES (?, ?)");
                    PreparedStatement wordDocInsertStatement = connection.prepareStatement("INSERT INTO " + wordDocTableName + " (word_id, doc_id) VALUES (?, ?)");
                    connection.setAutoCommit(false);
                    int counter = 0;
                    int wordId = 1;

                    while (resultSet.next()){
                        String url = resultSet.getString(1);
                        String text = resultSet.getString(2);
                        docIdInsertStatement.setString(1, url);
                        docIdInsertStatement.setInt(2, docId);
                        docIdInsertStatement.addBatch();
                        ++counter;
                        if (counter % batchSize == 0){
                            docIdInsertStatement.executeBatch();
                            connection.commit();
                        }
                        String[] splittedText = text.split(splitReg);
                        for (String word:splittedText) {
                            word = word.toLowerCase();
                            if (word.matches(matchReg) && word.length() > 2){
                                if (!invertedFile.containsKey(word)){
                                    invertedFile.put(word, new HashSet<>());
                                    wordIdMap.put(word, wordId);
                                    ++wordId;
                                }
                                Set<Integer> wordDocIdSet = invertedFile.get(word);
                                wordDocIdSet.add(docId);
                            }
                        }
                        ++docId;
                    }
                    if (counter % batchSize != 0 ){
                        docIdInsertStatement.executeBatch();
                        connection.commit();
                    }
                    docIdInsertStatement.close();
                    resultSet.close();
                    counter = 0;

                    for (String word:invertedFile.keySet()) {
                        wordId = wordIdMap.get(word);
                        wordInsertStatement.setInt(1, wordId);
                        wordInsertStatement.setString(2, word);
                        wordInsertStatement.addBatch();
                        if (counter % batchSize == 0){
                            wordInsertStatement.executeBatch();
                            connection.commit();
                        }

                        Set<Integer> docIdSet = invertedFile.get(word);
                        for (Integer id:docIdSet) {
                            wordDocInsertStatement.setInt(1, wordId);
                            wordDocInsertStatement.setInt(2, id);
                            wordDocInsertStatement.addBatch();
                            if (counter % batchSize == 0){
                                wordDocInsertStatement.executeBatch();
                                connection.commit();
                            }
                        }
                        ++counter;
                    }
                    if (counter % batchSize != 0 ){
                        wordDocInsertStatement.executeBatch();
                        wordInsertStatement.executeBatch();
                        connection.commit();
                    }
                    wordDocInsertStatement.close();
                    wordInsertStatement.close();
                } finally {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
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
