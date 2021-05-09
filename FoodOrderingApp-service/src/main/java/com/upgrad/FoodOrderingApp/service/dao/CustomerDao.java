package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext private EntityManager entityManager;

    /**
     *
     *
     * @param customerEntity for creating new customer.
     * @return CustomerEntity object.
     */
    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) {
        entityManager.persist(customerEntity);
        return customerEntity;
    }

    /**
     *
     *
     * @param contactNumber already registered with this number
     * @return CustomerEntity number exists in the database
     */
    public CustomerEntity getCustomerByContactNumber(final String contactNumber) {
        try {
            return entityManager
                    .createNamedQuery("customerByContactNumber", CustomerEntity.class)
                    .setParameter("contactNumber", contactNumber)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     *
     *
     * @param customerEntity update.
     * @return Updated  object.
     */
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity) {
        entityManager.merge(customerEntity);
        return customerEntity;
    }
}