package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;

import java.security.Principal;

import fi.haagahelia.project.config.EncryptionUtil;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.repository.PasswordResetTokenRepo;
import fi.haagahelia.project.model.PasswordResetToken;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class UserController {

    private final AppUserRepo repository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;
    private final PasswordResetTokenRepo tokenRepo;

    public UserController(AppUserRepo repository, PasswordEncoder passwordEncoder, EncryptionUtil encryptionUtil, PasswordResetTokenRepo tokenRepo) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionUtil = encryptionUtil;
        this.tokenRepo = tokenRepo;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
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
            return "redirect:/register?error=Passwords do nor match!"; // If the password and confirm password do not match, we redirect the user back to the registration page with an error message.
        }

        //Password strength check. Is it long enough?
        if (password.length() < 8) {
            return "redirect:/register?error=passowrd must be at least 8 characters long"; // If the password is too short, we redirect the user back to the registration page with an error message.
        }
    
        //Check to see if user is taken
        if (repository.findByUsername(username) != null) {
            return "redirect:/register?error=Username taken"; // If the username is already taken, we redirect the user back to the registration page with an error message.
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        newUser.setEmail(email);

        // Encrypting the URL
        newUser.setMoodleUrl(encryptionUtil.encrypt(moodleUrl));

        // Hashing password
        newUser.setPasswordHash(passwordEncoder.encode(password));

        repository.save(newUser);
    
        // send back to login screen
        return "redirect:/login";
    }

    @GetMapping("/settings") //adding a settings page
    public String showSettings(Model model, Principal principal) {
        
        AppUser user = repository.findByUsername(principal.getName()); //find the current user

        String currentUrl = ""; //init cuurentUrl
        if (user.getMoodleUrl() != null && !user.getMoodleUrl().isEmpty()) { //we make shure the URL isn't empty or null
            currentUrl = encryptionUtil.decrypt(user.getMoodleUrl()); //we decrypt the Url of the user in the database and show it in the currentUrl string
        }

        model.addAttribute("currnetUrl", currentUrl);
        return "settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam String newMoodleUrl, Principal principal) {
        
        AppUser user = repository.findByUsername(principal.getName());

        user.setMoodleUrl(encryptionUtil.encrypt(newMoodleUrl)); // we encrypt the new URL and set it as the user's new moodle URL, this will ensure that the URL is stored securely in the database.
        repository.save(user);
        
        return "redirect:/calendar";
    }
    
    @PostMapping("/settings/delete")
    public String deleteAccount(Principal principal, HttpServletRequest request) throws ServletException {
        
        AppUser user = repository.findByUsername(principal.getName());

        PasswordResetToken token = tokenRepo.findByUser(user); // Find any password reset tokens associated with the user
        if (token != null) {
            tokenRepo.delete(token); // Delete any associated password reset tokens for the user to prevent crashes after the user account is deleted
        }

        repository.delete(user); // Delete the user account from the database

        request.logout(); // Log out the user after deleting the account
        
        return "redirect:/login?accountDeleted"; // Redirect to the login page with a message indicating the account was deleted
    }
    
    
    
}
