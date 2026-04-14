package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.model.PasswordResetToken;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.repository.PasswordResetTokenRepo;
import fi.haagahelia.project.service.EmailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PasswordResetController {

    private final AppUserRepo userRepo;
    private final PasswordResetTokenRepo tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetController(AppUserRepo userRepo, PasswordResetTokenRepo tokenRepo, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        AppUser user = userRepo.findByEmail(email);

        if (user != null) {
            PasswordResetToken oldToken = tokenRepo.findByUser(user); // we check if there is already a token for the user, if there is we delete it, because we will create a new one.
            if (oldToken != null) {
                tokenRepo.delete(oldToken);
            }

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(); // we create a new token for the user
            resetToken.setToken(token); // we set the token string to the generated UUID
            resetToken.setUser(user); // we associate the token with the user
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // we set the expiry date to 15 minutes from now
            tokenRepo.save(resetToken); // we save the token to the database

            String resetUrl = "http://localhost:8080/reset-password?token=" + token; // we create the reset URL that will be sent to the user, this URL contains the token as a query parameter, which will be used to identify the token when the user clicks the link in the email.
            
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetUrl); // we use the email service to send the password reset email to the user, this method will need to be implemented in the EmailService class, and it should use JavaMailSender to send the email.
            } catch (Exception e) {
                System.out.println("Error sending email: " + e.getMessage());
            }
        
        }
        model.addAttribute("message", "If an account with that email exists, a password reset link has been sent."); // we add a message to the model to inform the user that if an account with that email exists, a password reset link has been sent, this is important for security reasons, because we don't want to reveal whether an email is registered or not.
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        
        PasswordResetToken resetToken = tokenRepo.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) { // we check if the token is valid and not expired, if it's not valid or expired, we show an error message to the user.
            model.addAttribute("error", "Invalid or expired token.");
            return "reset-password";
        }

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token);

                if (resetToken == null) {
            return "redirect:/forgot-password?error=Invalid token. Please request a new link."; // If the token is not found, we redirect the user back to the forgot password page with an error message.
        }


        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) { // we check if the token is valid and not expired, if it's not valid or expired, we show an error message to the user.
            model.addAttribute("error", "Invalid or expired token.");
            return "reset-password";
        }


        if (!password.equals(confirmPassword)) {
            // Redirect back to the reset form, and keep the token in the URL!
            return "redirect:/reset-password?token=" + token + "&error=Passwords do not match!"; // If the password and confirm password do not match, we redirect the user back to the reset password page with the token in the URL and an error message.
        }

        if (password.length() < 8) {
            return "redirect:/reset-password?token=" + token + "&error=Password must be at least 8 characters."; // If the password is too short, we redirect the user back to the reset password page with the token in the URL and an error message.
        }

        AppUser user = resetToken.getUser(); // we get the user associated with the token, this is the user whose password we will reset.
        user.setPasswordHash(passwordEncoder.encode(password)); // we encode the new password using the password encoder and set it as the user's new password hash, this will ensure that the password is stored securely in the database.
        userRepo.save(user); // we save the updated user to the database, this will update the user's password hash with the new password.
        tokenRepo.delete(resetToken); // we delete the token from the database, this is important for security reasons, because we don't want the token to be used again after the password has been reset.

        return "redirect:/login?message=Password reset successful. Please log in.";
    }
}
