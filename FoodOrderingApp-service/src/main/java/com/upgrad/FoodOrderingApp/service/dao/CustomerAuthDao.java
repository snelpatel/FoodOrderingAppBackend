package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerAuthDao {

    @PersistenceContext private EntityManager entityManager;

    /**
     * T authorization access token
     *
     * @param customerAuthEntity  new authorization will be
     *     created
     */
    public void createCustomerAuthToken(CustomerAuthEntity customerAuthEntity) {
        entityManager.persist(customerAuthEntity);
    }

    /**
     *
     * @param accessToken during successful login.
     * @return CustomerAuthEntity token not found in database.
     *
     * @param accessToken the access token which will be searched in database
     * @return CustomerAuthEntity  if given access token exists
     */
    public CustomerAuthEntity getCustomerAuthByToken(final String accessToken) {
        try {
            return entityManager
                    .createNamedQuery("customerAuthByToken", CustomerAuthEntity.class)
                    .setParameter("accessToken", accessToken)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * This method updates the customers logout time in the database.
     *
     * @param updatedCustomerAuthEntity CustomerAuthEntity object to update.
     */
    public void updateCustomerAuth(final CustomerAuthEntity updatedCustomerAuthEntity) {
        entityManager.merge(updatedCustomerAuthEntity);
    }
}