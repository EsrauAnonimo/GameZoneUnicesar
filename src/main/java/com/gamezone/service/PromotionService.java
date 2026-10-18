package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.persistence.PromotionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for promotion operations.
 * Handles the business logic for managing promotions and evaluating the best promotion for a sale.
 */
public class PromotionService {

    private final PromotionRepository repository;
    private final List<Promotion> promotions;

    /**
     * Constructs a PromotionService with the given repository.
     * Loads existing promotions from the repository on initialization.
     *
     * @param repository the repository used for promotion persistence
     */
    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
        this.promotions = new ArrayList<>();
        this.promotions.addAll(repository.loadAll());
    }

    /**
     * Registers a percentage discount promotion and persists it.
     *
     * @param id         promotion ID
     * @param name       promotion name
     * @param startDate  start date
     * @param endDate    end date
     * @param percentage discount percentage
     */
    public void registerPercentageDiscount(String id, String name, LocalDate startDate,
                                           LocalDate endDate, double percentage) {
        PercentageDiscount promotion = new PercentageDiscount(id, name, startDate, endDate, percentage);
        promotions.add(promotion);
        repository.saveAll(promotions);
    }

    /**
     * Registers a category discount promotion and persists it.
     *
     * @param id             promotion ID
     * @param name           promotion name
     * @param startDate      start date
     * @param endDate        end date
     * @param percentage     discount percentage
     * @param targetCategory category name (e.g. VIDEOGAME, CONSOLE)
     */
    public void registerCategoryDiscount(String id, String name, LocalDate startDate,
                                         LocalDate endDate, double percentage, String targetCategory) {
        CategoryDiscount promotion = new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
        promotions.add(promotion);
        repository.saveAll(promotions);
    }

    /**
     * Registers a bulk purchase discount promotion and persists it.
     *
     * @param id              promotion ID
     * @param name            promotion name
     * @param startDate       start date
     * @param endDate         end date
     * @param minimumQuantity minimum quantity required
     * @param percentage      discount percentage
     */
    public void registerBulkPurchaseDiscount(String id, String name, LocalDate startDate,
                                             LocalDate endDate, int minimumQuantity, double percentage) {
        BulkPurchaseDiscount promotion = new BulkPurchaseDiscount(id, name, startDate, endDate, minimumQuantity, percentage);
        promotions.add(promotion);
        repository.saveAll(promotions);
    }

    /**
     * Returns all registered promotions.
     *
     * @return list of all promotions
     */
    public List<Promotion> listAllPromotions() {
        return new ArrayList<>(promotions);
    }

    /**
     * Returns all promotions that are active today.
     *
     * @return list of active promotions
     */
    public List<Promotion> listActivePromotions() {
        return listActivePromotions(LocalDate.now());
    }

    /**
     * Returns all promotions that are active on the given date.
     *
     * @param date date to check against
     * @return list of active promotions on that date
     */
    public List<Promotion> listActivePromotions(LocalDate date) {
        List<Promotion> active = new ArrayList<>();
        if (date == null) {
            return active;
        }
        for (Promotion promotion : promotions) {
            if (promotion.isActive(date)) {
                active.add(promotion);
            }
        }
        return active;
    }

    /**
     * Finds the promotion that yields the highest discount for the given sale.
     * Only active promotions on the sale's date are evaluated.
     *
     * @param sale the sale to evaluate
     * @return the promotion providing the highest discount, or null if no discount applies
     */
    public Promotion findBestPromotionFor(Sale sale) {
        if (sale == null) {
            return null;
        }

        LocalDate date = sale.getDate() != null ? sale.getDate() : LocalDate.now();
        Promotion bestPromotion = null;
        double maxDiscount = 0.0;

        for (Promotion promotion : promotions) {
            if (promotion.isActive(date)) {
                double discount = promotion.calculateDiscount(sale);
                if (discount > maxDiscount) {
                    maxDiscount = discount;
                    bestPromotion = promotion;
                }
            }
        }

        return bestPromotion;
    }

    /**
     * Finds a promotion by its identifier.
     *
     * @param id promotion identifier
     * @return the promotion if found, or null otherwise
     */
    public Promotion findById(String id) {
        if (id == null) {
            return null;
        }
        for (Promotion promotion : promotions) {
            if (id.equals(promotion.getId())) {
                return promotion;
            }
        }
        return null;
    }
}
