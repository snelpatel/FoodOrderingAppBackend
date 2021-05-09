package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.common.Utility;
import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/")
public class CustomerController {


    @Autowired private CustomerService customerService;

    /**
     * API to register a new customer
     *
     * @param signupCustomerRequest argument to get essential customer information
     * @return ResponseEntity<SignupCustomerResponse> return type to share result and valid code
     * @throws SignUpRestrictedException exception handling
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/signup",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(
            @RequestBody(required = true) final SignupCustomerRequest signupCustomerRequest)
            throws SignUpRestrictedException {

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmailAddress(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        if (
                !customerEntity.getEmailAddress().isEmpty()  &&
                        !customerEntity.getFirstName().isEmpty() &&
                        !customerEntity.getContactNumber().isEmpty()
                        && !customerEntity.getPassword().isEmpty()) {
            CustomerEntity createdCustomerEntity = customerService.saveCustomer(customerEntity);

            SignupCustomerResponse customerResponse =
                    new SignupCustomerResponse()
                            .id(createdCustomerEntity.getUuid())
                            .status("CUSTOMER SUCCESSFULLY REGISTERED");
            return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
        } else {
            throw new SignUpRestrictedException(
                    "SGR-005", "Except last name all fields should be filled");
        }
    }



    /**
     * serves as login endpoint for API
     *
     * @param authorization for auhtorising user
     * @return ResponseEntity<LoginResponse> response with result and HTTP status coe
     * @throws AuthenticationFailedException for handling exceptions
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {
        byte[] decryptedCode;
        String contactNumber;
        String password;
        try {
            decryptedCode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decryptedText = new String(decryptedCode);
            String[] decryptedArray = decryptedText.split(":");
            contactNumber = decryptedArray[0];
            password = decryptedArray[1];
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            throw new AuthenticationFailedException(
                    "ATH-003", "Incorrect format of decoded customer name and password");
        }

        CustomerAuthEntity registeredCustomerAuthEntity =
                customerService.authenticate(contactNumber, password);

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", registeredCustomerAuthEntity.getAccessToken());
        List<String> header = new ArrayList<>();
        header.add("access-token");
        headers.setAccessControlExposeHeaders(header);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setFirstName(registeredCustomerAuthEntity.getCustomer().getFirstName());
        loginResponse.setContactNumber(registeredCustomerAuthEntity.getCustomer().getContactNumber());
        loginResponse.setEmailAddress(registeredCustomerAuthEntity.getCustomer().getEmailAddress());
        loginResponse.setId(registeredCustomerAuthEntity.getCustomer().getUuid());
        loginResponse.setLastName(registeredCustomerAuthEntity.getCustomer().getLastName());
        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");


        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    /**
     * API representing logout functionality
     *
     * @param authorization for authorising the customer logout
     * @return ResponseEntity<LogoutResponse> response object for successful result and status
     * @throws AuthorizationFailedException for any failed validations
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/customer/logout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        final String accessToken = Utility.getTokenFromAuthorization(authorization);
        CustomerAuthEntity registeredCustomerAuthEntity = customerService.logout(accessToken);
        LogoutResponse logoutResponse =
                new LogoutResponse()
                        .id(registeredCustomerAuthEntity.getCustomer().getUuid())
                        .message("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    /**
     * API representing customer portal
     *
     * @param updateCustomerRequest update information of customer
     * @param authorization to authorise the customer
     * @return ResponseEntity<UpdateCustomerResponse> result with HTTP status
     * @throws AuthorizationFailedException for failed authorisation
     * @throws UpdateCustomerException for failed validations
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> update(
            @RequestHeader("authorization") final String authorization,
            @RequestBody(required = true) final UpdateCustomerRequest updateCustomerRequest)
            throws UpdateCustomerException, AuthorizationFailedException {
        if (updateCustomerRequest.getFirstName() == null
                || updateCustomerRequest.getFirstName().isEmpty()) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }

        String accessToken = Utility.getTokenFromAuthorization(authorization);

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        customerEntity.setFirstName(updateCustomerRequest.getFirstName());
        customerEntity.setLastName(updateCustomerRequest.getLastName());

        CustomerEntity updatedCustomerEntity = customerService.updateCustomer(customerEntity);

        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setId(updatedCustomerEntity.getUuid());
        updateCustomerResponse.setFirstName(updatedCustomerEntity.getFirstName());
        updateCustomerResponse.setLastName(updatedCustomerEntity.getLastName());
        updateCustomerResponse.status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse, HttpStatus.OK);
    }


    /**
     * @param updatePasswordRequest contains information about customer password updates
     * @param authorization for authorising the user
     * @return ResponseEntity<UpdatePasswordResponse> result with http status
     * @throws AuthorizationFailedException failing authorisation handling
     * @throws UpdateCustomerException handling any failed validations
     */
    @CrossOrigin
    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/customer/password",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> changePassword(
            @RequestHeader("authorization") final String authorization,
            @RequestBody(required = true) final UpdatePasswordRequest updatePasswordRequest)
            throws UpdateCustomerException, AuthorizationFailedException {

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        if (oldPassword != null
                && !oldPassword.isEmpty()) {
            if (newPassword != null
                    && !newPassword.isEmpty()) {
                String token = Utility.getTokenFromAuthorization(authorization);
                CustomerEntity cusEntity = customerService.getCustomer(token);

                CustomerEntity updatedCusEntity =
                        customerService.updateCustomerPassword(oldPassword, newPassword, cusEntity);

                UpdatePasswordResponse updatePasswordResponse =
                        new UpdatePasswordResponse()
                                .id(updatedCusEntity.getUuid())
                                .status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");

                return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
            } else {
                throw new UpdateCustomerException("UCR-003", "No field should be empty");
            }
        }
        else {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
    }
}