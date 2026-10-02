package com.gamezone.ui;

import com.gamezone.model.*;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private static final String CATEGORY_VIDEOGAME = "VIDEOGAME";
    private static final String CATEGORY_CONSOLE = "CONSOLE";

    private PersonService personService;
    private ProductService productService;
    private SaleService saleService;
    private AccessoryService accessoryService;
    private PromotionService promotionService;
    private ReturnService returnService;
    private WarrantyService warrantyService;
    private Scanner scanner;

    public ConsoleUI(PersonService personService, ProductService productService, SaleService saleService,
                      AccessoryService accessoryService, PromotionService promotionService,
                      ReturnService returnService, WarrantyService warrantyService) {
        this.personService = personService;
        this.productService = productService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
        this.returnService = returnService;
        this.warrantyService = warrantyService;
        this.scanner = new Scanner(System.in);
    }

    public void showMainMenu() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n=== GameZone Unicesar ===");
            System.out.println("1. Menu de productos");
            System.out.println("2. Menu de personas");
            System.out.println("3. Menu de ventas");
            System.out.println("4. Menu de accesorios");
            System.out.println("5. Menu de promociones");
            System.out.println("6. Menu de devoluciones");
            System.out.println("7. Menu de garantias");
            System.out.println("0. Salir");
            System.out.print("Elija una opción: ");
            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1: showProductMenu(); break;
                case 2: showPersonMenu(); break;
                case 3: showSaleMenu(); break;
                case 4: showAccessoryMenu(); break;
                case 5: showPromotionMenu(); break;
                case 6: showReturnMenu(); break;
                case 7: showWarrantyMenu(); break;
                case 0: System.out.println("Cerrando GameZone..."); break;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    public void showProductMenu() {
        System.out.println("\n--- Productos ---");
        System.out.println("1. Registrar videojuego");
        System.out.println("2. Registrar consola");
        System.out.println("3. Listar todos los productos");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Título: ");
            String title = scanner.nextLine();
            System.out.print("Precio: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Cantidad disponible: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Plataforma: ");
            String platform = scanner.nextLine();
            System.out.print("Género: ");
            String genre = scanner.nextLine();
            System.out.print("Clasificación por edad: ");
            String ageRating = scanner.nextLine();

            VideoGame game = new VideoGame(id, title, price, quantity, platform, genre, ageRating);
            productService.registerVideoGame(game);
            System.out.println("Videojuego registrado.");

        } else if (option == 2) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Titulo: ");
            String title = scanner.nextLine();
            System.out.print("Precio: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Cantidad disponible: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Marca: ");
            String brand = scanner.nextLine();
            System.out.print("Modelo: ");
            String model = scanner.nextLine();
            System.out.print("Generación: ");
            String generation = scanner.nextLine();

            Console console = new Console(id, title, price, quantity, brand, model, generation);
            productService.registerConsole(console);
            // La consola recién registrada debe poder referenciarse desde los
            // accesorios, así que se refresca la lista de consolas conocidas.
            accessoryService.setAvailableConsoles(listConsoles());
            System.out.println("Consola registrada.");

        } else if (option == 3) {
            List<Product> products = productService.listAllProducts();
            for (Product p : products) {
                System.out.println(p.getDescription());
            }
        }
    }

    public void showPersonMenu() {
        System.out.println("\n--- Personas ---");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Nombre: ");
            String name = scanner.nextLine();
            System.out.print("Identificacion: ");
            String identification = scanner.nextLine();
            System.out.print("Telefono: ");
            String phone = scanner.nextLine();
            System.out.print("Correo electrónico: ");
            String email = scanner.nextLine();

            Customer customer = new Customer(id, name, identification, phone, email);
            personService.registerCustomer(customer);
            System.out.println("Cliente registrado.");

        } else if (option == 2) {
            for (Customer c : personService.listCustomers()) {
                System.out.println(c.getFullSummary());
            }
        } else if (option == 3) {
            for (Seller s : personService.listSellers()) {
                System.out.println(s.getFullSummary());
            }
        }
    }

    public void showSaleMenu() {
        System.out.println("\n--- Ventas ---");
        System.out.println("1. Registrar venta");
        System.out.println("2. Listar todas las ventas");
        System.out.println("3. Listar ventas por cliente");
        System.out.println("4. Listar ventas por vendedor");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("ID de la venta: ");
            String id = scanner.nextLine();
            System.out.print("ID del cliente: ");
            String customerId = scanner.nextLine();
            System.out.print("ID del vendedor: ");
            String sellerId = scanner.nextLine();

            // CAMBIO 1: usamos los métodos que ya existen en PersonService,
            // en vez de reimplementar la búsqueda aquí en la UI.
            Customer customer = personService.findCustomerById(customerId);
            Seller seller = personService.findSellerById(sellerId);

            // CAMBIO 2: validamos que existan antes de seguir, para no
            // construir una venta con datos nulos.
            if (customer == null) {
                System.out.println("Error: no se encontró ningún cliente con ese ID.");
                return;
            }
            if (seller == null) {
                System.out.println("Error: no se encontró ningún vendedor con ese ID.");
                return;
            }

            List<Product> products = new ArrayList<>();
            List<String> productIdsWithExtendedWarranty = new ArrayList<>();
            String more = "s";
            while (more.equalsIgnoreCase("s")) {
                // CAMBIO 3: ahora el ID puede corresponder tanto a un Product
                // (VideoGame/Console) como a un Accessory (Controller/Cable/Memory).
                System.out.print("ID del producto o accesorio: ");
                String itemId = scanner.nextLine();
                Product product = findItemById(itemId);
                if (product != null) {
                    products.add(product);
                    // Taller 4: solo las consolas tienen garantía, y la básica
                    // viene incluida. Se pregunta únicamente por la extendida.
                    if (product instanceof Console && wantsExtendedWarranty(product)) {
                        productIdsWithExtendedWarranty.add(product.getId());
                    }
                } else {
                    System.out.println("Aviso: no se encontró ningún producto o accesorio con ese ID, se omite.");
                }
                System.out.print("¿Agregar otro producto? (s/n): ");
                more = scanner.nextLine();
            }

            Sale sale = new Sale(id, LocalDate.now(), customer, seller, products);
            saleService.registerSale(sale, productIdsWithExtendedWarranty);
            System.out.println("Venta registrada. Total: " + sale.getTotal());
            // El recibo muestra el subtotal, el descuento aplicado (con el
            // nombre de la promoción) y el total final.
            System.out.println(sale.generateReceipt());

        } else if (option == 2) {
            for (Sale s : saleService.listAllSales()) {
                System.out.println("Venta " + s.getId() + " - Total: " + s.getTotal());
            }
        } else if (option == 3) {
            System.out.print("ID del cliente: ");
            String customerId = scanner.nextLine();
            for (Sale s : saleService.getSalesByCustomer(customerId)) {
                System.out.println("Venta " + s.getId() + " - Total: " + s.getTotal());
            }
        } else if (option == 4) {
            System.out.print("ID del vendedor: ");
            String sellerId = scanner.nextLine();
            for (Sale s : saleService.getSalesBySeller(sellerId)) {
                System.out.println("Venta " + s.getId() + " - Total: " + s.getTotal());
            }
        }
    }

    /**
     * Shows the accessory management submenu: register controllers, cables
     * and memories, list all accessories, filter by type, and query which
     * accessories are compatible with a given console.
     */
    public void showAccessoryMenu() {
        System.out.println("\n--- Gestión de accesorios ---");
        System.out.println("1. Registrar un nuevo control.");
        System.out.println("2. Registrar un nuevo cable.");
        System.out.println("3. Registrar una nueva memoria.");
        System.out.println("4. Listar todos los accesorios.");
        System.out.println("5. Listar accesorios por tipo.");
        System.out.println("6. Consultar accesorios compatibles con una consola.");
        System.out.println("0. Volver al menú principal.");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Título: ");
                String title = scanner.nextLine();
                System.out.print("Precio: ");
                double price = Double.parseDouble(scanner.nextLine());
                System.out.print("Cantidad disponible: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                System.out.print("Tipo de conexión: ");
                String connectionType = scanner.nextLine();

                Controller controller = new Controller(id, title, price, quantity, connectionType);
                controller.setCompatibleConsoles(askCompatibleConsoles());
                accessoryService.registerController(controller);
                System.out.println("Control registrado.");
                break;
            }
            case 2: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Título: ");
                String title = scanner.nextLine();
                System.out.print("Precio: ");
                double price = Double.parseDouble(scanner.nextLine());
                System.out.print("Cantidad disponible: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                System.out.print("Longitud (metros): ");
                double lengthMeters = Double.parseDouble(scanner.nextLine());
                System.out.print("Tipo de conector: ");
                String connectorType = scanner.nextLine();

                Cable cable = new Cable(id, title, price, quantity, lengthMeters, connectorType);
                cable.setCompatibleConsoles(askCompatibleConsoles());
                accessoryService.registerCable(cable);
                System.out.println("Cable registrado.");
                break;
            }
            case 3: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Título: ");
                String title = scanner.nextLine();
                System.out.print("Precio: ");
                double price = Double.parseDouble(scanner.nextLine());
                System.out.print("Cantidad disponible: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                System.out.print("Capacidad (GB): ");
                int capacityGb = Integer.parseInt(scanner.nextLine());
                System.out.print("Tipo de memoria: ");
                String memoryType = scanner.nextLine();

                Memory memory = new Memory(id, title, price, quantity, capacityGb, memoryType);
                memory.setCompatibleConsoles(askCompatibleConsoles());
                accessoryService.registerMemory(memory);
                System.out.println("Memoria registrada.");
                break;
            }
            case 4: {
                for (Accessory accessory : accessoryService.listAllAccessories()) {
                    System.out.println(accessory.getDescription());
                }
                break;
            }
            case 5: {
                System.out.print("Tipo (CONTROLLER/CABLE/MEMORY): ");
                String type = scanner.nextLine();
                for (Accessory accessory : accessoryService.listAccessoriesByType(type)) {
                    System.out.println(accessory.getDescription());
                }
                break;
            }
            case 6: {
                System.out.print("ID de la consola: ");
                String consoleId = scanner.nextLine();
                for (Accessory accessory : accessoryService.findAccessoriesCompatibleWith(consoleId)) {
                    System.out.println(accessory.getDescription());
                }
                break;
            }
            case 0:
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    /**
     * Shows the promotion management submenu: register promotions by
     * percentage, by category and by bulk purchase, list all promotions and
     * list only the promotions that are currently in force.
     */
    public void showPromotionMenu() {
        System.out.println("\n--- Gestión de promociones ---");
        System.out.println("1. Registrar promoción por porcentaje.");
        System.out.println("2. Registrar promoción por categoría.");
        System.out.println("3. Registrar promoción por volumen.");
        System.out.println("4. Listar todas las promociones.");
        System.out.println("5. Listar promociones vigentes.");
        System.out.println("0. Volver al menú principal.");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Nombre: ");
                String name = scanner.nextLine();
                Period period = askPromotionPeriod();
                if (period == null) {
                    return;
                }
                double percentage = askPercentage("Porcentaje de descuento: ");

                promotionService.registerPercentageDiscount(
                        id, name, period.getStartDate(), period.getEndDate(), percentage);
                System.out.println("Promoción por porcentaje registrada.");
                break;
            }
            case 2: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Nombre: ");
                String name = scanner.nextLine();
                Period period = askPromotionPeriod();
                if (period == null) {
                    return;
                }
                double percentage = askPercentage("Porcentaje de descuento: ");
                String category = askCategory();

                promotionService.registerCategoryDiscount(
                        id, name, period.getStartDate(), period.getEndDate(), percentage, category);
                System.out.println("Promoción por categoría registrada.");
                break;
            }
            case 3: {
                System.out.print("ID: ");
                String id = scanner.nextLine();
                System.out.print("Nombre: ");
                String name = scanner.nextLine();
                Period period = askPromotionPeriod();
                if (period == null) {
                    return;
                }
                int minimumQuantity = askPositiveInt("Cantidad mínima de productos: ");
                double percentage = askPercentage("Porcentaje de descuento: ");

                promotionService.registerBulkPurchaseDiscount(
                        id, name, period.getStartDate(), period.getEndDate(), minimumQuantity, percentage);
                System.out.println("Promoción por volumen registrada.");
                break;
            }
            case 4:
                showPromotions(promotionService.listAllPromotions());
                break;
            case 5:
                showPromotions(promotionService.listActivePromotions());
                break;
            case 0:
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    /**
     * Shows the return management submenu: register a new return, list every
     * registered return and query the returns by customer or by original sale.
     */
    public void showReturnMenu() {
        System.out.println("\n--- Gestión de devoluciones ---");
        System.out.println("1. Registrar nueva devolución.");
        System.out.println("2. Consultar todas las devoluciones.");
        System.out.println("3. Consultar devoluciones por cliente.");
        System.out.println("4. Consultar devoluciones por venta.");
        System.out.println("5. Consultar balance mensual.");
        System.out.println("0. Volver al menú principal.");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1:
                registerReturnFlow();
                break;
            case 2:
                showReturns(returnService.viewAllReturns());
                break;
            case 3: {
                System.out.print("ID del cliente: ");
                String customerId = scanner.nextLine();
                showReturns(returnService.viewReturnsByCustomer(customerId));
                break;
            }
            case 4: {
                System.out.print("ID de la venta: ");
                String saleId = scanner.nextLine();
                showReturns(returnService.viewReturnsBySale(saleId));
                break;
            }
            case 5:
                showMonthlyBalance();
                break;
            case 0:
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    /**
     * Asks for the month and the year of the requested balance and prints the
     * three figures the module offers: the sales of the period, the returns of
     * the period and the resulting net balance.
     */
    private void showMonthlyBalance() {
        int month = askMonth();
        int year = askYear();

        double sales = returnService.calculateMonthlySales(month, year);
        double returns = returnService.calculateMonthlyReturns(month, year);
        double balance = returnService.generateMonthlyBalance(month, year);

        System.out.printf("Balance de devoluciones de %02d/%d:%n", month, year);
        System.out.printf("  Total de ventas:      $%.2f%n", sales);
        System.out.printf("  Total de devoluciones: $%.2f%n", returns);
        System.out.printf("  Balance neto:         $%.2f%n", balance);
    }

    /**
     * Asks for a month number until a value between 1 and 12 is entered.
     *
     * @return the month entered by the user
     */
    private int askMonth() {
        while (true) {
            System.out.print("Mes (1-12): ");
            try {
                int month = Integer.parseInt(scanner.nextLine().trim());
                if (month >= 1 && month <= 12) {
                    return month;
                }
            } catch (NumberFormatException e) {
                // Se cae al mensaje de error de abajo.
            }
            System.out.println("Error: el mes debe ser un número entre 1 y 12.");
        }
    }

    /**
     * Asks for a year until a plausible value is entered.
     *
     * @return the year entered by the user
     */
    private int askYear() {
        while (true) {
            System.out.print("Año: ");
            try {
                int year = Integer.parseInt(scanner.nextLine().trim());
                if (year >= 2000 && year <= 2100) {
                    return year;
                }
            } catch (NumberFormatException e) {
                // Se cae al mensaje de error de abajo.
            }
            System.out.println("Error: el año debe estar entre 2000 y 2100.");
        }
    }

    /**
     * Guides the user through the registration of a return: asks for the
     * original sale, lets the user pick the products to give back, asks for the
     * reason and finally prints the receipt produced by the model.
     */
    private void registerReturnFlow() {
        System.out.print("ID de la venta a devolver: ");
        String saleId = scanner.nextLine().trim();

        Sale sale = findSaleById(saleId);
        if (sale == null) {
            System.out.println("Error: no se encontró ninguna venta con el ID " + saleId + ".");
            return;
        }
        if (!sale.canBeReturned()) {
            System.out.println("Error: la venta " + saleId + " supera los 30 días y no admite devoluciones.");
            return;
        }

        System.out.println("Productos de la venta " + saleId + ":");
        for (Product product : sale.getProducts()) {
            System.out.println("  - " + product.getId() + " | " + product.getDescription()
                    + " | $" + product.getPrice());
        }

        List<String> productIds = askReturnedProductIds(sale);
        if (productIds.isEmpty()) {
            System.out.println("Devolución cancelada: no se seleccionó ningún producto.");
            return;
        }

        System.out.print("Motivo de la devolución: ");
        String reason = scanner.nextLine().trim();

        Return registeredReturn;
        try {
            registeredReturn = returnService.registerReturn(saleId, productIds, reason);
        } catch (IllegalArgumentException e) {
            // El servicio comunica los rechazos lanzando IllegalArgumentException
            // y ese mensaje ya viene en espanol, asi que se muestra tal cual en
            // vez de dejar que la excepcion salga del menu y tumbe la app de
            // consola. Las validaciones de arriba ya evitan los rechazos
            // conocidos; esta red de seguridad cubre los que se agreguen.
            System.out.println("No se pudo registrar la devolución: " + e.getMessage());
            return;
        }

        if (registeredReturn == null) {
            System.out.println("Error: no fue posible registrar la devolución.");
            return;
        }
        System.out.println(registeredReturn.generateReturnReceipt());
    }

    /**
     * Asks for the ids of the products to be returned, accepting only ids that
     * belong to the given sale. Unknown ids are reported and discarded, and
     * repeated ids are ignored.
     *
     * @param sale the original sale of the return
     * @return the ids of the products to return (empty if the input is empty)
     */
    private List<String> askReturnedProductIds(Sale sale) {
        List<String> saleProductIds = new ArrayList<>();
        for (Product product : sale.getProducts()) {
            if (product != null && product.getId() != null && !saleProductIds.contains(product.getId())) {
                saleProductIds.add(product.getId());
            }
        }

        System.out.print("IDs de los productos a devolver (separados por coma): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return List.of();
        }

        List<String> selectedIds = new ArrayList<>();
        for (String rawId : input.split(",")) {
            String productId = rawId.trim();
            if (!saleProductIds.contains(productId)) {
                System.out.println("Aviso: el producto " + productId + " no pertenece a la venta, se omite.");
                continue;
            }
            if (!selectedIds.contains(productId)) {
                selectedIds.add(productId);
            }
        }
        return selectedIds;
    }

    /**
     * Prints a list of returns, or a message when there is nothing to show.
     *
     * @param returns the returns to print
     */
    private void showReturns(List<Return> returns) {
        if (returns == null || returns.isEmpty()) {
            System.out.println("No hay devoluciones registradas.");
            return;
        }
        for (Return productReturn : returns) {
            System.out.println(describeReturn(productReturn));
        }
    }

    /**
     * Builds the one-line description of a return, including the links with the
     * original sale and with the customer of that sale.
     *
     * @param productReturn the return to describe
     * @return the description of the return
     */
    private String describeReturn(Return productReturn) {
        Sale originalSale = productReturn.getOriginalSale();
        String saleId = originalSale != null ? originalSale.getId() : "N/A";
        String customerId = originalSale != null && originalSale.getCustomer() != null
                ? originalSale.getCustomer().getId() : "N/A";
        return String.format(
                "Devolución | ID: %s | Fecha: %s | Venta: %s | Cliente: %s | Motivo: %s | Reembolso: $%.2f",
                productReturn.getId(),
                productReturn.getDate(),
                saleId,
                customerId,
                productReturn.getReason(),
                productReturn.getRefundAmount());
    }

    /**
     * Searches a registered sale by its id through the sale service.
     *
     * @param saleId the id of the sale to look for
     * @return the sale, or null when no sale matches the id
     */
    private Sale findSaleById(String saleId) {
        for (Sale sale : saleService.listAllSales()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    /**
     * Prints a list of promotions, or a message when the list is empty.
     *
     * @param promotions the promotions to print
     */
    private void showPromotions(List<Promotion> promotions) {
        if (promotions.isEmpty()) {
            System.out.println("No hay promociones registradas.");
            return;
        }
        for (Promotion promotion : promotions) {
            System.out.println(describePromotion(promotion));
        }
    }

    /**
     * Builds the one-line description of a promotion using the common data
     * shared by every promotion type.
     *
     * @param promotion the promotion to describe
     * @return the description of the promotion
     */
    private String describePromotion(Promotion promotion) {
        return String.format("Promoción | ID: %s | Nombre: %s | Vigencia: %s a %s | ¿Vigente hoy?: %s",
                promotion.getId(),
                promotion.getName(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive(LocalDate.now()) ? "Sí" : "No");
    }

    /**
     * Asks for the validity period of a promotion. Dates are expected in
     * ISO format (yyyy-MM-dd). If the end date is earlier than the start
     * date, the registration is cancelled.
     *
     * @return the period, or null when the period is not valid
     */
    private Period askPromotionPeriod() {
        LocalDate startDate = askDate("Fecha de inicio (yyyy-MM-dd): ");
        LocalDate endDate = askDate("Fecha de fin (yyyy-MM-dd): ");
        if (endDate.isBefore(startDate)) {
            System.out.println("Error: la fecha de fin no puede ser anterior a la fecha de inicio.");
            return null;
        }
        return new Period(startDate, endDate);
    }

    /**
     * Asks for a date until a valid ISO date (yyyy-MM-dd) is entered.
     *
     * @param prompt the message shown to the user
     * @return the date entered by the user
     */
    private LocalDate askDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Error: fecha inválida. Use el formato yyyy-MM-dd.");
            }
        }
    }

    /**
     * Asks for a discount percentage until a value greater than zero and not
     * greater than 100 is entered.
     *
     * @param prompt the message shown to the user
     * @return the percentage entered by the user
     */
    private double askPercentage(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double percentage = Double.parseDouble(scanner.nextLine().trim());
                if (percentage > 0 && percentage <= 100) {
                    return percentage;
                }
            } catch (NumberFormatException e) {
                // Se cae al mensaje de error de abajo.
            }
            System.out.println("Error: el porcentaje debe ser un número mayor que 0 y menor o igual que 100.");
        }
    }

    /**
     * Asks for a positive integer until a valid value is entered.
     *
     * @param prompt the message shown to the user
     * @return the number entered by the user
     */
    private int askPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Se cae al mensaje de error de abajo.
            }
            System.out.println("Error: debe ingresar un número entero mayor que 0.");
        }
    }

    /**
     * Asks for the category targeted by a promotion, accepting only the
     * categories handled by the promotion module.
     *
     * @return the category in upper case (VIDEOGAME or CONSOLE)
     */
    private String askCategory() {
        while (true) {
            System.out.print("Categoría objetivo (VIDEOGAME/CONSOLE): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals(CATEGORY_VIDEOGAME) || input.equals(CATEGORY_CONSOLE)) {
                return input;
            }
            System.out.println("Error: la categoría debe ser VIDEOGAME o CONSOLE.");
        }
    }

    /**
     * Simple holder for the validity period of a promotion.
     */
    private static class Period {

        private final LocalDate startDate;
        private final LocalDate endDate;

        private Period(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private LocalDate getStartDate() {
            return startDate;
        }

        private LocalDate getEndDate() {
            return endDate;
        }
    }


    private List<Console> listConsoles() {
        List<Console> consoles = new ArrayList<>();
        for (Product p : productService.listAllProducts()) {
            if (p instanceof Console console) {
                consoles.add(console);
            }
        }
        return consoles;
    }


    private List<Console> askCompatibleConsoles() {
        List<Console> consoles = new ArrayList<>();
        System.out.print("IDs de consolas compatibles (separados por coma, o vacío si ninguna): ");
        String input = scanner.nextLine();
        if (!input.isBlank()) {
            String[] ids = input.split(",");
            for (String rawId : ids) {
                String consoleId = rawId.trim();
                Product product = findProductById(consoleId);
                if (product instanceof Console consoleProduct) {
                    consoles.add(consoleProduct);
                } else {
                    System.out.println("Aviso: no se encontró una consola con ID " + consoleId + ", se omite.");
                }
            }
        }
        return consoles;
    }


    /**
     * Shows the warranty management submenu: look up the warranty of a product
     * in a given sale, list every warranty, list the ones currently in force
     * and list the ones about to expire.
     */
    public void showWarrantyMenu() {
        System.out.println("\n--- Gestión de garantías ---");
        System.out.println("1. Consultar garantía de un producto en una venta.");
        System.out.println("2. Listar todas las garantías.");
        System.out.println("3. Listar garantías vigentes.");
        System.out.println("4. Listar garantías próximas a vencer.");
        System.out.println("0. Volver al menú principal.");
        System.out.print("Elija una opción: ");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1: {
                System.out.print("ID del producto: ");
                String productId = scanner.nextLine();
                System.out.print("ID de la venta: ");
                String saleId = scanner.nextLine();
                Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
                if (warranty == null) {
                    System.out.println("No hay garantía registrada para el producto " + productId
                            + " en la venta " + saleId + ".");
                } else {
                    System.out.println(warranty.generateWarrantyCertificate());
                }
                break;
            }
            case 2:
                showWarranties(warrantyService.listAllWarranties());
                break;
            case 3:
                showWarranties(warrantyService.listActiveWarranties());
                break;
            case 4: {
                int days = askPositiveInt("¿En cuántos días debe vencer una garantía para considerarse próxima? ");
                showWarranties(warrantyService.listWarrantiesExpiringSoon(days));
                break;
            }
            case 0:
                System.out.println("Volviendo al menú principal.");
                break;
            default:
                System.out.println("Opción inválida.");
        }
    }

    private void showWarranties(List<Warranty> warranties) {
        if (warranties.isEmpty()) {
            System.out.println("No hay garantías registradas.");
            return;
        }
        for (Warranty warranty : warranties) {
            System.out.println(describeWarranty(warranty));
        }
    }

    private String describeWarranty(Warranty warranty) {
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), warranty.getEndDate());
        String remaining = daysLeft < 0
                ? "vencida hace " + Math.abs(daysLeft) + " días"
                : "quedan " + daysLeft + " días";
        return warranty.getId() + " | " + warranty.getWarrantyType()
                + " | Producto: " + warranty.getProduct().getId()
                + " | Venta: " + warranty.getSale().getId()
                + " | Vence: " + warranty.getEndDate()
                + " (" + remaining + ")";
    }

    /**
     * Asks the customer whether the extended warranty is wanted for a console.
 * The basic warranty is included with every console, so this question is only
 * about paying to extend the coverage from six to twelve months.
 *
 * @param product the console that was just added to the sale
 * @return true when the customer accepts the extended warranty
 */
private boolean wantsExtendedWarranty(Product product) {
    double extraCost = product.getPrice() * 0.10;
    System.out.println("  La consola " + product.getId() + " (" + product.getTitle()
            + ") incluye garantía básica de 6 meses.");
    System.out.println("  ¿Desea comprar la garantía extendida de 12 meses por $"
            + String.format("%.2f", extraCost) + "? (s/n): ");
    String answer = scanner.nextLine();
    return "s".equalsIgnoreCase(answer.trim());
}

private Product findProductById(String id) {
        for (Product p : productService.listAllProducts()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }


    private Product findItemById(String id) {
        Product product = findProductById(id);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(id);
    }
}