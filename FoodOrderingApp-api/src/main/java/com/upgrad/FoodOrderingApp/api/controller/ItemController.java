package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private RestaurantService restaurantService;

    /**
     * top five  items of a restaurant
     *
     * @param restaurantId UUID for the restaurant
     * @return ItemListResponse
     * @throws RestaurantNotFoundException
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/item/restaurant/{restaurant_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ItemListResponse> getTopFiveItemsForRestaurant(
            @PathVariable("restaurant_id") final String restaurantId) throws RestaurantNotFoundException {
        RestaurantEntity restaurant = restaurantService.restaurantByUUID(restaurantId);
        List<ItemEntity> topFiveItems = itemService.getItemsByPopularity(restaurant);
        ItemListResponse itemListResponse = new ItemListResponse();
        for (ItemEntity entity : topFiveItems) {
            ItemList itemList = new ItemList();
            itemList.setId(UUID.fromString(entity.getUuid()));
            itemList.setItemName(entity.getItemName());

            ItemList.ItemTypeEnum itemTypeEnum = ItemList.ItemTypeEnum.VEG;

            if(Integer.valueOf(entity.getType()) == 0) {
                itemTypeEnum = ItemList.ItemTypeEnum.VEG;
            }else {
                itemTypeEnum = ItemList.ItemTypeEnum.NON_VEG;
            }
            itemList.setPrice(entity.getPrice());
            itemList.setItemType(itemTypeEnum);
            itemListResponse.add(itemList);
        }
        return new ResponseEntity<>(itemListResponse, HttpStatus.OK);
    }
}