package com.upgrad.FoodOrderingApp.service.common;
//This class provides various utility methods for customer entity

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.stereotype.Component;

@Component
public class UtilityProvider {

    //Method to validate the password for given conditions,1 Uppercase letter,1 Number,1 Special Character and atleast 8 characters
    public boolean isValidPassword(String password){
        Boolean lowerCase = false;
        Boolean upperCase = false;
        Boolean number = false;
        Boolean specialCharacter = false;

        if(password.length() < 8){
            return false;
        }
        if(password.matches("(?=.*[0-9]).*")){
            number = true;
        }
        if(password.matches("(?=.*[a-z]).*")){
            lowerCase = true;
        }
        if(password.matches("(?=.*[A-Z]).*")){
            upperCase = true;
        }
        if(password.matches("(?=.*[#@$%&*!^]).*")){
            specialCharacter = true;
        }
        if(lowerCase && upperCase){
            if(specialCharacter && number){
                return true;
            }
        }else{
            return false;
        }
        return false;
    }

    //Method to validate Contact Number
    public boolean isContactValid(String contactNumber){
        Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");
        Matcher m = p.matcher(contactNumber);
        return (m.find() && m.group().equals(contactNumber));
    }

    //Method to validate email address
    public boolean isEmailValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    //Method to validate zipcode
    public boolean isPincodeValid(String pincode){
        Pattern p = Pattern.compile("\\d{6}\\b");
        Matcher m = p.matcher(pincode);
        return (m.find() && m.group().equals(pincode));
    }

    //Method to validate signup request
    public boolean isValidSignupRequest (CustomerEntity customerEntity)throws SignUpRestrictedException{
        if (customerEntity.getFirstName() == null || customerEntity.getFirstName() == ""){
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }
        if(customerEntity.getPassword() == null||customerEntity.getPassword() == ""){
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }
        if (customerEntity.getEmail() == null||customerEntity.getEmail() == ""){
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }
        if (customerEntity.getContactNumber() == null||customerEntity.getContactNumber() == ""){
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }
        return true;
    }

    //Method to validate Authorization format
    public boolean isValidAuthorizationFormat(String authorization)throws AuthenticationFailedException{
        try {
            byte[] decoded = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedAuth = new String(decoded);
            String[] decodedArray = decodedAuth.split(":");
            String username = decodedArray[0];
            String password = decodedArray[1];
            return true;
        }catch (ArrayIndexOutOfBoundsException exc){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }
    }

    //Method to validate Customer Update Request
    public boolean isValidUpdateCustomerRequest (String firstName)throws UpdateCustomerException {
        if (firstName == null || firstName == "") {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        return true;
    }

    //Method to validate Password Update Request.
    public boolean isValidUpdatePasswordRequest(String oldPassword,String newPassword) throws UpdateCustomerException{
        if (oldPassword == null || oldPassword == "") {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        if (newPassword == null || newPassword == "") {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        return true;
    }

    //Method to validate Customer Ratings
    public boolean isValidCustomerRating(String cutomerRating){
        if(cutomerRating.equals("5.0")){
            return true;
        }
        Pattern p = Pattern.compile("[1-4].[0-9]");
        Matcher m = p.matcher(cutomerRating);
        return (m.find() && m.group().equals(cutomerRating));
    }

    //Method for sorting, internal usage
    public Map<String,Integer> sortMapByValues(Map<String,Integer> map){
        List<Map.Entry<String,Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue().compareTo(o1.getValue()));
            }
        });

        Map<String, Integer> sortedByValueMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> item : list) {
            sortedByValueMap.put(item.getKey(), item.getValue());
        }
        return sortedByValueMap;
    }
}
