package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.security.Principal;

import fi.haagahelia.project.config.EncryptionUtil;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;



@Controller
public class UserController {

    private final AppUserRepo repository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;

    public UserController(AppUserRepo repository, PasswordEncoder passwordEncoder, EncryptionUtil encryptionUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionUtil = encryptionUtil;
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

        user.setMoodleUrl(encryptionUtil.encrypt(newMoodleUrl));
        repository.save(user);
        
        return "redirect:/calendar";
    }
    
    
    
    
}
