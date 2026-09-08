package com.gamezone;

import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleUI;

public class Main {

    public static void main(String[] args) {
        PersonPersistence personPersistence = new PersonPersistence();
        PersonService personService = new PersonService(personPersistence);

        ProductService productService = new ProductService();

        SalePersistence salePersistence = new SalePersistence();
        SaleService saleService = new SaleService(salePersistence, productService);

        personService.preloadVendorsIfEmpty();

        ConsoleUI ui = new ConsoleUI(personService, productService, saleService);
        ui.showMainMenu();
    }
}