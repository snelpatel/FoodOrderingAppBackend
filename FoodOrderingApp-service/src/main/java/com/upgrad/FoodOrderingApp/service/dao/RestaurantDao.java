package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 *
 */
@Repository
public class RestaurantDao {
    @PersistenceContext private EntityManager entityManager;

    /**
     *
     *
     * @param uuid
     * @return RestaurantEntity if found in database else null.
     */
    public RestaurantEntity restaurantByUUID(String uuid) {
        try {
            return entityManager
                    .createNamedQuery("restaurantByUUID", RestaurantEntity.class)
                    .setParameter("uuid", uuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     *
     * @param
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByRating() {
        return entityManager
                .createNamedQuery("restaurantsByRating", RestaurantEntity.class)
                .getResultList();
    }

    /**
     *
     *
     * @param searchString
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByName(final String searchString) {
        return entityManager
                .createNamedQuery("getRestaurantByName", RestaurantEntity.class)
                .setParameter("searchString", "%" + searchString + "%")
                .getResultList();
    }

    /**
     *
     *
     * @param restaurantEntity
     * @return restaurantEntity
     */
    public RestaurantEntity updateRestaurantEntity(final RestaurantEntity restaurantEntity) {
        RestaurantEntity updatedRestaurantEntity = entityManager.merge(restaurantEntity);
        return updatedRestaurantEntity;
    }

    /**
     *
     *
     * @param categoryUuid
     * @return List of restaurantEntity
     */
    public List<RestaurantEntity> restaurantByCategory(final String categoryUuid) {

        return entityManager
                .createNamedQuery("restaurantByCategory", RestaurantEntity.class)
                .setParameter("categoryUuid", categoryUuid)
                .getResultList();
    }
}