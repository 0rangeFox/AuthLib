package com.mojang.authlib.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.security.*;

@Getter
@RequiredArgsConstructor
public class Property {

    private final String name;
    private final String value;
    private final String signature;

    public Property(String value, String name) {
        this(value, name, null);
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    public boolean isSignatureValid(PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(this.value.getBytes());

            return signature.verify(Base64.decodeBase64(this.signature));
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

}
