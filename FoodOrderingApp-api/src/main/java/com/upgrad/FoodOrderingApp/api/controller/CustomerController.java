package com.upgrad.FoodOrderingApp.api.controller;

import org.springframework.web.bind.annotation.*;
import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService; //Customer service handler

    @Autowired
    UtilityProvider utilityProvider; //Validity check handler


    //Request: /customer/signup
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST,path = "/signup",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signUpCustomer(@RequestBody(required = false)  final SignupCustomerRequest signupCustomerRequest)throws SignUpRestrictedException {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setUuid(UUID.randomUUID().toString());
        utilityProvider.isValidSignupRequest(customerEntity);
        CustomerEntity signedUpCustomer =  customerService.saveCustomer(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(signedUpCustomer.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse,HttpStatus.CREATED);
    }


    //Request: /customer/login
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST,path = "/login",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> customerLogin (@RequestHeader("authorization") final String authorization)throws AuthenticationFailedException {
        utilityProvider.isValidAuthorizationFormat(authorization);
        byte[] decoded = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedAuth = new String(decoded);
        String[] decodedArray = decodedAuth.split(":");
        CustomerAuthEntity customerAuthEntity = customerService.authenticate(decodedArray[0],decodedArray[1]);
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());

        List<String> header = new ArrayList<>();
        header.add("access-token");
        headers.setAccessControlExposeHeaders(header);

        LoginResponse loginResponse = new LoginResponse()
                .id(customerAuthEntity.getCustomer().getUuid())
                .contactNumber(customerAuthEntity.getCustomer().getContactNumber())
                .emailAddress(customerAuthEntity.getCustomer().getEmail())
                .firstName(customerAuthEntity.getCustomer().getFirstName())
                .lastName(customerAuthEntity.getCustomer().getLastName())
                .message("LOGGED IN SUCCESSFULLY");

        return new ResponseEntity<LoginResponse>(loginResponse,headers,HttpStatus.OK);
    }


    //Request: /customer/logout
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST,path = "/logout",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> customerLogout (@RequestHeader("authorization")final String authorization)throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[1];

        CustomerAuthEntity customerAuthEntity =  customerService.logout(accessToken);

        LogoutResponse logoutResponse = new LogoutResponse()
                .id(customerAuthEntity.getCustomer().getUuid())
                .message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse,HttpStatus.OK);
    }

    //Request: /customer
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT,path = "",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomerDetails(@RequestHeader("authorization")final String authorization,@RequestBody(required = false) UpdateCustomerRequest updateCustomerRequest)throws AuthorizationFailedException,UpdateCustomerException{
        utilityProvider.isValidUpdateCustomerRequest(updateCustomerRequest.getFirstName());
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity toBeUpdatedCustomerEntity = customerService.getCustomer(accessToken);
        toBeUpdatedCustomerEntity.setFirstName(updateCustomerRequest.getFirstName());
        toBeUpdatedCustomerEntity.setLastName(updateCustomerRequest.getLastName());
        CustomerEntity updatedCustomerEntity = customerService.updateCustomer(toBeUpdatedCustomerEntity);
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse()
                .firstName(updatedCustomerEntity.getFirstName())
                .lastName(updatedCustomerEntity.getLastName())
                .id(updatedCustomerEntity.getUuid())
                .status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse,HttpStatus.OK);
    }

    //Request: /customer/password
    @CrossOrigin
    @RequestMapping(method = RequestMethod.PUT,path = "/password",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updateCustomerPassword(@RequestHeader ("authorization") final String authorization,@RequestBody(required = false) UpdatePasswordRequest updatePasswordRequest)throws AuthorizationFailedException,UpdateCustomerException{
        utilityProvider.isValidUpdatePasswordRequest(updatePasswordRequest.getOldPassword(),updatePasswordRequest.getNewPassword());
        String accessToken = authorization.split("Bearer ")[1];
        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        CustomerEntity toBeUpdatedCustomerEntity = customerService.getCustomer(accessToken);
        CustomerEntity updatedCustomerEntity = customerService.updateCustomerPassword(oldPassword,newPassword,toBeUpdatedCustomerEntity);
        UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse()
                .id(updatedCustomerEntity.getUuid())
                .status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse,HttpStatus.OK);
    }
}
