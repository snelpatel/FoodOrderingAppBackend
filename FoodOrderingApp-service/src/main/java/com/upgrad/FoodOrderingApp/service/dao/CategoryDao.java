package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CategoryDao {

    @PersistenceContext private EntityManager entityManager;

    /**
     *  CategoryEntity
     *
     * @param categoryUuid
     * @return CategoryEntity
     */
    public CategoryEntity getCategoryByUuid(final String categoryUuid) {
        try {
            return entityManager
                    .createNamedQuery("categoryByUuid", CategoryEntity.class)
                    .setParameter("uuid", categoryUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     *
     * @param
     * @return List of categoryEntity
     */
    public List<CategoryEntity> getAllCategoriesOrderedByName() {

        return entityManager
                .createNamedQuery("getAllCategoriesOrderedByName", CategoryEntity.class)
                .getResultList();
    }

    /**
     *
     *
     * @param restaurantUuid
     * @return List of categoryEntity
     */
    public List<CategoryEntity> getCategoriesByRestaurant(final String restaurantUuid) {
        try {
            return entityManager
                    .createNamedQuery("getCategoriesByRestaurant", CategoryEntity.class)
                    .setParameter("restaurantUuid", restaurantUuid)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}