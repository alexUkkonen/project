package fi.haagahelia.project.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepo repository;

    public UserDetailsServiceImpl(AppUserRepo repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = repository.findByUsername(username); // This enables the program to find the user using the username.

        if (appUser == null) { // If there is no user throws an error.
            throw new UsernameNotFoundException("Username not found.");
        }

        return User.builder() // We build the User
            .username(appUser.getUsername())
            .password(appUser.getPasswordHash())
            .roles("USER")
            .build();
    }

}
