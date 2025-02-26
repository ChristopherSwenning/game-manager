package mygroup;
import java.io.IOException;


public class Application {

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.run();
        GenreMap genreMap = new GenreMap(client.getGameList());
        genreMap.addFromResource();
        DatabaseManager dbm = new DatabaseManager(client,genreMap);
        dbm.run();
        dbm.writeResultToFile();
        
        
        
        
        
        
        
    }
}
