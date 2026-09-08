package com.gamezone.ui;

import com.gamezone.model.*;
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
    private Scanner scanner;

    public ConsoleUI(PersonService personService, ProductService productService, SaleService saleService) {
        this.personService = personService;
        this.productService = productService;
        this.saleService = saleService;
        this.scanner = new Scanner(System.in);
    }

    public void showMainMenu() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n=== GameZone Unicesar ===");
            System.out.println("1. Products menu");
            System.out.println("2. People menu");
            System.out.println("3. Sales menu");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1: showProductMenu(); break;
                case 2: showPersonMenu(); break;
                case 3: showSaleMenu(); break;
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
                System.out.print("Product ID: ");
                String productId = scanner.nextLine();
                Product product = findProductById(productId);
                if (product != null) {
                    products.add(product);
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

    // NOTA: findCustomerById y findSellerById se eliminaron de aquí —
    // ya no hacen falta porque PersonService los provee.

    private Product findProductById(String id) {
        for (Product p : productService.listAllProducts()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }
}