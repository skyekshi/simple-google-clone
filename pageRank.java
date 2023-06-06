import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class pageRank {
    private Map<String, List<String>> invertedFile;

    private StopStem stopStem;

    public SearchEngine() {
        stopStem = new StopStem("stopwords.txt");
    }

    private boolean isStopWord(String str) {
        return stopStem.isStopWord(str);
    }

    public void loadInvertedFile(String invertedFileLocation) {
        invertedFile = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(invertedFileLocation))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String term = parts[0];
                    String[] documents = parts[1].split(",");
                    invertedFile.put(term, List.of(documents));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> search(String query) {
        List<String> queryTerms = preprocessQuery(query);
        Map<String, Double> documentScores = calculateDocumentScores(queryTerms);
        List<String> rankedDocuments = rankDocuments(documentScores);
        return rankedDocuments.subList(0, Math.min(rankedDocuments.size(), 50));
    }

    private List<String> preprocessQuery(String query) {
        List<String> preprocessedTerms = new ArrayList<>();

        // Tokenize the query terms
        String[] tokens = query.split("\\s+");

        // Apply stemming and remove stop words
        for (String token : tokens) {
            String word = token.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (!isStopWord(word)) {
                String stemmedWord = stem(word);
                preprocessedTerms.add(stemmedWord);
            }
        }

        return preprocessedTerms;
    }

    private Map<String, Double> calculateDocumentScores(List<String> queryTerms) {
        Map<String, Double> documentScores = new HashMap<>();

        for (String term : queryTerms) {
            double termWeight = calculateTermWeight(term);

            // Check if the term is found in the document title and apply a boost factor
            boolean isTitleMatch = checkIfTermInTitle(term);
            if (isTitleMatch) {
                termWeight *= BOOST_FACTOR;
            }

            Map<String, Double> documentsContainingTerm = getDocumentsContainingTerm(term);
            for (Map.Entry<String, Double> entry : documentsContainingTerm.entrySet()) {
                String documentId = entry.getKey();
                double documentSimilarity = entry.getValue();

                double currentScore = documentScores.getOrDefault(documentId, 0.0);
                double newScore = currentScore + (termWeight * documentSimilarity);
                documentScores.put(documentId, newScore);
            }
        }

        // Sort the document scores in descending order based on the scores
        documentScores = sortDocumentScores(documentScores);

        return documentScores;
    }

    private double calculateTermWeight(String term) {
        double tf = calculateTermFrequency(term); // Calculate term frequency
        double idf = calculateInverseDocumentFrequency(term); // Calculate inverse document frequency
        double termWeight = tf * idf; // Calculate term weight using tf-idf formula
    
        return termWeight;
    }

    private double calculateTermFrequency(String term, String document) {
        int termCount = 0;
        int totalTerms = 0;
    
        // Tokenize the document into individual terms
        String[] terms = document.split("\\s+");
    
        for (String docTerm : terms) {
            if (docTerm.equalsIgnoreCase(term)) {
                termCount++;
            }
            totalTerms++;
        }
    
        // Calculate the term frequency by dividing the term count by the total number of terms
        double termFrequency = (double) termCount / totalTerms;
    
        return termFrequency;
    }
    
    
    private double calculateInverseDocumentFrequency(String term) {
        int totalDocuments = getTotalDocuments(); // Retrieve the total number of documents
        int documentFrequency = getDocumentFrequency(term); // Retrieve the document frequency of the term
    
        // Add 1 to the document frequency to avoid division by zero and handle unseen terms
        double idf = Math.log((double) totalDocuments / (documentFrequency + 1));
    
        return idf;
    }
    
    private int getTotalDocuments() {
        int totalDocuments = 0;
    
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/searchwise", "时珂妍", "mjl010313sdx");
             Statement statement = connection.createStatement()) {
    
            String query = "SELECT COUNT(*) AS total FROM documents";
            ResultSet resultSet = statement.executeQuery(query);
    
            if (resultSet.next()) {
                totalDocuments = resultSet.getInt("total");
            }
    
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return totalDocuments;
    }
    
    private int getDocumentFrequency(String term) {
        // Implement the logic to retrieve the document frequency (DF) of the given term
        // Return the document frequency of the term
    }
    

    private boolean checkIfTermInTitle(String term) {
        // Implement the logic to check if the term is found in the document title
        // Return true if the term is found in the title, false otherwise
    }

    private Map<String, Double> getDocumentsContainingTerm(String term) {
        // Retrieve the documents containing the given term from the inverted file
        // Return a map of document IDs to their corresponding document similarity scores
    }

    private Map<String, Double> sortDocumentScores(Map<String, Double> documentScores) {
        // Implement the logic to sort the document scores in descending order based on the scores
        // Return the sorted document scores
    }

    private List<String> rankDocuments(Map<String, Double> documentScores) {
        // Sort and rank the documents based on their scores
        // Apply the mechanism to favor matches in the title by boosting the rank of pages with a match in the title
        // Return the ranked documents
    }

    // Other helper methods
}

