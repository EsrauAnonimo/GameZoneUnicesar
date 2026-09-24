package com.gamezone;

import com.gamezone.persistence.AccessoryPersistence;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleUI;

public class Main {

    public static void main(String[] args) {
        PersonPersistence personPersistence = new PersonPersistence();
        PersonService personService = new PersonService(personPersistence);

        ProductService productService = new ProductService();

        AccessoryPersistence accessoryPersistence = new AccessoryPersistence();
        AccessoryService accessoryService = new AccessoryService(accessoryPersistence);

        SalePersistence salePersistence = new SalePersistence();
        SaleService saleService = new SaleService(salePersistence, productService, accessoryService);

        personService.preloadVendorsIfEmpty();

        ConsoleUI ui = new ConsoleUI(personService, productService, saleService, accessoryService);
        ui.showMainMenu();
    }
}