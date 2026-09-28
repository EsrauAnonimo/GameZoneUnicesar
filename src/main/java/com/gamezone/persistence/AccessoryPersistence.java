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
     * Malformed lines (wrong column count, bad numbers, unknown type) are
     * skipped with a console warning instead of crashing the load.
     */
    private Accessory parseLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        if (parts.length < 7) {
            System.out.println("Malformed accessory line in file: " + line);
            return null;
        }
        String type = parts[0];
        if (!type.equals("CONTROLLER") && !type.equals("CABLE") && !type.equals("MEMORY")) {
            System.out.println("Unknown accessory type in file: " + type);
            return null;
        }
        String id = parts[1];
        String title = parts[2];
        String extra1 = parts[5];
        String extra2 = parts[6];
        String compatibleIdsField = parts.length > 7 ? parts[7] : "";

        double price;
        int availableQuantity;
        try {
            price = Double.parseDouble(parts[3]);
            availableQuantity = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            System.out.println("Malformed accessory line in file: " + line);
            return null;
        }

        List<Console> compatibleConsoles = resolveCompatibleConsoles(compatibleIdsField);

        try {
            switch (type) {
                case "CONTROLLER": {
                    Controller controller = new Controller(id, title, price, availableQuantity, extra1);
                    controller.setCompatibleConsoles(compatibleConsoles);
                    return controller;
                }
                case "CABLE": {
                    double lengthMeters = Double.parseDouble(extra1);
                    Cable cable = new Cable(id, title, price, availableQuantity, lengthMeters, extra2);
                    cable.setCompatibleConsoles(compatibleConsoles);
                    return cable;
                }
                default: {
                    int capacityGb = Integer.parseInt(extra1);
                    Memory memory = new Memory(id, title, price, availableQuantity, capacityGb, extra2);
                    memory.setCompatibleConsoles(compatibleConsoles);
                    return memory;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Malformed accessory line in file: " + line);
            return null;
        }
    }

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

    private void ensureDataFolderExists() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}