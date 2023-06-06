import IRUtilities.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.tinydb.TDB;
import org.tinydb.TDBFS;
import org.tinydb.TDBHashMap;

public class indexer {
    private static Porter porter;
    private static HashSet<String> stopWords;
    private static final String INVERTED_FILE_PATH = "inverted_file.tdb";
    private static final String STOP_WORDS_FILE = "stopwords.txt";

    public static boolean isStopWord(String str) {
        return stopWords.contains(str);    
    }

    public void StopStem(String str) {
        super();
        porter = new Porter();
        stopWords = new HashSet<String>();
                
        BufferedReader buffer = null;
        try {
            FileReader file = new FileReader("stopwords.txt");
            buffer = new BufferedReader(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            while ((line = buffer.readLine()) != null) {
                stopWords.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String stem(String str) {
        return porter.stripAffixes(str);
    }

    public static void main(String[] arg) {
        String url = "jdbc:mysql://localhost:3307/searchwise";
        String username = "时珂妍";
        String password = "mjl010313sdx";

        StopStem stopStem = new StopStem("stopwords.txt");

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();

            String query = "SELECT description FROM sites";
            ResultSet resultSet = statement.executeQuery(query);

            // Create inverted file
            TDB tdb = TDBFS.createOrOpen(INVERTED_FILE_PATH);
            TDBHashMap<String, List<String>> invertedFile = tdb.createHashMap("inverted_file").orElseThrow();

            while (resultSet.next()) {
                String description = resultSet.getString("description");
                List<String> words = processDescription(description);

                for (String word : words) {
                    // Apply Porter's algorithm for stemming
                    String stemmedWord = stem(word);
                    // Add the stemmed word to the inverted file
                    invertedFile.compute(stemmedWord, (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }
                        v.add(description);
                        return v;
                    });
                }
            }

            // Close the inverted file
            tdb.close();
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> processDescription(String description) {
        List<String> words = new ArrayList<>();

        // Split the description into individual words
        String[] tokens = description.split("\\s+");

        for (String token : tokens) {
            // Remove punctuation and convert to lowercase
            String word = token.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            // Check if the word is a stop word
            if (!isStopWord(word)) {
                words.add(word);
            }
        }

        return words;
    }
}
