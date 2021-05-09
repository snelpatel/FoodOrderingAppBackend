package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired public CategoryDao categoryDao;

    /*
     *
     * @param uuid
     * @return CategoryEntity
     * */
    public CategoryEntity getCategoryById(final String uuid) throws CategoryNotFoundException {
        if (uuid == null) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(uuid);
        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        return categoryEntity;
    }

    /*
     *
     *
     * @return CategoryEntity
     * */
    public List<CategoryEntity> getAllCategoriesOrderedByName() {
        List<CategoryEntity> categoryEntities = categoryDao.getAllCategoriesOrderedByName();
        return categoryEntities;
    }

    /**
     *
     *
     * @param restaurantUuid
     * @return  CategoryEntity
     */
    public List<CategoryEntity> getCategoriesByRestaurant(final String restaurantUuid) {
        List<CategoryEntity> categoryEntities = categoryDao.getCategoriesByRestaurant(restaurantUuid);

        return categoryEntities;
    }
}