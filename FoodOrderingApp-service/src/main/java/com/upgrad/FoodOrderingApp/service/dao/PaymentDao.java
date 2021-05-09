package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentDao {

    @PersistenceContext private EntityManager entityManager;

    /**
     * all the payment methods
     *
     * @return
     */
    public List<PaymentEntity> getAllPaymentMethods() {
        List<PaymentEntity> paymentMethods =
                entityManager.createNamedQuery("getAllPaymentMethods", PaymentEntity.class).getResultList();
        if (paymentMethods != null) {
            return paymentMethods;
        }
        return Collections.emptyList();
    }

    /**
     * Fetch payment based on UUID.
     *
     * @param paymentUUID
     * @return PaymentEntity
     */
    public PaymentEntity getPaymentByUUID(String paymentUUID) {
        try {
            PaymentEntity paymentEntity =
                    entityManager
                            .createNamedQuery("getPaymentByUUID", PaymentEntity.class)
                            .setParameter("paymentUUID", paymentUUID)
                            .getSingleResult();
            return paymentEntity;
        } catch (NoResultException nre) {
            return null;
        }
    }
}