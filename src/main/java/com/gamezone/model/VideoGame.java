package com.gamezone.model;

/**
 * Represents a video game product in the GameZone system.
 * This class extends Product and adds specific attributes for video games.
 */
public class VideoGame extends Product {

    private String platform;
    private String genre;
    private String ageRating;

    public VideoGame(String id, String title, double price, int availableQuantity,
                     String platform, String genre, String ageRating) {
        super(id, title, price, availableQuantity);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    public String getPlatform() {
        return platform;
    }

    public String getGenre() {
        return genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    @Override
    public String getDescription() {
        return "VideoGame{" +
               "price=" + getPrice() +
               ", availableQuantity=" + getAvailableQuantity() +
               ", platform='" + platform + '\'' +
               ", genre='" + genre + '\'' +
               ", ageRating='" + ageRating + '\'' +
               '}';
    }
}