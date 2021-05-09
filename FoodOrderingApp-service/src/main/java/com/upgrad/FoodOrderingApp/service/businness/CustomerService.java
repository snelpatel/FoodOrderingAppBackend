package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    @Autowired private CustomerDao cusDao;

    @Autowired private PasswordCryptographyProvider passwordCryptographyProvider;

    @Autowired private CustomerAuthDao cusAuthDao;

    /**
     *
     *
     * @param customerEntity
     * @return CustomerEntity object.
     * @throws SignUpRestrictedException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity)
            throws SignUpRestrictedException {

        if (
                !customerEntity.getEmailAddress().isEmpty()  &&
                        !customerEntity.getFirstName().isEmpty() &&
                        !customerEntity.getContactNumber().isEmpty()
                        && !customerEntity.getPassword().isEmpty()) {

            if (!isValidContactNumber(customerEntity.getContactNumber())) {
                throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
            }
            if (!isValidEmail(customerEntity.getEmailAddress())) {
                throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
            }

            if (isNewContactNumber(customerEntity.getContactNumber())) {
                throw new SignUpRestrictedException(
                        "SGR-001", "This contact number is already registered! Try other contact number.");
            }
            if (!isValidPassword(customerEntity.getPassword())) {
                throw new SignUpRestrictedException("SGR-004", "Weak password!");
            }
            customerEntity.setUuid(UUID.randomUUID().toString());

            String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return cusDao.saveCustomer(customerEntity);
        } else {
            throw new SignUpRestrictedException(
                    "SGR-005", "Except last name all fields should be filled");
        }
    }

    /**
     *
     * @param username customers contactnumber will be the username.
     * @param password customers password.
     * @return CustomerAuthEntity object.
     * @throws AuthenticationFailedException if any of the validation fails.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String username, String password)
            throws AuthenticationFailedException {
        CustomerEntity customerEntity = cusDao.getCustomerByContactNumber(username);
        if (customerEntity == null) {
            throw new AuthenticationFailedException(
                    "ATH-001", "This contact number has not been registered!");
        }
        final String encryptedPassword =
                PasswordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
        CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
        customerAuthEntity.setCustomer(customerEntity);
        customerAuthEntity.setUuid(UUID.randomUUID().toString());
        final ZonedDateTime dayT = ZonedDateTime.now();
        final ZonedDateTime expiresAt = dayT.plusHours(8);
        customerAuthEntity.setLoginAt(dayT);
        customerAuthEntity.setExpiresAt(expiresAt);
        String accessToken = jwtTokenProvider.generateToken(customerEntity.getUuid(), dayT, expiresAt);
        customerAuthEntity.setAccessToken(accessToken);
        cusAuthDao.createCustomerAuthToken(customerAuthEntity);
        return customerAuthEntity;
    }
    /**
     *
     *
     * @param accessToken Token for access generate
     * @return updated information
     * @throws AuthorizationFailedException for validations failure
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(final String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = cusAuthDao.getCustomerAuthByToken(accessToken);
        CustomerEntity customerEntity = getCustomer(accessToken);
        customerAuthEntity.setCustomer(customerEntity);
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        cusAuthDao.updateCustomerAuth(customerAuthEntity);
        return customerAuthEntity;
    }

    /**
     * customer details update PAI
     *
     * @param customerEntity updated information
     * @return updated information return after successul DB operation
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity) {
        return cusDao.updateCustomer(customerEntity);
    }

    /**
     * This method checks if the token is valid.
     *
     * @param accessToken for authorisation
     * @return customer information return
     * @throws AuthorizationFailedException exception in case customer token invalidated
     */
    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = cusAuthDao.getCustomerAuthByToken(accessToken);
        if (customerAuthEntity != null) {

            if (customerAuthEntity.getLogoutAt() != null) {
                throw new AuthorizationFailedException(
                        "ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
            }

            if (ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())) {
                throw new AuthorizationFailedException(
                        "ATHR-003", "Your session is expired. Log in again to access this endpoint.");
            }
            return customerAuthEntity.getCustomer();
        } else {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
    }

    /**
     * This method updates password of the given customer.
     *
     * @param oldPassword Customer's old password.
     * @param newPassword Customer's new password.
     * @param customerEntity CustomerEntity object to update the password.
     * @return Updated CustomerEntity object.
     * @throws UpdateCustomerException If any of the validation for old or new password fails.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(
            final String oldPassword, final String newPassword, final CustomerEntity customerEntity)
            throws UpdateCustomerException {
        if (isValidPassword(newPassword)) {
            String oldEncryptedPassword =
                    PasswordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());
            if (!oldEncryptedPassword.equals(customerEntity.getPassword())) {
                throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
            }
            String[] encryptedText = passwordCryptographyProvider.encrypt(newPassword);
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return cusDao.updateCustomer(customerEntity);
        } else {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
    }

    // method checks for given contact number is already registered or not
    private boolean isNewContactNumber(final String contactNumber) {
        return cusDao.getCustomerByContactNumber(contactNumber) != null;
    }

    private boolean isValidEmail(final String emailAddress) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(emailAddress);
        return matcher.matches();
    }


    // method checks for given contact number is valid or not
    private boolean isValidContactNumber(final String contactNumber) {
        if (contactNumber.length() != 10) {
            return false;
        }
        for (int i = 0; i < contactNumber.length(); i++) {
            if (!Character.isDigit(contactNumber.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // method checks for given password meets the requirements or not
    private boolean isValidPassword(final String password) {
        return password.matches("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#@$%&*!^]).{8,}$");
    }
}