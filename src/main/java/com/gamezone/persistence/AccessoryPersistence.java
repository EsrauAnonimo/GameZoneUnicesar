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
     * Loads all accessories stored in the accessories file, resolving each
     * accessory's compatible console ids against the given list of known
     * consoles.
     *
     * @param availableConsoles consoles currently registered in the system,
     *                           used to resolve compatibility ids into
     *                           actual Console objects
     * @return list of accessories found in the file (empty list if the file
     *         does not exist yet, e.g. on the very first execution)
     */
    public List<Accessory> loadAll(List<Console> availableConsoles) {
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
                Accessory accessory = parseLine(line, availableConsoles);
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
     *
     * @param accessory accessory to serialize
     * @return the CSV line representing this accessory
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
     * resolving compatible console ids against the given list of consoles.
     */
    private Accessory parseLine(String line, List<Console> availableConsoles) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        if (parts.length < 7) {
            // Linea incompleta o corrupta: se ignora.
            System.out.println("Malformed accessory line in file: " + line);
            return null;
        }
        String type = parts[0];
        if (!type.equals("CONTROLLER") && !type.equals("CABLE") && !type.equals("MEMORY")) {
            // Discriminador desconocido (por ejemplo, una fila de encabezado):
            // se ignora la linea.
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

        List<Console> compatibleConsoles = resolveCompatibleConsoles(compatibleIdsField, availableConsoles);

        switch (type) {
            case "CONTROLLER": {
                Controller controller = new Controller(id, title, price, availableQuantity, extra1);
                controller.setCompatibleConsoles(compatibleConsoles);
                return controller;
            }
            case "CABLE": {
                Cable cable;
                try {
                    cable = new Cable(id, title, price, availableQuantity, Double.parseDouble(extra1), extra2);
                } catch (NumberFormatException e) {
                    System.out.println("Malformed cable line in file: " + line);
                    return null;
                }
                cable.setCompatibleConsoles(compatibleConsoles);
                return cable;
            }
            default: {
                Memory memory;
                try {
                    memory = new Memory(id, title, price, availableQuantity, Integer.parseInt(extra1), extra2);
                } catch (NumberFormatException e) {
                    System.out.println("Malformed memory line in file: " + line);
                    return null;
                }
                memory.setCompatibleConsoles(compatibleConsoles);
                return memory;
            }
        }
    }

    /**
     * Resolves a "|"-separated field of console ids into actual Console
     * objects, matching against the given list of available consoles.
     */
    private List<Console> resolveCompatibleConsoles(String compatibleIdsField, List<Console> availableConsoles) {
        List<Console> resolved = new ArrayList<>();
        if (compatibleIdsField == null || compatibleIdsField.isBlank()) {
            return resolved;
        }
        String[] ids = compatibleIdsField.split(CONSOLE_ID_SEPARATOR);
        for (String consoleId : ids) {
            for (Console console : availableConsoles) {
                if (console.getId().equals(consoleId)) {
                    resolved.add(console);
                    break;
                }
            }
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