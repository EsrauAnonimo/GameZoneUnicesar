package com.gamezone.persistence;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence for Promotion entities using a CSV file.
 * The repository is the only component in the promotion module allowed
 * to access data/promotions.csv.
 */
public class PromotionRepository {

    private static final String FILE_NAME = "data/promotions.csv";
    private static final String CSV_HEADER = "type;id;name;startDate;endDate;percentage;targetCategory;minimumQuantity";
    private static final String SEPARATOR = ";";

    /**
     * Saves the list of promotions into the CSV file, overwriting existing content.
     *
     * @param promotions the list of promotions to save
     */
    public void saveAll(List<Promotion> promotions) {
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (Promotion promotion : promotions) {
                writer.write(buildCsvLine(promotion));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving promotions: " + e.getMessage());
        }
    }

    /**
     * Loads all promotions from data/promotions.csv.
     *
     * @return list of loaded promotions, or an empty list if file doesn't exist or is empty
     */
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return promotions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("type;") || line.equalsIgnoreCase(CSV_HEADER)) {
                    continue;
                }
                Promotion promotion = parseCsvLine(line);
                if (promotion != null) {
                    promotions.add(promotion);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading promotions: " + e.getMessage());
        }

        return promotions;
    }

    private String buildCsvLine(Promotion promotion) {
        String type;
        String targetCategory = "";
        String minQuantity = "";
        double percentage = 0.0;

        if (promotion instanceof PercentageDiscount percentageDiscount) {
            type = "PERCENTAGE";
            percentage = percentageDiscount.getPercentage();
        } else if (promotion instanceof CategoryDiscount categoryDiscount) {
            type = "CATEGORY";
            percentage = categoryDiscount.getPercentage();
            targetCategory = categoryDiscount.getTargetCategory() != null ? categoryDiscount.getTargetCategory() : "";
        } else if (promotion instanceof BulkPurchaseDiscount bulkDiscount) {
            type = "BULK";
            percentage = bulkDiscount.getPercentage();
            minQuantity = String.valueOf(bulkDiscount.getMinimumQuantity());
        } else {
            type = "UNKNOWN";
        }

        return String.join(SEPARATOR,
                type,
                promotion.getId(),
                promotion.getName(),
                promotion.getStartDate().toString(),
                promotion.getEndDate().toString(),
                formatPercentage(percentage),
                targetCategory,
                minQuantity);
    }

    private Promotion parseCsvLine(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length < 6) {
            System.out.println("Malformed promotion line in file: " + line);
            return null;
        }

        String type = parts[0].trim().toUpperCase();
        String id = parts[1].trim();
        String name = parts[2].trim();

        LocalDate startDate;
        LocalDate endDate;
        double percentage;

        try {
            startDate = LocalDate.parse(parts[3].trim());
            endDate = LocalDate.parse(parts[4].trim());
            percentage = Double.parseDouble(parts[5].trim());
        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("Invalid data format in promotion line: " + line);
            return null;
        }

        switch (type) {
            case "PERCENTAGE":
                return new PercentageDiscount(id, name, startDate, endDate, percentage);

            case "CATEGORY": {
                String targetCategory = parts.length > 6 ? parts[6].trim() : "";
                return new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
            }

            case "BULK": {
                int minQty = 0;
                if (parts.length > 7 && !parts[7].isBlank()) {
                    try {
                        minQty = Integer.parseInt(parts[7].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid minimum quantity in bulk promotion line: " + line);
                        return null;
                    }
                }
                return new BulkPurchaseDiscount(id, name, startDate, endDate, minQty, percentage);
            }

            default:
                System.out.println("Unknown promotion type in file: " + type);
                return null;
        }
    }

    private String formatPercentage(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void ensureDataFolderExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
}
