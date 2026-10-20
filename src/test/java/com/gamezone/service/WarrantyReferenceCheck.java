package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manual verification of the A2 adjustment: warranties are stored as ids only
 * and WarrantyService resolves the sale and product references, without
 * WarrantyRepository depending on SaleService.
 * <p>
 * The project has no test framework, so this class has a main method and
 * prints PASS or FAIL for each check. It uses a temporary CSV file and
 * stubbed sale and product sources, so it never touches the real data files.
 * <pre>
 * java -cp target\classes;target\test-classes com.gamezone.service.WarrantyReferenceCheck
 * </pre>
 */
public class WarrantyReferenceCheck {

    private static int failures = 0;

    /**
     * Runs every check and exits with code 1 if any of them fails.
     *
     * @param args not used
     * @throws IOException if the temporary file cannot be handled
     */
    public static void main(String[] args) throws IOException {
        Path tempFile = Files.createTempFile("warranties", ".csv");
        try {
            runChecks(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures > 0) {
            System.exit(1);
        }
    }

    private static void runChecks(Path tempFile) {
        LocalDate today = LocalDate.now();
        Product game = newProduct("P1", 100.0);
        Product console = newProduct("P2", 100.0);
        List<Product> items = new ArrayList<>();
        items.add(game);
        items.add(console);
        Sale sale = new Sale("S1", today, null, null, items);

        // The repository is built without any service: it only needs a file.
        WarrantyRepository repository = new WarrantyRepository(tempFile.toString());

        SalePersistence salePersistence = new SalePersistence() {
            @Override
            public List<Sale> loadAll() {
                List<Sale> sales = new ArrayList<>();
                sales.add(sale);
                return sales;
            }
        };
        ProductService productService = new ProductService() {
            @Override
            public List<Product> listAllProducts() {
                return new ArrayList<>();
            }
        };
        WarrantyService service = new WarrantyService(repository, salePersistence, productService);

        BasicWarranty basic = service.assignBasicWarranty(game, sale, today);
        ExtendedWarranty extended = service.assignExtendedWarranty(console, sale, today);
        check("first warranty id is WAR-001", "WAR-001".equals(basic.getId()));
        check("second warranty id is WAR-002", "WAR-002".equals(extended.getId()));

        List<WarrantyRecord> records = repository.loadAll();
        check("repository stores two records", records.size() == 2);
        check("record keeps only ids",
                "P1".equals(records.get(0).getProductId()) && "S1".equals(records.get(0).getSaleId()));

        Warranty foundBasic = service.findWarrantyByProduct("P1", "S1");
        check("basic warranty is resolved from its ids",
                foundBasic instanceof BasicWarranty && foundBasic.getProduct() == game);
        Warranty foundExtended = service.findWarrantyByProduct("P2", "S1");
        check("extended warranty is resolved from its ids",
                foundExtended instanceof ExtendedWarranty && foundExtended.getSale() == sale);
        check("extended warranty costs 10% of the product price",
                foundExtended != null && Math.abs(foundExtended.getAdditionalCost() - 10.0) < 0.0001);

        // A record whose sale does not exist must be skipped, not break loading.
        records.add(new WarrantyRecord(WarrantyRepository.TYPE_BASIC, "WAR-003", "P1", "S9", today));
        repository.saveAll(records);
        check("record with a missing sale is not resolved",
                service.findWarrantyByProduct("P1", "S9") == null);
        check("listAllWarranties skips unresolved records", service.listAllWarranties().size() == 2);
        check("unresolved record is kept in the file", repository.loadAll().size() == 3);

        BasicWarranty next = service.assignBasicWarranty(game, sale, today);
        check("next id continues after the highest one", "WAR-004".equals(next.getId()));
        check("active warranties are listed", service.listActiveWarranties().size() == 3);
    }

    private static Product newProduct(String id, double price) {
        return new Product(id, "Item " + id, price, 5) {
            @Override
            public String getDescription() {
                return "Item " + id;
            }
        };
    }

    private static void check(String name, boolean condition) {
        System.out.println((condition ? "PASS  " : "FAIL  ") + name);
        if (!condition) {
            failures++;
        }
    }
}
