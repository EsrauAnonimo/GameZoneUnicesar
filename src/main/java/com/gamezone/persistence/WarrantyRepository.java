package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists warranties in a CSV file. It only stores and loads identifiers
 * (warranty, product and sale ids) and never depends on sales or products:
 * the service layer is the one that resolves those references.
 * <p>
 * The first column is a type discriminator (BASIC or EXTENDED):
 * <pre>
 * type;id;productId;saleId;startDate
 * </pre>
 */
public class WarrantyRepository {

    /** Discriminator used for basic warranties. */
    public static final String TYPE_BASIC = "BASIC";

    /** Discriminator used for extended warranties. */
    public static final String TYPE_EXTENDED = "EXTENDED";

    private static final String DEFAULT_PATH = "data/warranties.csv";
    private static final String SEPARATOR = ";";
    private static final int EXPECTED_FIELDS = 5;

    private final Path filePath;

    /** Creates a repository that uses the default file {@code data/warranties.csv}. */
    public WarrantyRepository() {
        this(DEFAULT_PATH);
    }

    /**
     * Creates a repository backed by the given file.
     *
     * @param filePath path of the CSV file
     */
    public WarrantyRepository(String filePath) {
        this.filePath = Paths.get(filePath);
    }

    /**
     * Overwrites the CSV file with the given records.
     *
     * @param records the warranty records to persist
     */
    public void saveAll(List<WarrantyRecord> records) {
        List<String> lines = new ArrayList<>();
        for (WarrantyRecord record : records) {
            lines.add(String.join(SEPARATOR, record.getType(), record.getId(),
                    record.getProductId(), record.getSaleId(), record.getStartDate().toString()));
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el archivo " + filePath
                    + " (operacion: guardar garantias): " + e.getMessage());
        }
    }

    /**
     * Loads every warranty record from the CSV file. Malformed lines are
     * skipped.
     *
     * @return the records, or an empty list if the file does not exist
     */
    public List<WarrantyRecord> loadAll() {
        List<WarrantyRecord> records = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                WarrantyRecord record = parse(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("No se pudo leer el archivo " + filePath
                    + " (operacion: cargar garantias): " + e.getMessage());
        }
        return records;
    }

    private WarrantyRecord parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != EXPECTED_FIELDS) {
            return null;
        }
        if (!TYPE_BASIC.equals(fields[0]) && !TYPE_EXTENDED.equals(fields[0])) {
            return null;
        }
        try {
            return new WarrantyRecord(fields[0], fields[1], fields[2], fields[3],
                    LocalDate.parse(fields[4]));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Identifier-only representation of a warranty, as stored in the CSV file.
     */
    public static class WarrantyRecord {

        private final String type;
        private final String id;
        private final String productId;
        private final String saleId;
        private final LocalDate startDate;

        /**
         * Creates a record.
         *
         * @param type      TYPE_BASIC or TYPE_EXTENDED
         * @param id        warranty id
         * @param productId id of the covered product
         * @param saleId    id of the sale the product belongs to
         * @param startDate date the warranty starts
         */
        public WarrantyRecord(String type, String id, String productId, String saleId,
                              LocalDate startDate) {
            this.type = type;
            this.id = id;
            this.productId = productId;
            this.saleId = saleId;
            this.startDate = startDate;
        }

        /** @return the type discriminator */
        public String getType() {
            return type;
        }

        /** @return the warranty id */
        public String getId() {
            return id;
        }

        /** @return the id of the covered product */
        public String getProductId() {
            return productId;
        }

        /** @return the id of the sale */
        public String getSaleId() {
            return saleId;
        }

        /** @return the start date */
        public LocalDate getStartDate() {
            return startDate;
        }
    }
}
