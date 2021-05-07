package com.upgrad.FoodOrderingApp.service.dao;

//Methods to access database related to State Entity
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

@Repository
public class StateDao {
    @PersistenceContext
    private EntityManager entityManager;

    //Method to get state by uuid
    public StateEntity getStateByUuid(String uuid){
        try{
            StateEntity stateEntity = entityManager.createNamedQuery("getStateByUuid",StateEntity.class).setParameter("uuid",uuid).getSingleResult();
            return stateEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    //Method to list all states
    public List<StateEntity> getAllStates(){
        try {
            List<StateEntity> stateEntities = entityManager.createNamedQuery("getAllStates",StateEntity.class).getResultList();
            return stateEntities;
        }catch (NoResultException nre){
            return null;
        }
    }
}
