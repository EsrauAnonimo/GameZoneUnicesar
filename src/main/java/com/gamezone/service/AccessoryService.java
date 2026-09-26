package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.persistence.AccessoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules of the accessory module (controllers, cables
 * and memories). This is the only class in the module allowed to call
 * AccessoryPersistence; the UI layer must always go through this service.
 */
public class AccessoryService {

    private final AccessoryPersistence accessoryPersistence;
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
        this.accessories = accessoryPersistence.loadAll(availableConsoles);
    }

    /**
     * Registers a new controller and immediately persists the updated list.
     *
     * @param title              controller title
     * @param price              controller price
     * @param availableQuantity  initial available quantity
     * @param connectionType     connection type (wireless or wired)
     * @param compatibleConsoles consoles this controller is compatible with
     * @return the newly created controller
     */
    public Controller registerController(String title, double price, int availableQuantity,
                                          String connectionType, List<Console> compatibleConsoles) {
        String id = UUID.randomUUID().toString();
        Controller controller = new Controller(id, title, price, availableQuantity, connectionType, compatibleConsoles);
        accessories.add(controller);
        saveAll();
        return controller;
    }

    /**
     * Registers a new cable and immediately persists the updated list.
     *
     * @param title             cable title
     * @param price             cable price
     * @param availableQuantity initial available quantity
     * @param lengthMeters      cable length in meters
     * @param connectorType     connector type (HDMI, USB, optical, etc.)
     * @return the newly created cable
     */
    public Cable registerCable(String title, double price, int availableQuantity,
                                double lengthMeters, String connectorType) {
        String id = UUID.randomUUID().toString();
        // Los cables no manejan compatibilidad con consolas.
        Cable cable = new Cable(id, title, price, availableQuantity, lengthMeters, connectorType, new ArrayList<>());
        accessories.add(cable);
        saveAll();
        return cable;
    }

    /**
     * Registers a new memory and immediately persists the updated list.
     *
     * @param title              memory title
     * @param price              memory price
     * @param availableQuantity  initial available quantity
     * @param capacityGb         storage capacity in gigabytes
     * @param memoryType         memory type (SD, microSD, internal card)
     * @param compatibleConsoles consoles this memory is compatible with
     *                           (some memories are console-specific)
     * @return the newly created memory
     */
    public Memory registerMemory(String title, double price, int availableQuantity,
                                  int capacityGb, String memoryType, List<Console> compatibleConsoles) {
        String id = UUID.randomUUID().toString();
        Memory memory = new Memory(id, title, price, availableQuantity, capacityGb, memoryType, compatibleConsoles);
        accessories.add(memory);
        saveAll();