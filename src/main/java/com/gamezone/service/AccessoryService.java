package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryPersistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the business rules of the accessory module (controllers, cables
 * and memories). This is the only class in the module allowed to call
 * AccessoryPersistence; the UI layer must always go through this service.
 * Accessory objects are built and populated (including compatible consoles)
 * by the caller before being registered here.
 */
public class AccessoryService {

    private final AccessoryPersistence accessoryPersistence;
    private List<Accessory> accessories;

    /**
     * Creates the service and immediately loads the previously stored
     * accessories.
     *
     * @param accessoryPersistence repository used to read and write
     *                             accessory data
     */
    public AccessoryService(AccessoryPersistence accessoryPersistence) {
        this.accessoryPersistence = accessoryPersistence;
        this.accessories = accessoryPersistence.loadAll();
    }

    /**
     * Registers an already built controller and immediately persists the
     * updated list.
     *
     * @param controller the controller to register
     */
    public void registerController(Controller controller) {
        accessories.add(controller);
        saveAll();
    }

    /**
     * Registers an already built cable and immediately persists the
     * updated list.
     *
     * @param cable the cable to register
     */
    public void registerCable(Cable cable) {
        accessories.add(cable);
        saveAll();
    }

    /**
     * Registers an already built memory and immediately persists the
     * updated list.
     *
     * @param memory the memory to register
     */
    public void registerMemory(Memory memory) {
        accessories.add(memory);
        saveAll();
    }

    /**
     * Returns the list of all registered accessories, regardless of type.
     *
     * @return list of accessories
     */
    public List<Accessory> listAllAccessories() {
        return accessories;
    }

    /**
     * Filters the registered accessories by their concrete type.
     *
     * @param type "CONTROLLER", "CABLE" or "MEMORY" (case-insensitive)
     * @return list of accessories matching the given type
     */
    public List<Accessory> listAccessoriesByType(String type) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            if (matchesType(accessory, type)) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Finds all accessories compatible with the console identified by the
     * given id.
     *
     * @param consoleId id of the console to check compatibility against
     * @return list of accessories compatible with that console
     */
    public List<Accessory> findAccessoriesCompatibleWith(String consoleId) {
        List<Accessory> result = new ArrayList<>();
        for (Accessory accessory : accessories) {
            for (Console console : accessory.getCompatibleConsoles()) {
                if (console.getId().equals(consoleId)) {
                    result.add(accessory);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Finds an accessory by its id.
     *
     * @param id accessory id
     * @return the matching accessory, or null if none is found
     */
    public Accessory findById(String id) {
        return accessories.stream()
                .filter(accessory -> accessory.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the available quantity of an accessory and persists the
     * change. Used both for manual stock corrections and for automatic
     * inventory updates when a sale is registered.
     *
     * @param accessoryId id of the accessory to update
     * @param quantity    new available quantity
     */
    public void updateStock(String accessoryId, int quantity) {
        Accessory accessory = findById(accessoryId);
        if (accessory != null) {
            accessory.setAvailableQuantity(quantity);
            saveAll();
        }
    }

    /**
     * Checks whether a given accessory matches the requested type label.
     */
    private boolean matchesType(Accessory accessory, String type) {
        if (type == null) {
            return false;
        }
        return switch (type.toUpperCase()) {
            case "CONTROLLER" -> accessory instanceof Controller;
            case "CABLE" -> accessory instanceof Cable;
            case "MEMORY" -> accessory instanceof Memory;
            default -> false;
        };
    }

    /**
     * Delegates the save of the current accessory list to the persistence
     * layer.
     */
    private void saveAll() {
        accessoryPersistence.saveAll(accessories);
    }
}