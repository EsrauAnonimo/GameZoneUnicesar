package com.gamezone.model;

/**
 * Represents a video game product in the GameZone system.
 * This class extends Product and adds specific attributes for video games.
 */
public class VideoGame extends Product {

    // Private attributes specific to video games
    private String platform;
    private String genre;
    private String ageRating;

    /**
     * Constructor for a VideoGame.
     * Calls the parent constructor with common attributes.
     * 
     * @param id Unique code that identifies the product
     * @param title Name or title of the video game
     * @param price Sale price of the video game
     * @param availableQuantity Number of units available in stock
     * @param platform Gaming platform (e.g., PlayStation, Xbox, PC)
     * @param genre Game genre (e.g., Action, RPG, Adventure)
     * @param ageRating Age rating (e.g., E, T, M, AO)
     */
    public VideoGame(String id, String title, double price, int availableQuantity,
                     String platform, String genre, String ageRating) {
        super(id, title, price, availableQuantity);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    /**
     * Gets the gaming platform.
     * 
     * @return The platform name
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Gets the game genre.
     * 
     * @return The genre name
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Gets the age rating.
     * 
     * @return The age rating
     */
    public String getAgeRating() {
        return ageRating;
    }

    /**
     * Generates a complete description of the video game.
     * Implements the abstract method from Product class.
     * 
     * @return A formatted string with all video game details
     */
    @Override
    public String getDescription() {
        return "VideoGame{" +
               "id='" + getId() + '\'' +
               ", title='" + getTitle() + '\'' +
               ", price=" + getPrice() +
               ", availableQuantity=" + getAvailableQuantity() +
               ", platform='" + platform + '\'' +
               ", genre='" + genre + '\'' +
               ", ageRating='" + ageRating + '\'' +
               '}';
    }
}