package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.PaymentDao;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired private PaymentDao paymentDao;

    /**
     *
     *
     * @return
     */
    public List<PaymentEntity> getAllPaymentMethods() {
        return paymentDao.getAllPaymentMethods();
    }

    /**
     *
     *
     * @param paymentUUID
     * @return
     */
    public PaymentEntity getPaymentByUUID(String paymentUUID) throws PaymentMethodNotFoundException {
        PaymentEntity payment = paymentDao.getPaymentByUUID(paymentUUID);
        if (payment == null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }else {
            return payment;
        }
    }
}