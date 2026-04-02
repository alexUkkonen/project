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
        AppUser appUser = repository.findByUsername(username);

        if (appUser == null) {
            throw new UsernameNotFoundException("Username not found.");
        }

        return User.builder()
            .username(appUser.getUsername())
            .password(appUser.getPasswordHash())
            .roles("USER")
            .build();
    }

}
