package com.gamezone.persistence;

import com.gamezone.model.Product;
import com.gamezone.model.Return;
import com.gamezone.model.Sale;
import com.gamezone.service.AccessoryService;
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
    private static final char ESCAPE_CHARACTER = '\\';

    private final SaleService saleService;
    private final ProductService productService;
    private final AccessoryService accessoryService;

    /**
     * Creates the repository.
     *
     * @param saleService      used to resolve the original Sale referenced by
     *                         each stored return
     * @param productService   used to resolve the returned Product objects
     *                         referenced by each stored return
     * @param accessoryService used to resolve the returned accessories, which
     *                         are not part of the product list
     */
    public ReturnRepository(SaleService saleService, ProductService productService,
                            AccessoryService accessoryService) {
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
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
                escape(theReturn.getId()),
                escape(theReturn.getDate().toString()),
                escape(theReturn.getOriginalSale().getId()),
                escape(theReturn.getReason()),
                String.valueOf(theReturn.getRefundAmount()),
                productIds.toString());
    }

    /**
     * Escapes the characters that would otherwise break the line format, so a
     * reason such as "se rompio; venia mojado" is stored as one single field
     * instead of splitting the record in two.
     *
     * @param value the raw text of a field
     * @return the text safe to write between separators
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == ESCAPE_CHARACTER || character == FIELD_SEPARATOR.charAt(0)) {
                result.append(ESCAPE_CHARACTER);
            }
            result.append(character);
        }
        return result.toString();
    }

    /**
     * Splits a stored line into its fields, ignoring the separators that were
     * escaped on write, and unescaping the rest of the text on the way.
     *
     * @param line the raw line read from the file
     * @return the fields of the line, in order
     */
    private List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                current.append(character);
                escaped = false;
            } else if (character == ESCAPE_CHARACTER) {
                escaped = true;
            } else if (character == FIELD_SEPARATOR.charAt(0)) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private Return parseLine(String line) {
        List<String> fields = splitFields(line);
        if (fields.size() < 6) {
            System.out.println("Malformed return line in file: " + line);
            return null;
        }

        String id = fields.get(0);
        LocalDate date;
        try {
            date = LocalDate.parse(fields.get(1));
        } catch (RuntimeException e) {
            System.out.println("Malformed return line in file: " + line);
            return null;
        }
        String saleId = fields.get(2);
        String reason = fields.get(3);
        String productIdsField = fields.get(5);

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
                } else {
                    // Avisar en vez de omitir en silencio: si el producto ya no
                    // esta, el reembolso de esta devolucion quedaria incompleto
                    // y nadie se enteraria de por que.
                    System.out.println("Return " + id + " references unknown product " + productId
                            + ", it is left out of the refund.");
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
        // Los accesorios no viven en la lista de productos, asi que sin esta
        // segunda busqueda una devolucion de un accesorio se recargaria sin
        // productos y con reembolso cero.
        return accessoryService.findById(productId);
    }

    private void ensureDataFolderExists() {
        File folder = new File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}