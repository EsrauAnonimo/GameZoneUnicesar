package com.gamezone;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.persistence.AccessoryPersistence;
import com.gamezone.persistence.PersonPersistence;
import com.gamezone.persistence.ProductPersistence;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
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

        // Taller 2: módulo de promociones. El repositorio es la única capa
        // que toca el archivo data/promotions.csv.
        PromotionRepository promotionRepository = new PromotionRepository();
        PromotionService promotionService = new PromotionService(promotionRepository);

        SaleService saleService = new SaleService(salePersistence, productService, accessoryService,
                promotionService);

        // Taller 3: módulo de devoluciones. El repositorio es la única capa
        // que toca el archivo data/returns.csv y necesita los servicios para
        // resolver la venta original y los productos devueltos al leer.
        ReturnRepository returnRepository = new ReturnRepository(saleService, productService, accessoryService);
        ReturnService returnService = new ReturnService(returnRepository, saleService, productService,
                accessoryService);

        personService.preloadVendorsIfEmpty();

        ConsoleUI ui = new ConsoleUI(personService, productService, saleService, accessoryService,
                promotionService, returnService);
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