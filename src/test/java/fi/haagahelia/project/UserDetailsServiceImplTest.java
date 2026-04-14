package fi.haagahelia.project;

import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserDetailsServiceImplTest {

    @Test
    void loadUserByUsername_returnsUserDetails() {
        AppUser appUser = new AppUser();
        appUser.setUsername("john");
        appUser.setPasswordHash("secret-hash");

        AppUserRepo repo = Mockito.mock(AppUserRepo.class);
        Mockito.when(repo.findByUsername("john")).thenReturn(appUser);

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(repo);
        UserDetails userDetails = service.loadUserByUsername("john");

        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.getPassword()).isEqualTo("secret-hash");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_missingUser_throwsUsernameNotFoundException() {
        AppUserRepo repo = Mockito.mock(AppUserRepo.class);
        Mockito.when(repo.findByUsername("missing")).thenReturn(null);

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(repo);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username not found.");
    }
}
