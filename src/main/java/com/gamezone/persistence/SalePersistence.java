package com.gamezone.persistence;

import com.gamezone.model.Sale;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SalePersistence {

    private static final String FILE_PATH = "data/sales.dat";

    public void save(List<Sale> sales) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH));
            out.writeObject(sales);
            out.close();
        } catch (IOException e) {
            System.out.println("Error saving sales: " + e.getMessage());
        }
    }

    public List<Sale> loadAll() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            List<Sale> sales = (List<Sale>) in.readObject();
            in.close();
            return sales;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading sales: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}