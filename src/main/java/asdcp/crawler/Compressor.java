package asdcp.crawler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static java.lang.Class.forName;

public class Compressor {
    private static String convertToBinaryString(int num) {
        String str = "";

        if (num == 0)
            return "0";

        while (num > 0) {
            str = num % 2 + str;
            num = num / 2;
        }
        return str;
    }

    private static int convertBinaryStringToNum(String str) {

        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '1') {
                num += (int) Math.pow(2, str.length() - i - 1);
            }
        }
        return num;
    }

    public static String deltaEncode(int num) {
        String bin = convertToBinaryString(num);
        String bitsInBinary = convertToBinaryString(bin.length());
        String removeRemainingBit = bin.substring(1);
        String combined = bitsInBinary + removeRemainingBit;
        for (int i = 0; i < bitsInBinary.length() - 1; i++) {
            combined = "0" + combined;
        }
        return combined;
    }

    public static int deltaDecode(String str) {

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '0')
                count++;
            else
                break;
        }

        String temp = "";
        for (int i = count; i <= 2 * count; i++) {
            char c = str.charAt(i);
            temp += c;
        }
        int numOfBitsToRead = convertBinaryStringToNum(temp);

        String output = "1";
        for (int i = 2 * count + 1; i < 2 * count + numOfBitsToRead; i++) {
            char c = str.charAt(i);
            output += c;
        }

        return convertBinaryStringToNum(output);
    }
    // старший бит i=0
    private static byte setBit(byte b, int i){
        byte outByte = 0;
        int shift = 8 - (i + 1);
        outByte = (byte)(outByte|1 << shift);
        outByte = (byte)(b|outByte);
        return outByte;
    }

    public static void decompress(){
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

        String compressionTableName = "compression";

        try (Connection connection =
                     DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info)){
            try (PreparedStatement selectStatement = connection.prepareStatement("SELECT code FROM " + compressionTableName
                    +  " WHERE word_id = 77")){
                try (ResultSet resultSet = selectStatement.executeQuery()){
                    ByteArrayInputStream byteArrayInputStream;
                    if (resultSet.next()){
                        byteArrayInputStream = (ByteArrayInputStream) resultSet.getBinaryStream(1);
                        byte[] bytes = new byte[byteArrayInputStream.available()];
                        int bytesCount = byteArrayInputStream.read(bytes);
                        for (int i = 0; i < bytesCount; i++) {
                            String s1 = String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0');
                            System.out.println(s1);
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void compress(){
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

        String compressionTableName = "compression";
        String wordDocTableName = "word_doc";
        String wordTableName = "word";
        int maxWordId = 38688;
        int batchSize = 1000;

        try {
            try (Connection connection =
                         DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info)) {
                connection.setAutoCommit(false);

                try (Statement statement = connection.createStatement()){
                    statement.execute("DROP TABLE IF EXISTS " + compressionTableName);

                    statement
                            .execute("CREATE TABLE IF NOT EXISTS " + compressionTableName + " (" + "word_id INTEGER NOT NULL, "
                                    + "code BLOB NOT NULL, "
                                    + "last_bits_size TINYINT NOT NULL DEFAULT 8, "
                                    + "FOREIGN KEY (word_id) "
                                    + "REFERENCES " + wordTableName + " (word_id) "
                                    + "ON DELETE CASCADE" + ")");

//                    try (ResultSet resultSet = statement.executeQuery("SELECT MAX(word_id) as m FROM " + wordDocTableName)){
//                        while (resultSet.next()){
//                            maxWordId = resultSet.getInt("m");
//                        }
//                    }

                }
                StringBuilder stringBuilder = new StringBuilder();
                PreparedStatement insertStatement =
                        connection.prepareStatement("INSERT INTO " + compressionTableName +
                                " (word_id, code, last_bits_size) VALUES (?, ?, ?)");

                try (PreparedStatement selectStatement = connection.prepareStatement("SELECT word_id, doc_id from " + wordDocTableName +
                        " WHERE word_id=? ORDER BY doc_id")){

                    for (int i = 2; i <= maxWordId; i++) {
                        //i = 77;
                        selectStatement.setInt(1, i);
                        try (ResultSet resultSet = selectStatement.executeQuery()){
                            int prevDocId = 0;
                            if (resultSet.next()){
                                prevDocId = resultSet.getInt(2);
                                stringBuilder.append(deltaEncode(prevDocId));
                            } else {
                                continue;
                            }

                            int currentDocId;
                            while (resultSet.next()){
                                currentDocId = resultSet.getInt(2);
                                stringBuilder.append(deltaEncode(currentDocId - prevDocId));
                                prevDocId = currentDocId;
                            }
                        }
                        String codeStr = stringBuilder.toString();
                        int bytesCount;
                        int lastBitCount = 8;
                        int codeLength = codeStr.length();
                        if (codeLength % 8 == 0){
                            bytesCount = codeLength / 8;
                        } else {
                            bytesCount = codeLength / 8 + 1;
                            lastBitCount = codeLength % 8;
                        }

                        byte superByte = 0;
                        int bitsCounter = 0;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesCount);

                        for (int j = 0; j < codeLength; j++) {
                            int bitIndex = j % 8;
                            if (codeStr.charAt(j) == '1'){
                                superByte = setBit(superByte, bitIndex);
                            }
                            //String s1 = String.format("%8s", Integer.toBinaryString(superByte & 0xFF)).replace(' ', '0');
                            bitsCounter += 1;
                            if (bitsCounter == 8){
                                byteBuffer.put(superByte);
                                superByte = 0;
                                bitsCounter = 0;
                            }
//                            if (j == codeLength - 1){
//                                byteBuffer.put(superByte);
//                            }
                        }
                        InputStream inputStream = new ByteArrayInputStream(byteBuffer.array());
                        insertStatement.setInt(1, i);
                        insertStatement.setBinaryStream(2, inputStream);
                        insertStatement.setInt(3, lastBitCount);
                        insertStatement.addBatch();
                        if (i % batchSize == 0){
                            insertStatement.executeBatch();
                            connection.commit();
                        }
                        stringBuilder.setLength(0);
                    }

                } finally {
                    insertStatement.executeBatch();
                    connection.commit();
                    insertStatement.close();
                    connection.setAutoCommit(true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        compress();
        long timeSpent = System.currentTimeMillis() - startTime;
        System.out.println("Compress time: " + timeSpent);
        decompress();

    }
}
