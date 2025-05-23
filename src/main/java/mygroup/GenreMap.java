package mygroup;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;


/**
 * Manages a mapping of game names to their corresponding genres.
 * The mapping is loaded from a resource file and can be accessed as a {@link Map}.
 */
public class GenreMap {
    private Map<String,String> myMap;
    private List<Game> gameList;
    private static final Logger logger = AppLogger.get();

    /**
     * Constructs a GenreMap instance.
     *
     * @param gameList A list of games for which genres will be mapped.
     */
    public GenreMap(List<Game> gameList) {
        myMap = new LinkedHashMap<String,String>();
        this.gameList = gameList;
        
    }

    /**
     * Loads game genre mappings from a resource file named "game_genres.txt".
     * The file should contain lines formatted as "gameName%genre".
     * Only games that exist in the provided game list will be added to the map.
     */
    public void addFromResource() {
        InputStream inputstream = Client.class.getClassLoader().getResourceAsStream("game_genres.txt");
        if (inputstream == null) {
            throw new IllegalStateException("Error loading in game_genres.txt from resources");
            
        }
        try (Scanner scanner = new Scanner(inputstream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split("%");
                if(splitted.length != 2) {
                    throw new IllegalArgumentException("Malformed line in game_genres.txt");
                }
                String name = splitted[0];
                String genre = splitted[1];
                for (Game game : gameList) {
                    if(name.equals(game.getName()) && !myMap.containsKey(name)) {
                        myMap.put(name,genre);
                    }
                }

                
            }
        }
       

    }
    /**
     * Retrieves the mapping of game names to their genres.
     *
     * @return A map where keys are game names and values are their respective genres.
     */
    public Map<String,String> getMap() {
        return myMap;
    }
    /**
     * Prints all stored game-genre mappings to the console.
     */
    public void printMap() {
        myMap.forEach((key,value) -> logger.info(key + " " + value));
    }
}
