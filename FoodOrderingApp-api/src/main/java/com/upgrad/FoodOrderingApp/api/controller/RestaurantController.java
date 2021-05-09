package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.common.Utility;
import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;

@RestController
@RequestMapping("/")
public class RestaurantController {

    @Autowired private RestaurantService restaurantService;

    @Autowired private ItemService itemService;

    @Autowired private CustomerService customerService;

    @Autowired private CategoryService categoryService;

    /**
     * API endpoint gets list of all restaurant
     *
     * @return
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/restaurant",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurants() {

        List<RestaurantEntity> allRestaurants = restaurantService.restaurantsByRating();
        List<RestaurantList> allRestaurantsList = createListOfRestaurantList(allRestaurants);

        RestaurantListResponse restaurantListResponse =
                new RestaurantListResponse().restaurants(allRestaurantsList);

        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }

    /**
     *
     *
     * @param restaurantName
     * @return RestaurantListResponse
     * @throws RestaurantNotFoundException
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = ("/restaurant/name/{restaurant_name}"),
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantsBySearchString(
            @PathVariable("restaurant_name") final String restaurantName)
            throws RestaurantNotFoundException {

        List<RestaurantEntity> allRestaurants = restaurantService.restaurantsByName(restaurantName);
        List<RestaurantList> allRestaurantsList = createListOfRestaurantList(allRestaurants);
        RestaurantListResponse restaurantListResponse =
                new RestaurantListResponse().restaurants(allRestaurantsList);

        if (allRestaurantsList.isEmpty()) {
            return new ResponseEntity<>(restaurantListResponse, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
        }
    }

    /**
     *
     *
     * @param categoryUuid
     * @return
     * @throws CategoryNotFoundException
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = ("/restaurant/category/{category_id}"),
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategory(
            @PathVariable("category_id") final String categoryUuid) throws CategoryNotFoundException {

        List<RestaurantEntity> allRestaurants = restaurantService.restaurantByCategory(categoryUuid);
        List<RestaurantList> allRestaurantsList = createListOfRestaurantList(allRestaurants);
        RestaurantListResponse restaurantListResponse =
                new RestaurantListResponse().restaurants(allRestaurantsList);

        if (allRestaurantsList.isEmpty()) {
            return new ResponseEntity<>(restaurantListResponse, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
        }
    }

    /**
     *
     * @param restaurantUuid
     * @return RestaurantDetailsResponse
     * @throws RestaurantNotFoundException
     *
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = ("/restaurant/{restaurant_id}"),
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantById(
            @PathVariable("restaurant_id") final String restaurantUuid)
            throws RestaurantNotFoundException {

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantUuid);
        RestaurantDetailsResponse restaurantDetailsResponse =
                createRestaurantDetailsResponse(restaurantEntity);
        List<CategoryList> categories = getAllCategoryItemsInRestaurant(restaurantUuid);
        restaurantDetailsResponse.setCategories(categories);
        return new ResponseEntity<>(restaurantDetailsResponse, HttpStatus.OK);
    }

    /**
     *
     *
     * @param authorization
     * @param restaurantUuid
     * @param customerRating
     * @return
     * @throws AuthorizationFailedException
     * @throws RestaurantNotFoundException
     *
     * @throws InvalidRatingException
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            path = ("/restaurant/{restaurant_id}"),
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantRating(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("restaurant_id") final String restaurantUuid,
            @RequestParam("customer_rating") final Double customerRating)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {
        String accessToken = Utility.getTokenFromAuthorization(authorization);
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantUuid);
        RestaurantEntity updatedRestaurantEntity =
                restaurantService.updateRestaurantRating(restaurantEntity, customerRating);
        RestaurantUpdatedResponse restaurantUpdatedResponse = new RestaurantUpdatedResponse();
        restaurantUpdatedResponse.setId(fromString(updatedRestaurantEntity.getUuid()));
        restaurantUpdatedResponse.setStatus("RESTAURANT RATING UPDATED SUCCESSFULLY");

        return new ResponseEntity<RestaurantUpdatedResponse>(restaurantUpdatedResponse, HttpStatus.OK);
    }

    /**
     *
     * @param allRestaurants
     * @return
     */
    private List<RestaurantList> createListOfRestaurantList(
            final List<RestaurantEntity> allRestaurants) {
        List<RestaurantList> allRestaurantsList = new ArrayList<>();
        for (RestaurantEntity restaurantEntity : allRestaurants) {
            RestaurantList restaurantList = new RestaurantList();
            restaurantList.setId(fromString(restaurantEntity.getUuid()));
            RestaurantDetailsResponseAddress restaurantDetailsResponseAddress =
                    createRestaurantDetailsResponseAddress(restaurantEntity.getAddress());

            restaurantList.setAveragePrice(restaurantEntity.getAvgPrice());
            restaurantList.setAddress(restaurantDetailsResponseAddress);

            List<CategoryEntity> categoryEntities =
                    categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            StringBuilder categoriesString = new StringBuilder();
            for (CategoryEntity category : categoryEntities) {
                categoriesString.append(category.getCategoryName() + ", ");
            }
            restaurantList.setCategories(categoriesString.toString().replaceAll(", $", ""));
            restaurantList.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated());
            restaurantList.setPhotoURL(restaurantEntity.getPhotoUrl());
            restaurantList.setCustomerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()));
            restaurantList.setRestaurantName(restaurantEntity.getRestaurantName());
            allRestaurantsList.add(restaurantList);
        }

        return allRestaurantsList;
    }

    /**
     *
     * @param restaurantEntity
     * @return
     */

    private RestaurantDetailsResponse createRestaurantDetailsResponse(
            RestaurantEntity restaurantEntity) {

        RestaurantDetailsResponse restaurantDetailsResponse = new RestaurantDetailsResponse();

        restaurantDetailsResponse.setId(fromString(restaurantEntity.getUuid()));

        RestaurantDetailsResponseAddress restaurantDetailsResponseAddress =
                createRestaurantDetailsResponseAddress(restaurantEntity.getAddress());

        restaurantDetailsResponse.setAveragePrice(restaurantEntity.getAvgPrice());
        restaurantDetailsResponse.setCustomerRating(
                BigDecimal.valueOf(restaurantEntity.getCustomerRating()));
        restaurantDetailsResponse.setAddress(restaurantDetailsResponseAddress);


        restaurantDetailsResponse.setPhotoURL(restaurantEntity.getPhotoUrl());
        restaurantDetailsResponse.setRestaurantName(restaurantEntity.getRestaurantName());
        restaurantDetailsResponse.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated());

        return restaurantDetailsResponse;
    }

    /**
     *
     * @param restaurantAddress
     * @return
     */

    private RestaurantDetailsResponseAddress createRestaurantDetailsResponseAddress(
            AddressEntity restaurantAddress) {
        RestaurantDetailsResponseAddress restaurantDetailsResponseAddress =
                new RestaurantDetailsResponseAddress();
        RestaurantDetailsResponseAddressState restaurantDetailsResponseAddressState =
                new RestaurantDetailsResponseAddressState();
        AddressEntity addressEntity = restaurantAddress;
        restaurantDetailsResponseAddress.setId(fromString(addressEntity.getUuid()));
        restaurantDetailsResponseAddress.setLocality(addressEntity.getLocality());
        restaurantDetailsResponseAddress.setPincode(addressEntity.getPincode());
        restaurantDetailsResponseAddress.setFlatBuildingName(addressEntity.getFlatBuilNo());
        restaurantDetailsResponseAddress.setCity(addressEntity.getCity());

        restaurantDetailsResponseAddressState.setId(
                fromString(addressEntity.getState().getUuid()));
        restaurantDetailsResponseAddressState.setStateName(addressEntity.getState().getStateName());
        restaurantDetailsResponseAddress.setState(restaurantDetailsResponseAddressState);
        return restaurantDetailsResponseAddress;
    }

    /**
     *
     * @param restaurantUuid
     * @return
     */
    private List<CategoryList> getAllCategoryItemsInRestaurant(final String restaurantUuid) {
        List<CategoryList> allCategoryItems = new ArrayList<>();
        List<CategoryEntity> categories = categoryService.getCategoriesByRestaurant(restaurantUuid);

        for (CategoryEntity c : categories) {
            CategoryList categoryList = new CategoryList();
            categoryList.setId(fromString(c.getUuid()));
            categoryList.setCategoryName(c.getCategoryName());
            List<ItemList> allItemsInCategory =
                    getAllItemsInCategoryInRestaurant(restaurantUuid, c.getUuid());
            categoryList.setItemList(allItemsInCategory);
            allCategoryItems.add(categoryList);
        }

        return allCategoryItems;
    }

    /**
     *
     * @param restaurantUuid
     * @param categoryUuid
     * @return
     */
    private List<ItemList> getAllItemsInCategoryInRestaurant(
            final String restaurantUuid, final String categoryUuid) {
        List<ItemList> itemsInCategoryInRestaurant = new ArrayList<>();
        List<ItemEntity> items =
                itemService.getItemsByCategoryAndRestaurant(restaurantUuid, categoryUuid);
        for (ItemEntity item : items) {
            ItemList itemList = new ItemList();
            itemList.setItemName(item.getItemName());
            itemList.setPrice(item.getPrice());
            itemList.setId(fromString(item.getUuid()));
            if (item.getType().equals("0")) {
                itemList.setItemType(ItemList.ItemTypeEnum.valueOf("VEG"));
            } else {
                itemList.setItemType(ItemList.ItemTypeEnum.valueOf("NON_VEG"));
            }

            itemsInCategoryInRestaurant.add(itemList);
        }

        return itemsInCategoryInRestaurant;
    }
}