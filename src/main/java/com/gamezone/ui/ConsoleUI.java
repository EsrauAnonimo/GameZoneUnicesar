package com.gamezone.ui;

import com.gamezone.model.*;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private PersonService personService;
    private ProductService productService;
    private SaleService saleService;
    private AccessoryService accessoryService;
    private Scanner scanner;

    public ConsoleUI(PersonService personService, ProductService productService, SaleService saleService,
                      AccessoryService accessoryService) {
        this.personService = personService;
        this.productService = productService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.scanner = new Scanner(System.in);
    }

    public void showMainMenu() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n=== GameZone Unicesar ===");
            System.out.println("1. Products menu");
            System.out.println("2. People menu");
            System.out.println("3. Sales menu");
            System.out.println("4. Accessories menu");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1: showProductMenu(); break;
                case 2: showPersonMenu(); break;
                case 3: showSaleMenu(); break;
                case 4: showAccessoryMenu(); break;
                case 0: System.out.println("Closing GameZone..."); break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    public void showProductMenu() {
        System.out.println("\n--- Products ---");
        System.out.println("1. Register video game");
        System.out.println("2. Register console");
        System.out.println("3. List all products");
        System.out.print("Choose an option: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Available quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Platform: ");
            String platform = scanner.nextLine();
            System.out.print("Genre: ");
            String genre = scanner.nextLine();
            System.out.print("Age rating: ");
            String ageRating = scanner.nextLine();

            VideoGame game = new VideoGame(id, title, price, quantity, platform, genre, ageRating);
            productService.registerVideoGame(game);
            System.out.println("Video game registered.");

        } else if (option == 2) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Price: ");
            double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Available quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Brand: ");
            String brand = scanner.nextLine();
            System.out.print("Model: ");
            String model = scanner.nextLine();
            System.out.print("Generation: ");
            String generation = scanner.nextLine();

            Console console = new Console(id, title, price, quantity, brand, model, generation);
            productService.registerConsole(console);
            System.out.println("Console registered.");

        } else if (option == 3) {
            List<Product> products = productService.listAllProducts();
            for (Product p : products) {
                System.out.println(p.getDescription());
            }
        }
    }

    public void showPersonMenu() {
        System.out.println("\n--- People ---");
        System.out.println("1. Register customer");
        System.out.println("2. List customers");
        System.out.println("3. List sellers");
        System.out.print("Choose an option: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("ID: ");
            String id = scanner.nextLine();
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Identification: ");
            String identification = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            Customer customer = new Customer(id, name, identification, phone, email);
            personService.registerCustomer(customer);
            System.out.println("Customer registered.");

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
        System.out.println("\n--- Sales ---");
        System.out.println("1. Register sale");
        System.out.println("2. List all sales");
        System.out.println("3. List sales by customer");
        System.out.println("4. List sales by seller");
        System.out.print("Choose an option: ");
        int option = Integer.parseInt(scanner.nextLine());

        if (option == 1) {
            System.out.print("Sale ID: ");
            String id = scanner.nextLine();
            System.out.print("Customer ID: ");
            String customerId = scanner.nextLine();
            System.out.print("Seller ID: ");
            String sellerId = scanner.nextLine();

            // CAMBIO 1: usamos los métodos que ya existen en PersonService,
            // en vez de reimplementar la búsqueda aquí en la UI.
            Customer customer = personService.findCustomerById(customerId);
            Seller seller = personService.findSellerById(sellerId);

            // CAMBIO 2: validamos que existan antes de seguir, para no
            // construir una venta con datos nulos.
            if (customer == null) {
                System.out.println("Error: no customer found with that ID.");
                return;
            }
            if (seller == null) {
                System.out.println("Error: no seller found with that ID.");
                return;
            }

            List<Product> products = new ArrayList<>();
            String more = "s";
            while (more.equalsIgnoreCase("s")) {
                // CAMBIO 3: ahora el ID puede corresponder tanto a un Product
                // (VideoGame/Console) como a un Accessory (Controller/Cable/Memory).
                System.out.print("Product/Accessory ID: ");
                String itemId = scanner.nextLine();
                Product product = findItemById(itemId);
                if (product != null) {
                    products.add(product);
                } else {
                    System.out.println("Warning: no product or accessory found with that ID, skipped.");
                }
                System.out.print("Add another product? (s/n): ");
                more = scanner.nextLine();
            }

            Sale sale = new Sale(id, LocalDate.now(), customer, seller, products);
            saleService.registerSale(sale);
            System.out.println("Sale registered. Total: " + sale.getTotal());

        } else if (option == 2) {
            for (Sale s : saleService.listAllSales()) {
                System.out.println("Sale " + s.getId() + " - Total: " + s.getTotal());
            }
        } else if (option == 3) {
            System.out.print("Customer ID: ");
            String customerId = scanner.nextLine();
            for (Sale s : saleService.getSalesByCustomer(customerId)) {
                System.out.println("Sale " + s.getId() + " - Total: " + s.getTotal());
            }
        } else if (option == 4) {
            System.out.print("Seller ID: ");
            String sellerId = scanner.nextLine();
            for (Sale s : saleService.getSalesBySeller(sellerId)) {
                System.out.println("Sale " + s.getId() + " - Total: " + s.getTotal());
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