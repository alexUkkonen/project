package fi.haagahelia.project;

import fi.haagahelia.project.config.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionUtilTest {

    private static final String TEST_KEY = "8x/w/bO3B+y/Lz1gZq8uP6H9h+L5mQ8uR4aT7vW2yE4=";
    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "base64Key", TEST_KEY);
        encryptionUtil.init();
    }

    
    @Test
    void encryptDecrypt_roundTrip() {
        String original = "https://example.com/moodle";

        String encrypted = encryptionUtil.encrypt(original);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(encrypted).isNotEmpty();
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encryptProducesDifferentValueThanOriginal() {
        String original = "test-data";

        String encrypted = encryptionUtil.encrypt(original);

        assertThat(encrypted).isNotEqualTo(original);
    }
}
