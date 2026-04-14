package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;

import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;


@Controller
public class UserController {

    private final AppUserRepo repository;
    private final PasswordEncoder passwordEncoder;

    public UserController(AppUserRepo repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForum() {
        return "register";
    }

    @PostMapping("/register")
    public String saveNewUser( @RequestParam String username, 
            @RequestParam String email, 
            @RequestParam String moodleUrl,
            @RequestParam String password, 
            @RequestParam String confirmPassword) {

        //Do passowrds match?
        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=Passwords do nor match!";
        }

        //Password strength check. Is it long enough?
        if (password.length() < 8) {
            return "redirect:/register?error=passowrd must be at least 8 characters long";
        }
    
        //Check to see if user is taken
        if (repository.findByUsername(username) != null) {
            return "redirect:/register?error username taken";
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setMoodleUrl(moodleUrl);

        // Hashing password
        newUser.setPasswordHash(passwordEncoder.encode(password));

        repository.save(newUser);
    
        // send back to login screen
        return "redirect:/login";
    }
    
    
}
