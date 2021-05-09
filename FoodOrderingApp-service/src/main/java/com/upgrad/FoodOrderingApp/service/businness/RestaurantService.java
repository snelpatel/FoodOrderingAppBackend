package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    @Autowired private RestaurantDao restaurantDao;

    /**
     *
     *
     * @param
     * @return
     * @throws RestaurantNotFoundException
     */
    public RestaurantEntity restaurantByUUID(String uuid) throws RestaurantNotFoundException {
        RestaurantEntity restaurant = restaurantDao.restaurantByUUID(uuid);
        if (restaurant == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        return restaurant;
    }

    /**
     *
     * @return
     */
    public List<RestaurantEntity> restaurantsByRating() {

        return restaurantDao.restaurantsByRating();
    }

    /**
     * G
     *
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantsByName(final String search)
            throws RestaurantNotFoundException {
        if (search == null || search.isEmpty()) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> relevantRestaurantEntities = restaurantDao.restaurantsByName(search);

        return relevantRestaurantEntities;
    }

    /**
     * Gets based on Category Uuid
     *
     * @return List of RestaurantEntity
     */
    public List<RestaurantEntity> restaurantByCategory(final String categoryUuid)
            throws CategoryNotFoundException {
        if (categoryUuid == null) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantByCategory(categoryUuid);
        if (restaurantEntities == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        return restaurantEntities;
    }

    /**
     * Updates the customer rating
     * @param restaurantEntity
     * @return RestaurantEntity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantRating(
            final RestaurantEntity restaurantEntity, final Double customerRating)
            throws InvalidRatingException {
        if (Double.valueOf(customerRating) < 1 || Double.valueOf(customerRating) > 5) {
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        Double currentRating = restaurantEntity.getCustomerRating();
        Integer numberCustomersRated = restaurantEntity.getNumberCustomersRated();


        Double newRating =
                ((currentRating * numberCustomersRated) + currentRating) / (numberCustomersRated + 1);


        restaurantEntity.setNumberCustomersRated(numberCustomersRated + 1);
        restaurantEntity.setCustomerRating(newRating);

        return restaurantDao.updateRestaurantEntity(restaurantEntity);
    }
}