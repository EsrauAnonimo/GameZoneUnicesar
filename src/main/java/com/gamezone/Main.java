package com.gamezone;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.persistence.AccessoryPersistence;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleUI;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        PersonPersistence personPersistence = new PersonPersistence();
        PersonService personService = new PersonService(personPersistence);

        ProductService productService = new ProductService();

        AccessoryPersistence accessoryPersistence = new AccessoryPersistence();
        AccessoryService accessoryService = new AccessoryService(accessoryPersistence, currentConsoles(productService));

        SalePersistence salePersistence = new SalePersistence();
        SaleService saleService = new SaleService(salePersistence, productService, accessoryService);

        personService.preloadVendorsIfEmpty();

        ConsoleUI ui = new ConsoleUI(personService, productService, saleService, accessoryService);
        ui.showMainMenu();
    }

    /**
     * Extracts the consoles currently registered as products, so the
     * accessory module can resolve accessory compatibility against them.
     *
     * @param productService service holding the registered products
     * @return list of registered consoles (empty on the very first run)
     */
    private static List<Console> currentConsoles(ProductService productService) {
        List<Console> consoles = new ArrayList<>();
        for (Product product : productService.listAllProducts()) {
            if (product instanceof Console console) {
                consoles.add(console);
            }
        }
        return consoles;
    }
}