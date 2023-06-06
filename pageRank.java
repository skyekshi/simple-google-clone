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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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
        // Retrieve the inverted file data structure
        Map<String, List<String>> invertedFile = getInvertedFile(); // Replace with your actual implementation
    
        // Check if the term exists in the inverted file
        if (invertedFile.containsKey(term)) {
            List<String> documents = invertedFile.get(term);
            return documents.size(); // Return the document frequency
        }
    
        return 0; // The term doesn't exist in the inverted file, so return 0
    }    
    

    private boolean checkIfTermInTitle(String term, String documentTitle) {
        // Remove unwanted characters and perform case-insensitive matching
        String cleanedTerm = term.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        String cleanedTitle = documentTitle.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
    
        // Check if the cleaned term is found in the cleaned title
        return cleanedTitle.contains(cleanedTerm);
    }
    

    private Map<String, Double> getDocumentsContainingTerm(String term) {
        // Retrieve the inverted file data structure
        Map<String, List<String>> invertedFile = getInvertedFile(); // Replace with your actual implementation
    
        // Create a map to store document IDs and their similarity scores
        Map<String, Double> documentScores = new HashMap<>();
    
        // Check if the term exists in the inverted file
        if (invertedFile.containsKey(term)) {
            List<String> documents = invertedFile.get(term);
            
            // Iterate over the documents and calculate the similarity scores
            for (String documentId : documents) {
                double similarityScore = calculateDocumentSimilarity(term, documentId); // Replace with your calculation method
                documentScores.put(documentId, similarityScore);
            }
        }
    
        return documentScores;
    }
    

    private Map<String, Double> sortDocumentScores(Map<String, Double> documentScores) {
        // Sort the document scores in descending order based on the scores
        Comparator<Map.Entry<String, Double>> scoreComparator = Map.Entry.comparingByValue(Comparator.reverseOrder());
        Map<String, Double> sortedDocumentScores = documentScores.entrySet()
                .stream()
                .sorted(scoreComparator)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        return sortedDocumentScores;
    }

    private List<String> rankDocuments(Map<String, Double> documentScores) {
        // Sort the documents based on their scores
        List<Map.Entry<String, Double>> sortedDocuments = new ArrayList<>(documentScores.entrySet());
        sortedDocuments.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        
        // Apply the mechanism to favor matches in the title by boosting the rank of pages with a match in the title
        List<String> rankedDocuments = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedDocuments) {
            String documentId = entry.getKey();
            boolean isInTitle = checkIfTermInTitle(documentId); // Assuming the documentId represents the document title
            
            // Boost the rank of pages with a match in the title
            double boostedScore = entry.getValue();
            if (isInTitle) {
                boostedScore *= 1.5; // Increase the score by a factor (e.g., 1.5)
            }
            
            // Add the document to the ranked list
            rankedDocuments.add(documentId);
        }
        
        return rankedDocuments;
    }
    

}

