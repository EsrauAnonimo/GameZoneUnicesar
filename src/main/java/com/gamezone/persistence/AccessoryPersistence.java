package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Accessory data (Controllers, Cables and
 * Memories) to and from a single CSV file. This class belongs to the
 * persistence layer and is the ONLY class in the accessory module allowed
 * to touch the file system.
 */
public class AccessoryPersistence {

    // Archivo unico con separador ";" y un discriminador de tipo por linea.
    private static final String ACCESSORIES_FILE = "data/accessories.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String CONSOLE_ID_SEPARATOR = "\\|";
    private static final String CONSOLE_ID_JOINER = "|";

    /**
     * Saves the given list of accessories to the accessories file,
     * overwriting any previous content.
     *
     * @param accessories list of accessories to persist
     */
    public void saveAll(List<Accessory> accessories) {
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCESSORIES_FILE))) {
            for (Accessory accessory : accessories) {
                writer.write(buildLine(accessory));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving accessories: " + e.getMessage());
        }
    }

    /**
     * Loads all accessories stored in the accessories file. Compatible
     * console references are reconstructed as minimal placeholder Console
     * objects carrying only their id, since this layer has no access to
     * the real console catalog; that id is enough for compatibility
     * comparisons elsewhere in the service.
     *
     * @return list of accessories found in the file (empty list if the
     *         file does not exist yet, e.g. on the very first execution)
     */
    public List<Accessory> loadAll() {
        List<Accessory> accessories = new ArrayList<>();
        File file = new File(ACCESSORIES_FILE);
        if (!file.exists()) {
            // Primera ejecucion: aun no hay archivo, se retorna lista vacia.
            return accessories;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Accessory accessory = parseLine(line);
                if (accessory != null) {
                    accessories.add(accessory);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading accessories: " + e.getMessage());
        }
        return accessories;
    }

    /**
     * Builds a single CSV line for the given accessory, using the correct
     * discriminator and extra fields depending on its concrete type.
     */
    private String buildLine(Accessory accessory) {
        String type;
        String extra1;
        String extra2;

        if (accessory instanceof Controller controller) {
            type = "CONTROLLER";
            extra1 = controller.getConnectionType();
            extra2 = "";
        } else if (accessory instanceof Cable cable) {
            type = "CABLE";
            extra1 = String.valueOf(cable.getLengthMeters());
            extra2 = cable.getConnectorType();
        } else if (accessory instanceof Memory memory) {
            type = "MEMORY";
            extra1 = String.valueOf(memory.getCapacityGb());
            extra2 = memory.getMemoryType();
        } else {
            // No deberia pasar: solo existen estos 3 tipos concretos.
            type = "UNKNOWN";
            extra1 = "";
            extra2 = "";
        }

        String compatibleIds = buildCompatibleIdsField(accessory);

        return String.join(FIELD_SEPARATOR,
                type,
                accessory.getId(),
                accessory.getTitle(),
                String.valueOf(accessory.getPrice()),
                String.valueOf(accessory.getAvailableQuantity()),
                extra1,
                extra2,
                compatibleIds);
    }

    /**
     * Joins the ids of an accessory's compatible consoles with the "|"
     * separator, or returns an empty string if there are none.
     */
    private String buildCompatibleIdsField(Accessory accessory) {
        List<Console> compatibleConsoles = accessory.getCompatibleConsoles();
        if (compatibleConsoles == null || compatibleConsoles.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < compatibleConsoles.size(); i++) {
            if (i > 0) {
                builder.append(CONSOLE_ID_JOINER);
            }
            builder.append(compatibleConsoles.get(i).getId());
        }
        return builder.toString();
    }

    /**
     * Parses a single CSV line into the corresponding Accessory subclass,
     * then attaches its compatible consoles (reconstructed by id only).
     */
    private Accessory parseLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int availableQuantity = Integer.parseInt(parts[4]);
        String extra1 = parts[5];
        String extra2 = parts[6];
        String compatibleIdsField = parts.length > 7 ? parts[7] : "";

        List<Console> compatibleConsoles = resolveCompatibleConsoles(compatibleIdsField);

        Accessory accessory;
        switch (type) {
            case "CONTROLLER":
                accessory = new Controller(id, title, price, availableQuantity, extra1);
                break;
            case "CABLE":
                double lengthMeters = Double.parseDouble(extra1);
                accessory = new Cable(id, title, price, availableQuantity, lengthMeters, extra2);
                break;
            case "MEMORY":
                int capacityGb = Integer.parseInt(extra1);
                accessory = new Memory(id, title, price, availableQuantity, capacityGb, extra2);
                break;
            default:
                // Discriminador desconocido: se ignora la linea.
                System.out.println("Unknown accessory type in file: " + type);
                return null;
        }
        accessory.setCompatibleConsoles(compatibleConsoles);
        return accessory;
    }

    /**
     * Resolves a "|"-separated field of console ids into placeholder
     * Console objects carrying only their id. This layer has no access to
     * the real console catalog, but the id is all that compatibility
     * comparisons in AccessoryService need.
     */
    private List<Console> resolveCompatibleConsoles(String compatibleIdsField) {
        List<Console> resolved = new ArrayList<>();
        if (compatibleIdsField == null || compatibleIdsField.isBlank()) {
            return resolved;
        }
        String[] ids = compatibleIdsField.split(CONSOLE_ID_SEPARATOR);
        for (String consoleId : ids) {
            resolved.add(new Console(consoleId, "", 0, 0, "", "", ""));
        }
        return resolved;
    }

    /**
     * Creates the data folder if it does not exist yet, so the first save
     * does not fail.
     */
    private void ensureDataFolderExists() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}