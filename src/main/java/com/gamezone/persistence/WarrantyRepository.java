package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ProductService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists warranties in a CSV file. The first column is a type discriminator
 * (BASIC or EXTENDED) used to rebuild the right subclass when loading.
 * <pre>
 * type;id;productId;saleId;startDate
 * </pre>
 * The end date is not stored because every warranty calculates it from its
 * start date and duration. Sale and product references are resolved by id
 * while loading.
 */
public class WarrantyRepository {

    private static final String DEFAULT_PATH = "data/warranties.csv";
    private static final String SEPARATOR = ";";
    private static final String TYPE_BASIC = "BASIC";
    private static final String TYPE_EXTENDED = "EXTENDED";
    private static final int EXPECTED_FIELDS = 5;

    private final Path filePath;
    private final SalePersistence salePersistence;
    private final ProductService productService;
    private final AccessoryService accessoryService;

    /**
     * Creates a repository that uses the default file {@code data/warranties.csv}.
     *
     * @param salePersistence  persistence used to resolve sales by id
     * @param productService   service used to resolve products by id
     * @param accessoryService service used to resolve accessories by id
     */
    public WarrantyRepository(SalePersistence salePersistence, ProductService productService,
                              AccessoryService accessoryService) {
        this(DEFAULT_PATH, salePersistence, productService, accessoryService);
    }

    /**
     * Creates a repository backed by the given file.
     *
     * @param filePath         path of the CSV file
     * @param salePersistence  persistence used to resolve sales by id
     * @param productService   service used to resolve products by id
     * @param accessoryService service used to resolve accessories by id
     */
    public WarrantyRepository(String filePath, SalePersistence salePersistence,
                              ProductService productService, AccessoryService accessoryService) {
        this.filePath = Paths.get(filePath);
        this.salePersistence = salePersistence;
        this.productService = productService;
        this.accessoryService = accessoryService;
    }

    /**
     * Overwrites the CSV file with the given warranties.
     *
     * @param warranties the warranties to persist
     */
    public void saveAll(List<Warranty> warranties) {
        List<String> lines = new ArrayList<>();
        for (Warranty warranty : warranties) {
            lines.add(serialize(warranty));
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save warranties", e);
        }
    }

    /**
     * Loads every warranty from the CSV file, resolving the sale and the
     * product of each one. Lines that are malformed or whose sale or product
     * no longer exists are skipped.
     *
     * @return the warranties, or an empty list if the file does not exist
     */
    public List<Warranty> loadAll() {
        List<Warranty> warranties = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return warranties;
        }

        Map<String, Sale> salesById = new HashMap<>();
        for (Sale sale : salePersistence.loadAll()) {
            salesById.put(sale.getId(), sale);
        }

        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                Warranty warranty = deserialize(line, salesById);
                if (warranty != null) {
                    warranties.add(warranty);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load warranties", e);
        }
        return warranties;
    }

    private String serialize(Warranty warranty) {
        String type;
        if (warranty instanceof ExtendedWarranty) {
            type = TYPE_EXTENDED;
        } else if (warranty instanceof BasicWarranty) {
            type = TYPE_BASIC;
        } else {
            throw new IllegalArgumentException(
                    "Unknown warranty type: " + warranty.getClass().getSimpleName());
        }
        return String.join(SEPARATOR, type, warranty.getId(),
                warranty.getProduct().getId(), warranty.getSale().getId(),
                warranty.getStartDate().toString());
    }

    private Warranty deserialize(String line, Map<String, Sale> salesById) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != EXPECTED_FIELDS) {
            return null;
        }

        Sale sale = salesById.get(fields[3]);
        if (sale == null) {
            return null;
        }
        Product product = findProduct(sale, fields[2]);
        if (product == null) {
            return null;
        }

        LocalDate startDate;
        try {
            startDate = LocalDate.parse(fields[4]);
        } catch (DateTimeParseException e) {
            return null;
        }

        if (TYPE_BASIC.equals(fields[0])) {
            return new BasicWarranty(fields[1], product, sale, startDate);
        } else if (TYPE_EXTENDED.equals(fields[0])) {
            return new ExtendedWarranty(fields[1], product, sale, startDate);
        }
        return null;
    }

    /**
     * Looks for the product first among the items of the sale, then in the
     * general product inventory and finally among the accessories.
     */
    private Product findProduct(Sale sale, String productId) {
        if (productId == null) {
            return null;
        }
        if (sale != null && sale.getProducts() != null) {
            for (Product product : sale.getProducts()) {
                if (product != null && productId.equals(product.getId())) {
                    return product;
                }
            }
        }
        if (productService != null) {
            for (Product product : productService.listAllProducts()) {
                if (product != null && productId.equals(product.getId())) {
                    return product;
                }
            }
        }
        if (accessoryService != null) {
            for (Product accessory : accessoryService.listAllAccessories()) {
                if (accessory != null && productId.equals(accessory.getId())) {
                    return accessory;
                }
            }
        }
        return null;
    }
}
