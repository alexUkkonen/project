package fi.haagahelia.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.CommandLineRunner;

import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.model.AppUser;

@Configuration
public class SecurityConfig {

    @Bean // This is used to encrypt the password so we dont have any plaintext passwords saved.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login").permitAll() // This permits anyone to access /register and /login
                .anyRequest().authenticated() // with this we make it so that anything else requires you to be logged in (authenticated)
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/calendar", true) //redirects any logged in user to /calendar
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }

    @Bean
    public CommandLineRunner loadData(AppUserRepo repository, PasswordEncoder passwordEncoder) { // here we create a test user. the test user will be using my URL
        return (args) -> { // TODO: Remove before pushing to production
            if (repository.findByUsername("testuser") == null) {
                AppUser user = new AppUser();
                user.setUsername("testuser");
                user.setPasswordHash(passwordEncoder.encode("password123"));
                repository.save(user);
                System.out.println("--- Test user 'testuser' with password 'password123' created! ---");
            }
        };
    }
}
