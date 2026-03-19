package com.billermanagement.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

@Slf4j
@Service
public class OGPEncryptionService {

  @Value("${ogp.certification.path}")
  private String ogpCertPath;

  @Value("${ogp.signature.alias}")
  private String ogpSignAlias = "";

  @Value("${ogp.signature.password}")
  private String ogpSignPassword;

  String encrypt(String data) {
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(new FileInputStream(ogpCertPath), ogpSignPassword.toCharArray());
      PrivateKey privateKey = (PrivateKey) keyStore.getKey(ogpSignAlias, ogpSignPassword.toCharArray());

      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(data.getBytes());

      return (new BASE64Encoder()).encode(signature.sign()).replace("\n", "").replace("\r", "");
    } catch (Exception e) {
      log.warn("Got Exception " + e);
      return null;
    }
  }
}
