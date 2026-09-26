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
 */
public class AccessoryService {

    private final AccessoryPersistence accessoryPersistence;
    private List<Console> availableConsoles;
    private List<Accessory> accessories;

    /**
     * Creates the service and immediately loads the previously stored
     * accessories, resolving their compatible consoles against the given
     * list of consoles known by the system.
     *
     * @param accessoryPersistence repository used to read and write
     *                             accessory data
     * @param availableConsoles    consoles currently registered in the
     *                             system, needed to resolve compatibility
     */
    public AccessoryService(AccessoryPersistence accessoryPersistence, List<Console> availableConsoles) {
        this.accessoryPersistence = accessoryPersistence;
        this.availableConsoles = new ArrayList<>(availableConsoles);
        this.accessories = new ArrayList<>();

        // Carga automática al iniciar la aplicación.
        this.accessories.addAll(accessoryPersistence.loadAll(this.availableConsoles));
    }

    /**
     * Replaces the list of consoles used to resolve accessory compatibility
     * and reloads the stored accessories, so that consoles registered after
     * startup are taken into account.
     *
     * @param availableConsoles consoles currently registered in the system
     */
    public void setAvailableConsoles(List<Console> availableConsoles) {
        this.availableConsoles = new ArrayList<>(availableConsoles);
        this.accessories = accessoryPersistence.loadAll(this.availableConsoles);
    }

    /**
     * Registers a new controller (already built by the caller) and
     * immediately persists the updated list.
     *
     * @param controller the controller to register
     */
    public void registerController(Controller controller) {
        accessories.add(controller);
        saveAll(); // Guardado automático tras la operación.
    }

    /**
     * Registers a new cable (already built by the caller) and immediately
     * persists the updated list.
     *
     * @param cable the cable to register
     */
    public void registerCable(Cable cable) {
        accessories.add(cable);
        saveAll(); // Guardado automático tras la operación.
    }

    /**
     * Registers a new memory (already built by the caller) and immediately
     * persists the updated list.
     *
     * @param memory the memory to register
     */
    public void registerMemory(Memory memory) {
        accessories.add(memory);
        saveAll(); // Guardado automático tras la operación.
    }

    /**
     * Returns the list of all registered accessories.
     *
     * @return list of accessories
     */
    public List<Accessory> listAllAccessories() {
        return accessories;
    }

    /**
     * Returns the accessories of a single type. The accepted type names are
     * the same discriminators used in the persistence file: CONTROLLER,
     * CABLE and MEMORY (case insensitive).
     *
     * @param type type of accessory to filter by
     * @return list of accessories of the given type (empty if the type is
     *         unknown)
     */
    public List<Accessory> listAccessoriesByType(String type) {
        List<Accessory> result = new ArrayList<>();
        if (type == null) {
            return result;
        }
        String wanted = type.trim().toUpperCase();
        for (Accessory accessory : accessories) {
            if (wanted.equals("CONTROLLER") && accessory instanceof Controller
                    || wanted.equals("CABLE") && accessory instanceof Cable
                    || wanted.equals("MEMORY") && accessory instanceof Memory) {
                result.add(accessory);
            }
        }
        return result;
    }

    /**
     * Returns the accessories compatible with the console having the given id.
     *
     * @param consoleId id of the console to look up
     * @return list of compatible accessories (empty if the id does not match
     *         any registered console)
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
     * Finds an accessory by id. Used by the sale UI to resolve an item id
     * that may belong either to a product or to an accessory.
     *
     * @param id accessory id
     * @return the matching accessory, or null if none is found
     */
    public Accessory findById(String id) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equals(id)) {
                return accessory;
            }
        }
        return null;
    }

    /**
     * Updates the stock of an accessory and persists the change. Called by
     * the sale service after each sale.
     *
     * @param accessoryId  id of the accessory to update
     * @param newQuantity  new available quantity
     */
    public void updateStock(String accessoryId, int newQuantity) {
        for (Accessory accessory : accessories) {
            if (accessory.getId().equals(accessoryId)) {
                accessory.setAvailableQuantity(newQuantity);
                break;
            }
        }
        saveAll();
    }

    /**
     * Delegates the save of the whole accessory list to the persistence layer.
     */
    private void saveAll() {
        accessoryPersistence.saveAll(accessories);
    }
}
