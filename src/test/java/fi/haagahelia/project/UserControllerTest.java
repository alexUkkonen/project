package fi.haagahelia.project;

import fi.haagahelia.project.config.EncryptionUtil;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.web.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private AppUserRepo repository;
    private PasswordEncoder passwordEncoder;
    private EncryptionUtil encryptionUtil;
    private UserController controller;

    @BeforeEach
    void setUp() {
        repository = mock(AppUserRepo.class);
        passwordEncoder = mock(PasswordEncoder.class);
        encryptionUtil = mock(EncryptionUtil.class);
        controller = new UserController(repository, passwordEncoder, encryptionUtil);
    }

    @Test
    void saveNewUser_validInput_savesUserAndRedirectsToLogin() {
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(encryptionUtil.encrypt("https://moodle.example.com")).thenReturn("encrypted-url");
        when(repository.findByUsername("username")).thenReturn(null);

        String result = controller.saveNewUser(
                "username",
                "user@example.com",
                "https://moodle.example.com",
                "password123",
                "password123"
        );

        assertThat(result).isEqualTo("redirect:/login");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());
        AppUser savedUser = captor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("username");
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getMoodleUrl()).isEqualTo("encrypted-url");
    }

    @Test
    void saveNewUser_passwordsDoNotMatch_redirectsWithError() {
        String result = controller.saveNewUser(
                "username",
                "user@example.com",
                "https://moodle.example.com",
                "password123",
                "otherpass"
        );

        assertThat(result).isEqualTo("redirect:/register?error=Passwords do nor match!");
    }

    @Test
    void saveNewUser_passwordTooShort_redirectsWithError() {
        String result = controller.saveNewUser(
                "username",
                "user@example.com",
                "https://moodle.example.com",
                "short",
                "short"
        );

        assertThat(result).isEqualTo("redirect:/register?error=passowrd must be at least 8 characters long");
    }

    @Test
    void saveNewUser_usernameTaken_redirectsWithError() {
        when(repository.findByUsername("takenuser")).thenReturn(new AppUser());

        String result = controller.saveNewUser(
                "takenuser",
                "user@example.com",
                "https://moodle.example.com",
                "password123",
                "password123"
        );

        assertThat(result).isEqualTo("redirect:/register?error username taken");
    }

    @Test
    void showSettings_decryptsMoodleUrlAndAddsItToModel() {
        AppUser appUser = new AppUser();
        appUser.setMoodleUrl("encrypted-url");
        when(repository.findByUsername("john")).thenReturn(appUser);
        when(encryptionUtil.decrypt("encrypted-url")).thenReturn("https://moodle.example.com");

        Model model = mock(Model.class);
        Principal principal = () -> "john";

        String view = controller.showSettings(model, principal);

        assertThat(view).isEqualTo("settings");
        verify(model).addAttribute("currnetUrl", "https://moodle.example.com");
    }

    @Test
    void updateSettings_savesEncryptedUrlAndRedirectsToCalendar() {
        AppUser appUser = new AppUser();
        when(repository.findByUsername("john")).thenReturn(appUser);
        when(encryptionUtil.encrypt("https://moodle.example.com")).thenReturn("encrypted-url");

        Principal principal = () -> "john";

        String view = controller.updateSettings("https://moodle.example.com", principal);

        assertThat(view).isEqualTo("redirect:/calendar");
        assertThat(appUser.getMoodleUrl()).isEqualTo("encrypted-url");
        verify(repository).save(appUser);
    }
}
