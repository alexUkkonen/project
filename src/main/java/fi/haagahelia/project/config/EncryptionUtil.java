package fi.haagahelia.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES"; //we define the algorithm we will be using.

    @Value("${app.security.encryption.key}") //We tell it to use the key we stored in application propperties
    private String base64Key;

    private byte[] keyBytes;

    // make it run on startupp
    @PostConstruct
    public void init() {
        this.keyBytes = Base64.getDecoder().decode(base64Key);
    }

    public String encrypt(String value) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM); //We create a SecretKey using the ALGORITHM AES
            Cipher cipher = Cipher.getInstance(ALGORITHM); //We tell it to use the AES Cipher
            cipher.init(Cipher.ENCRYPT_MODE, secretKey); //We initialize the cipher in encrypt mode
            byte[] encryptedValue = cipher.doFinal(value.getBytes()); //we encrypt
            return Base64.getEncoder().encodeToString(encryptedValue); //the result.
        }
        catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    public String decrypt(String value) { //This is the decryption version of the code above. no major differences just changed to decrypt mode
        try {
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedValue = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(decryptedValue);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

}
