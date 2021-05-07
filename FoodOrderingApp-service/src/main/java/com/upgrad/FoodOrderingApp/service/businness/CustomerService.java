package com.upgrad.FoodOrderingApp.service.businness;
//This class will handle all the services related to Customers
import org.springframework.stereotype.Service;
import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    CustomerDao customerDao;

    @Autowired
    PasswordCryptographyProvider passwordCryptographyProvider;

    @Autowired
    UtilityProvider utilityProvider;

    @Autowired
    CustomerAuthDao customerAuthDao;


    //Method to save customer, generates appropriate exception according to given criteria
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        //Checks if customer already exists
        CustomerEntity existingCustomerEntity = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());
        if (existingCustomerEntity != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number");
        }
        //Checks if valid signup request has occurred
        if (!utilityProvider.isValidSignupRequest(customerEntity)) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        //Checks email validity
        if (!utilityProvider.isEmailValid(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }
        //Checks contact validity
        if (!utilityProvider.isContactValid(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }
        //Checks passwords validity
        if (!utilityProvider.isValidPassword(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        }

        //Password is encoded using passwordCryptographyProvider and encoded password adn salt is added to the customerentity and persisited.
        String[] encryptedPassword = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedPassword[0]);
        customerEntity.setPassword(encryptedPassword[1]);

        //Calls createCustomer of customerDao to create new customer.
        CustomerEntity createdCustomerEntity = customerDao.createCustomer(customerEntity);

        return createdCustomerEntity;
    }

    //Method to authenticate customer
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String contactNumber, String password) throws AuthenticationFailedException {
        //It Calls getCustomerByContactNumber method(customerDao) by contact no.
        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        //Checks if customer exists or not
        if (customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }
        String encryptedPassword = passwordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            //Calling createCustomerAuth (customerAuthDao) creating new CustomerAuthEntity in database with access token.
            CustomerAuthEntity createdCustomerAuthEntity = customerAuthDao.createCustomerAuth(customerAuthEntity);
            return createdCustomerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");//when Authenticate fails throws exception.
        }

    }

    //Method to logout
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerAuthDao.getCustomerAuthByAccessToken(accessToken);
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        if (customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        if (customerAuthEntity.getExpiresAt().compareTo(now) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        //Calling customerLogout(customerAuthDao) to change CustomerAuthEntity and logout customer.
        CustomerAuthEntity upatedCustomerAuthEntity = customerAuthDao.customerLogout(customerAuthEntity);
        return upatedCustomerAuthEntity;
    }

    //Method to update customer
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(CustomerEntity customerEntity) throws UpdateCustomerException {
        CustomerEntity customerToBeUpdated = customerDao.getCustomerByUuid(customerEntity.getUuid());
        customerToBeUpdated.setFirstName(customerEntity.getFirstName());
        customerToBeUpdated.setLastName(customerEntity.getLastName());
        CustomerEntity updatedCustomer = customerDao.updateCustomer(customerEntity);
        return updatedCustomer;
    }


    //Method to update customer password
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(String oldPassword,String newPassword,CustomerEntity customerEntity ) throws UpdateCustomerException {
        if (!utilityProvider.isValidPassword(newPassword)) {//Checking if the Password is Weak.
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        String encryptedOldPassword = passwordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());
        if (encryptedOldPassword.equals(customerEntity.getPassword())) {
            CustomerEntity tobeUpdatedCustomerEntity = customerDao.getCustomerByUuid(customerEntity.getUuid());
            String[] encryptedPassword = passwordCryptographyProvider.encrypt(newPassword);
            tobeUpdatedCustomerEntity.setSalt(encryptedPassword[0]);
            tobeUpdatedCustomerEntity.setPassword(encryptedPassword[1]);
            CustomerEntity updatedCustomerEntity = customerDao.updateCustomer(tobeUpdatedCustomerEntity);
            return updatedCustomerEntity;
        } else {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }
    }

    //Method to get customer by access token
    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerAuthDao.getCustomerAuthByAccessToken(accessToken);
        //Is customer logged in?
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        //Is customer logged out?
        if (customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        //Is access token expired?
        if (customerAuthEntity.getExpiresAt().compareTo(now) <= 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return customerAuthEntity.getCustomer();
    }
}
