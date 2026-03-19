package com.billermanagement.services;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Date;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

@Slf4j
public class BniEncryption {
    private static final int TIME_DIFF_LIMIT = 300;

    public BniEncryption() {
    }

    protected static String getTime() {
        Date date = new Date();
        String time = String.valueOf(date.getTime());
        return time.substring(0, Math.min(time.length(), 10));
    }

    public static String hashData(String json_data, String cid, String secret) {
        StringBuilder sb = new StringBuilder(getTime());
        String strrevtime = sb.reverse().toString();
        String hash = doubleEncrypt(strrevtime + "." + json_data, cid, secret);
        return hash;
    }

    public static String parseData(String hash, String cid, String secret) {
        String parsed_string = doubleDecrypt(hash, cid, secret);
        String[] arr = parsed_string.split("\\.", 2);
        if (arr.length == 2) {
            StringBuilder sb = new StringBuilder(arr[0]);
            String strrevtime = sb.reverse().toString();
            int _time = Integer.parseInt(strrevtime);
            if (tsDiff(_time)) {
                return arr[1];
            }
        }

        return null;
    }

    protected static Boolean tsDiff(int ts) {
        return Math.abs(ts - Integer.parseInt(getTime())) <= 300 ? true : false;
    }

    protected static String doubleEncrypt(String string, String cid, String secret) {
        byte[] result = new byte[string.length()];
        result = encrypt(string.getBytes(), cid);
        result = encrypt(result, secret);
        Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(result);
        String rtrm = base64.replaceAll("=+$", "");
        return rtrm.replace('+', '-').replace('/', '_');
    }

    protected static byte[] encrypt(byte[] str, String key) {
        int x = str.length;
        byte[] result = new byte[x];

        for(int i = 0; i < x; ++i) {
            byte chr = str[i];
            char keychr = key.charAt((i + key.length() - 1) % key.length());
            chr = (byte)((chr + keychr) % 128);
            result[i] = chr;
        }

        return result;
    }

    protected static String doubleDecrypt(String string, String cid, String secret) {
        for(Double ceils = Math.ceil((double)string.length() / 4.0D) * 4.0D; (double)string.length() < ceils; string = string + "=") {
        }

        string = string.replace('-', '+').replace('_', '/');
        Decoder decoder = Base64.getDecoder();
        byte[] result = decoder.decode(string.getBytes());
        result = decrypt(result, cid);
        result = decrypt(result, secret);
        return new String(result);
    }

    protected static byte[] decrypt(byte[] str, String key) {
        int x = str.length;
        byte[] result = new byte[x];

        for(int i = 0; i < x; ++i) {
            byte chr = str[i];
            char keychr = key.charAt((i + key.length() - 1) % key.length());
            chr = (byte)((chr - keychr + 128) % 128);
            result[i] = chr;
        }

        return result;
    }

     public static void TestBniEncryption(){
        BniEncryption hash = new BniEncryption();
        String data = "{\"customer_email\":\"tech@bni-ecollection.com\",\"trx_id\":\"INV/2016/01/01/0001\",\"datetime_expired\":\"2016-01-25 23:59:59\",\"client_id\":\"000\",\"customer_phone\":\"62856789101112\",\"customer_name\":\"Test Create Billing\",\"type\":\"createbilling\",\"virtual_account\":\"\",\"trx_amount\":\"1000\",\"billing_type\":\"c\"}";

        String cid = "513"; // from BNI, testing purpose
        String key = "ffcff955e7a53ebf76cda9cd16232ac4"; // from BNI, testing purpose

        String parsedData = hash.hashData(data, cid, key);
        String decodeData = hash.parseData(parsedData, cid, key);

        log.info(parsedData);
        log.info(decodeData);
    }
}
