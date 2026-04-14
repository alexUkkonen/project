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
            PasswordResetToken oldToken = tokenRepo.findByUser(user);
            if (oldToken != null) {
                tokenRepo.delete(oldToken);
            }

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
            tokenRepo.save(resetToken);

            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
            } catch (Exception e) {
                System.out.println("Error sending email: " + e.getMessage());
            }
        
        }
        model.addAttribute("message", "If an account with that email exists, a password reset link has been sent.");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        
        PasswordResetToken resetToken = tokenRepo.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token.");
            return "reset-password";
        }

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword, Model model) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token);

                if (resetToken == null) {
            return "redirect:/forgot-password?error=Invalid token. Please request a new link.";
        }


        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Invalid or expired token.");
            return "reset-password";
        }


        if (!password.equals(confirmPassword)) {
            // Redirect back to the reset form, and keep the token in the URL!
            return "redirect:/reset-password?token=" + token + "&error=Passwords do not match!";
        }

        if (password.length() < 8) {
            return "redirect:/reset-password?token=" + token + "&error=Password must be at least 8 characters.";
        }

        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepo.save(user);
        tokenRepo.delete(resetToken);

        return "redirect:/login?message=Password reset successful. Please log in.";
    }
}
