package com.upgrad.FoodOrderingApp.service.dao;

//Methods to access database related to Customer Entity

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {
    @PersistenceContext
    private EntityManager entityManager;

    //Method to get customer by Contact number, returns null if no results found
    public CustomerEntity getCustomerByContactNumber (final String contact_number){
        try{
            CustomerEntity customer = entityManager.createNamedQuery("customerByContactNumber",CustomerEntity.class).setParameter("contact_number",contact_number).getSingleResult();
            return customer;
        }catch (NoResultException nre){
            return null;
        }
    }

    //Method to save new customer entity
    public CustomerEntity createCustomer(CustomerEntity customerEntity){
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    //Method to update customer
    public CustomerEntity updateCustomer(CustomerEntity customerToBeUpdated){
        entityManager.merge(customerToBeUpdated);
        return customerToBeUpdated;
    }

    //Method to find customer by UUID
    public CustomerEntity getCustomerByUuid (final String uuid){
        try {
            CustomerEntity customer = entityManager.createNamedQuery("customerByUuid",CustomerEntity.class).setParameter("uuid",uuid).getSingleResult();
            return customer;
        }catch (NoResultException nre){
            return null;
        }
    }



}
