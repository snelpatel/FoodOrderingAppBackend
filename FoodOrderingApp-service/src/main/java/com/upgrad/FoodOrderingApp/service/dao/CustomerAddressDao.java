package com.upgrad.FoodOrderingApp.service.dao;

//Methods to access database related to Customer Address Entity
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerAddressDao {
    @PersistenceContext
    private EntityManager entityManager;

    //Method to save customer address
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity){
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    //Method to list all customer address by customer
    public List<CustomerAddressEntity> getAllCustomerAddressByCustomer(CustomerEntity customerEntity){
        try{
            Integer active = 1;
            List <CustomerAddressEntity> customerAddressEntities = entityManager.createNamedQuery("getAllCustomerAddressByCustomer",CustomerAddressEntity.class).setParameter("customer_entity",customerEntity).setParameter("active",active).getResultList();
            return customerAddressEntities;
        }catch (NoResultException nre){
            return null;
        }
    }

    //Method to list all customer address by address
    public CustomerAddressEntity getCustomerAddressByAddress(AddressEntity addressEntity){
        try {
            CustomerAddressEntity customerAddressEntity = entityManager.createNamedQuery("getCustomerAddressByAddress",CustomerAddressEntity.class).setParameter("address_entity",addressEntity).getSingleResult();
            return customerAddressEntity;
        }catch (NoResultException nre){
            return null;
        }
    }
}
