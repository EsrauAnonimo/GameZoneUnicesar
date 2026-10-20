package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Sale;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SalePersistence {

    private static final String FILE_PATH = "data/sales.dat";

    /**
     * Saves all sales to the file.
     *
     * @param sales the sales to save
     * @throws PersistenceException if the file cannot be written
     */
    public void save(List<Sale> sales) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(sales);
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el archivo " + FILE_PATH
                    + " (operacion: guardar ventas): " + e.getMessage());
        }
    }

    /**
     * Loads all sales from the file.
     *
     * @return the sales stored in the file, or an empty list if the file does
     *         not exist
     * @throws PersistenceException if the file exists but cannot be read
     */
    public List<Sale> loadAll() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Sale>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("No se pudo leer el archivo " + FILE_PATH
                    + " (operacion: cargar ventas): " + e.getMessage());
        }
    }
}
