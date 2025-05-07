package mygroup;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Manages database operations including inserting and retrieving game data.
 * This class interacts with a MySQL database, executing queries to store and fetch game-related information.
 */
public class DatabaseManager {
    private Client client;
    private GenreMap genreMap;
    private final List<String> sqlQueries;
    private Map<String,String> resultMap;
    private static final Logger logger = AppLogger.get();
    
    /**
     * Constructs a DatabaseManager with a Client and GenreMap.
     * 
     * @param client   The client containing the game data.
     * @param genreMap The mapping of game names to genres.
     */
    public DatabaseManager(Client client, GenreMap genreMap) {
        this.client = client;
        this.genreMap = genreMap;
        this.resultMap = new LinkedHashMap<String,String>();
        sqlQueries = new ArrayList<String>();
        sqlQueries.add("""
                    SELECT SUM(minutes_played) AS minutes_played,genres 
                    FROM games
                    GROUP BY genres
                    ORDER BY SUM(minutes_played) DESC
                    LIMIT 5;
                    """);
        sqlQueries.add("""
                    SELECT SUM(minutes_played) AS minutes_played,name 
                    FROM games
                    GROUP BY name
                    ORDER BY SUM(minutes_played) DESC
                    LIMIT 50;
                """);
    }
    
    
    /**
     * Executes the database operations: reading config, prompting for user credentials,
     * inserting game data, and retrieving query results.
     */
    public void run() {
        List<String> config = readConfig();
        String url = config.get(0);
        
        String username = readUsername();
        char[] password = readPassword();

        try (Connection conn = DriverManager.getConnection(url, username, new String(password))) {
            Arrays.fill(password, '\0');
            logger.info("Connected to MySQL successfully!");
            insertData(conn);
            selectData(conn);
        }catch (SQLException e) {
            logger.log(Level.FINE, "Error while connecting to MySQL", e);
            throw new RuntimeException("Error while connecting to MySQL");
        }
        
    }
    
    /**
     * Reads database configuration from a resource file.
     * 
     * @return A list containing database connection details.
     */
    private static List<String> readConfig() {
        List<String> configs = new ArrayList<String>();
        InputStream inputstream = Client.class.getClassLoader().getResourceAsStream("config.txt");
        if (inputstream == null) {
            throw new IllegalStateException("Error loading in file from resources");
        }
        
        try (Scanner scanner = new Scanner(inputstream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                configs.add(line);
            }
        }
        
        return configs;
    }

    /**
     * Prompts the user to enter the MySQL username.
     * 
     * @return The username inputted by the user.
     */
    private String readUsername() {
        logger.info("Enter MySQL username: ");
        Console c = System.console();
        if(c == null) {
            throw new IllegalStateException("No console available for credentials");
        }
        return c.readLine();
    }
    
    /**
     * Prompts the user to enter the MySQL password.
     * 
     * @return The password inputted by the user.
     */
    private char[] readPassword() {
        logger.info("Enter MySQL password: ");
        Console c = System.console();
        if(c == null) {
            throw new IllegalStateException("No console available for credentials");
        }
        return c.readPassword();
    }
    
    /**
     * Inserts game data from the client into the database.
     * 
     * @param url      The database URL.
     * @param username The database username.
     * @param password The database password.
     */
    private void insertData(Connection conn) {
        
        String insertOrUpdate = "INSERT INTO games (name, minutes_played, last_played_hours, genres) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "minutes_played = VALUES(minutes_played), " +
                            "last_played_hours = VALUES(last_played_hours), " +
                            "genres = VALUES(genres)";
        try (PreparedStatement insertStatement = conn.prepareStatement(insertOrUpdate)) {
                for (Game game : client.getGameList()) {
                    insertStatement.setString(1, game.getName());
                    insertStatement.setString(2, game.getMP());
                    insertStatement.setString(3, game.getLPE());
                        
                    String genre = Optional.ofNullable(genreMap.getMap().get(game.getName()))
                        .map(s -> s.strip()).orElse("Unknown");
                    insertStatement.setString(4,genre);
                    insertStatement.executeUpdate();
                }
                    
                logger.info("Data inserted successfully");
        } catch (SQLException e ) {
            logger.log(Level.FINE, "Error while executing SQL statement", e);
            throw new RuntimeException("Error while executing SQL statement");
        }

    } 
    
    
    /**
     * Executes predefined SQL queries and processes results.
     * 
     * @param url      The database URL.
     * @param username The database username.
     * @param password The database password.
     */
    private void selectData(Connection conn)  {
        
        for (String query : sqlQueries) {
            try(PreparedStatement selectStatement = conn.prepareStatement(query)) {
                ResultSet resultSet = selectStatement.executeQuery();
                    
                convertResultSet(resultSet,query);
            }catch (SQLException e) {
                logger.log(Level.FINE, "Error while executing SQL query", e);
                throw new RuntimeException("Error while executing SQL query");
            } 
        }
        logger.info("Data extracted successfully");
            
    }
    
    
    /**
     * Converts the SQL query result set into a map for further processing.
     * 
     * @param resultSet The result set from an executed SQL query.
     * @param query     The SQL query that was executed.
     * @throws SQLException If an error occurs while reading the result set.
     */
    private void convertResultSet(ResultSet resultSet, String query) throws SQLException {
        
        while (resultSet.next()) {
            String minutes = resultSet.getString("minutes_played");

            if (query.contains("GROUP BY genres")) {
                String genre = resultSet.getString("genres");
                resultMap.put(genre,minutes);
            }
            if(query.contains("GROUP BY name")) {
                String name = resultSet.getString("name");
                resultMap.put(name,minutes);
            }
        }
    }

    /**
     * Writes the query results to a JSON file.
     */
    public void writeResultToFile() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(resultMap);
            File file = new File("result.json");
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(json);
                logger.info("Data written to result.json");
            } catch(IOException e) {
                logger.log(Level.FINE, "Error writing to result.json", e);
                throw new RuntimeException("Error writing to result.json");
            }

        } catch (JsonProcessingException e) {
            logger.log(Level.FINE, "Error writing to result.json", e);
            throw new RuntimeException("Error processing resultMap");
        }
    }
}
