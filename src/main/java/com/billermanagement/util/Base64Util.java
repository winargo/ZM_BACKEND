/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.billermanagement.util;

import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 *
 * @author sulaeman
 */
@Component
public class Base64Util {

    public Base64Util() {
    }

    public String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public String decode(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }

//    public static void main(String[] args) {
//        Base64Util base64Util = new Base64Util();
//        String encoded = base64Util.encode("eman:password");
//        String decoded = base64Util.decode(encoded);
//        
//        System.out.println("Encoded : "+ encoded);
//        System.out.println("Decoded  : "+ decoded);
//    }
}
