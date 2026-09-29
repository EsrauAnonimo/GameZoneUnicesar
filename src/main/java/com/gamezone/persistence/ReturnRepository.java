package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Return data to and from a CSV file. This class
 * belongs to the persistence layer and is the ONLY class in the return
 * module allowed to touch the file system. Since a Return references a
 * Sale and a list of Product objects, this repository receives SaleService
 * and ProductService by constructor to resolve those references when
 * loading a return back from disk.
 */
public class ReturnRepository {

    private static final String RETURNS_FILE = "data/returns.csv";
    private static final String FIELD_SEPARATOR = ";";
    private static final String PRODUCT_ID_SEPARATOR = "\\|";
    private static final String PRODUCT_ID_JOINER = "|";

    private final SaleService saleService;
    private final ProductService productService;

    /**
     * Creates the repository.
     *
     * @param saleService    used to resolve the original Sale referenced by
     *                       each stored return
     * @param productService used to resolve the returned Product objects
     *                       referenced by each stored return
     */
    public ReturnRepository(SaleService saleService, ProductService productService) {
        this.saleService = saleService;
        this.productService = productService;
    }

    /**
     * Saves the given list of returns to the returns file, overwriting any
     * previous content.
     *
     * @param returns list of returns to persist
     */
    public void saveAll(List<Return> returns) {
        ensureDataFolderExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RETURNS_FILE))) {
            for (Return theReturn : returns) {
                writer.write(buildLine(theReturn));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving returns: " + e.getMessage());
        }
    }

    /**
     * Loads all returns stored in the returns file, resolving each one's
     * original sale and returned products against the current sales and
     * products known by the system.
     *
     * @return list of returns found in the file (empty list if the file
     *         does not exist yet, e.g. on the very first execution)
     */
    public List<Return> loadAll() {
        List<Return> returns = new ArrayList<>();
        File file = new File(RETURNS_FILE);
        if (!file.exists()) {
            return returns;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Return theReturn = parseLine(line);
                if (theReturn != null) {
                    returns.add(theReturn);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading returns: " + e.getMessage());
        }
        return returns;
    }

    private String buildLine(Return theReturn) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = theReturn.getReturnedProducts();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) {
                productIds.append(PRODUCT_ID_JOINER);
            }
            productIds.append(products.get(i).getId());
        }

        return String.join(FIELD_SEPARATOR,
                theReturn.getId(),
                theReturn.getDate().toString(),
                theReturn.getOriginalSale().getId(),
                theReturn.getReason(),
                String.valueOf(theReturn.getRefundAmount()),
                productIds.toString());
    }

    private Return parseLine(String line) {
        String[] parts = line.split(FIELD_SEPARATOR, -1);
        if (parts.length < 6) {
            System.out.println("Malformed return line in file: " + line);
            return null;
        }

        String id = parts[0];
        LocalDate date;
        try {
            date = LocalDate.parse(parts[1]);
        } catch (RuntimeException e) {
            System.out.println("Malformed return line in file: " + line);
            return null;
        }
        String saleId = parts[2];
        String reason = parts[3];
        String productIdsField = parts[5];

        Sale originalSale = findSaleById(saleId);
        if (originalSale == null) {
            System.out.println("Return " + id + " references unknown sale " + saleId + ", skipped.");
            return null;
        }

        Return theReturn = new Return(id, date, originalSale, reason);
        if (!productIdsField.isBlank()) {
            for (String productId : productIdsField.split(PRODUCT_ID_SEPARATOR)) {
                Product product = findProductById(productId);
                if (product != null) {
                    theReturn.addReturnedProduct(product);
                }
            }
        }
        theReturn.calculateRefundAmount();
        return theReturn;
    }

    private Sale findSaleById(String saleId) {
        for (Sale sale : saleService.listAllSales()) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }

    private Product findProductById(String productId) {
        for (Product product : productService.listAllProducts()) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private void ensureDataFolderExists() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}