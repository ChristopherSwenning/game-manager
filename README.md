# Game Manager
## Description
This Java program fetches data related to your Steam library, processes the data, and stores it in a MySQL database. It extracts game information such as playtime and last played times via API requests and categorizes them by genres. The application then interacts with the database to fetch insights and writes the results to a JSON file.

## Dependencies
- **JDBC** (Java Database Connectivity): Used for interacting with the MySQL database to insert and query data.
- **Jackson**: A JSON processing library for serializing and deserializing JSON data.
- **MySQL**: The relational database used for storing and retrieving game data.
## Requirements
- MySQL database set up with a table for storing game data.
- Three resource text files (urls.txt, config.txt, game_genres.txt) for the program to work.
## Configuration
Before running the program, ensure the following files are configured correctly:

1. urls.txt
This file should contain one row on the following format:

http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=YOURSTEAMAPIKEY&steamid=YOURSTEAMID&include_appinfo=true&format=json response-games name,playtime_forever,rtime_last_played

Replace YOURSTEAMAPIKEY and YOURSTEAMID with your actual Steam API key and Steam ID.

2. config.txt
This file should contain one row on the following format:

jdbc:mysql://IPWHEREMYSQLRUNS:PORT/YOURDATABASENAME

Replace IPWHEREMYSQLRUNS, PORT, and YOURDATABASENAME with your MySQL server's IP address, port, and database name.

3. game_genres.txt
This file should contain row entries for each game you want to include, formatted as:

gamename%genre

Example:

The Witcher 3%RPG

So far, 90 popular games have been added to game_genres.txt, but you can extend this list. 

**Note:** The program will still work without the `game_genres.txt` file. If the file is not provided, the program will automatically assign the genre "Unknown" to each game. This functionality was added to avoid the tedious process of manually updating `game_genres.txt` as you add more games to your library. For users who prefer to skip this step, the program will still fetch the most played games, but the genre-related functionality will not be included in the results.
 

## Running the Program
To run the program:

1. Make sure all the necessary dependencies are added to your pom.xml (for Maven users):
    - JDBC
    - Jackson
    - MySQL connector

2. Place the urls.txt, config.txt, and game_genres.txt files in the appropriate resource folder (src/main/resources).

3. Compile and run the program from the terminal. The program will prompt you to enter your MySQL credentials.

Upon successful execution, the program will query the database and write the results to a result.json file. This file will include:

- The top 50 most played games
- The 5 most popular game genres (as per the queries in DatabaseManager)

## Notes
- The program queries MySQL for specific insights. You can modify the queries in the List<String> sqlQueries within the DatabaseManager class if you'd like different results (e.g., a different number of games or genres).

- The program interacts with three main classes: Client, GenreMap, and DatabaseManager. Client fetches the game data from Steam, GenreMap maps games to genres, and DatabaseManager handles database interactions.

- The program is designed to be run via the terminal, so make sure you are running it from the appropriate command line or terminal window.