package mygroup;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


/**
 * The {@code Client} class is responsible for fetching JSON data from URLs,
 * extracting relevant information, and processing it into a list of {@code Game} objects.
 */
public class Client {
    
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private Map<String, String> savedData;
    private List<Game> gameList;
    
    /**
     * Constructs a new {@code Client} instance, initializing the HTTP client,
     * JSON object mapper, and data storage structures.
     */
    public Client() {
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        savedData = new HashMap<String,String>();
        gameList = new ArrayList<Game>();
    }
    
    /**
     * Runs the client, performing the main operations of reading URLs,
     * formatting times, and filtering non-played games.
     */
    public void run() {
        readURLS();
        formatTimes();
        filterNonPlayed();
    }

    /**
     * Reads URLs from a resource file named "urls.txt" and processes each entry.
     * Extracts the URL, JSON path, and keywords, then fetches and processes data.
     */
    private void readURLS() {
        InputStream inputstream = Client.class.getClassLoader().getResourceAsStream("urls.txt");
        if (inputstream == null) {
            System.out.println("Error loading in file from resources");
        }
        
        try (Scanner scanner = new Scanner(inputstream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitURLAndKeywords = line.split(" ");
                String url = splitURLAndKeywords[0];
                String[] pathToRoot = splitURLAndKeywords[1].split("-");
                String[] keywords = splitURLAndKeywords[2].split(",");
                
                getData(url, pathToRoot,keywords);
                
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves JSON data associated with a given URL.
     * 
     * @param url  the URL from which the data was retrieved
     * @param json the JSON data to save
     */
    private void saveData(String url, String json) {
        savedData.put(url, json);
    }
    
    
    /**
     * Fetches JSON data from a URL, processes it, and extracts relevant game information.
     * If data has already been retrieved from the URL, it is loaded from memory instead.
     * 
     * @param url        the URL to fetch JSON data from
     * @param pathToRoot the path to navigate inside the JSON structure
     * @param jsonKeys   the keys to extract from the JSON data
     * @throws IOException          if an I/O error occurs while fetching data
     * @throws InterruptedException if the request is interrupted
     */
    private void getData(String url, String[] pathToRoot, String[] jsonKeys) throws IOException, InterruptedException {
        String json;
        if(!savedData.containsKey(url)) {
            //GET request        
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
    
            // HTTP response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            //Json String format
            json = response.body();
            //size check to see if tree traversal is efficient in terms of memory usage
            int jsonSizeInBytes = json.getBytes(StandardCharsets.UTF_8).length;
            System.out.println("Size of JSON in bytes: " + jsonSizeInBytes);
            
    
        }else {
            json = savedData.get(url);
        }
        
        saveData(url,json);
        
        JsonNode rootNode = objectMapper.readTree(json);
        traversal(rootNode,pathToRoot, jsonKeys);
    }

    
     /**
     * Traverses a JSON tree to extract relevant information based on specified keys.
     * 
     * @param root       the root JSON node
     * @param pathToRoot the path to navigate inside the JSON structure
     * @param jsonKeys   the keys to extract from the JSON data
     */
    private void traversal(JsonNode root, String[]pathToRoot ,  String[] jsonKeys) {
        JsonNode startNode = root;
        List<String> temp = new ArrayList<String>();
        for(String path : pathToRoot){
            startNode = startNode.path(path);
        }
        
        if(startNode.isArray()){
            
            for (JsonNode node : startNode) {
            
            
                for (String key : jsonKeys){
                    
                    temp.add(node.path(key).asText());
                }
            }
        }
        
        if (gameList.isEmpty()) {
            for (int i = 0; i < temp.size(); i += 3) {
                Game game = new Game(temp.get(i), temp.get(i+1), temp.get(i+2));
                gameList.add(game);
            }
        }
    }
    
    /**
     * Prints the list of games to the console.
     */
    public void printList() {
        for (Game game : gameList) {
            System.out.println(game.getName() + ", " + game.getMP() + ", " + game.getLPE());
        }
    }
    
    /**
     * Formats game playtime information. If a game has never been played, it is marked accordingly.
     */
    private void formatTimes() {
        for (Game game : gameList) {
            if(game.getMP().equals("0")) {
                game.setLPE("Never played");
            }
            else {
                game.EpochToHours();
            }
        }
    }

    /**
     * Filters out games that have never been played from the game list.
     */
    private void filterNonPlayed() {
        gameList.removeIf(game -> game.getLPE().equals("Never played"));
    }

    /**
     * Retrieves the list of games.
     * 
     * @return the list of processed {@code Game} objects
     */
    public List<Game> getGameList() {
        return gameList;
    }
}
    


