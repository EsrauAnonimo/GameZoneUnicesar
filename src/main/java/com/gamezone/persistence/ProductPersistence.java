    package com.gamezone.persistence;

    import com.gamezone.model.Product;
    import java.io.*;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * Persistence class for Product objects.
     * Handles saving and loading products to/from a file inside the data/ folder.
     */
    public class ProductPersistence {


        private static final String FILE_NAME = "data/products.dat";


        public void save(List<Product> products) {
            // Asegurar que la carpeta data/ existe
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
                oos.writeObject(products);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Loads all products from the file.
         * 
         * @return A list of products loaded from the file
         */
        public List<Product> loadAll() {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<Product>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }