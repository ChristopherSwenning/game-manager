package mygroup;

/**
 * Represents a game with its name, minutes played, and last played time in epoch format.
 */
public class Game {
    private String name;
    private String minutesPlayed;
    private String lastPlayedEpoch;

    /**
     * Constructs a new Game instance.
     *
     * @param name The name of the game.
     * @param minutesPlayed The total minutes played.
     * @param lastPlayedEpoch The last played time in epoch format (seconds since Unix epoch).
     */
    public Game(String name, String minutesPlayed, String lastPlayedEpoch) {
        this.name = name;
        this.minutesPlayed = minutesPlayed;
        this.lastPlayedEpoch = lastPlayedEpoch;
    }

    /**
     * Gets the name of the game.
     *
     * @return The game name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the total minutes played.
     *
     * @return The minutes played as a string.
     */
    public String getMP() {
        return minutesPlayed;
    }

    /**
     * Gets the last played time in epoch format.
     *
     * @return The last played time as a string.
     */
    public String getLPE() {
        return lastPlayedEpoch;
    }

    /**
     * Sets the last played time.
     *
     * @param str The new last played time in epoch format.
     */
    public void setLPE(String str) {
        lastPlayedEpoch = str;
    }
    
    /**
     * Converts the last played epoch time to hours since last played.
     * Updates {@code lastPlayedEpoch} with the formatted time difference in hours.
     */
    public void EpochToHours() {
        long longEpoch = Long.parseLong(lastPlayedEpoch);
        long currentEpoch = System.currentTimeMillis() / 1000;
        double diff = (currentEpoch - longEpoch) / 3600.0;
        lastPlayedEpoch = String.format("%.2f",diff); 
    }
}
