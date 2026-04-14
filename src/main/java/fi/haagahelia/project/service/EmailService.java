package fi.haagahelia.project.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("noreply@gmail.com"); // makes us apear as noreply@gmail.com
        message.setTo(toEmail);
        message.setSubject("Password Reset Request"); //sets title of the email
        message.setText("Hello,\n\n" + //sets the content of the email
                "You have requested to reset your password. Please click the link below to choose a new password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes.\n\n" +
                "If you did not request this, please ignore this email.");
        mailSender.send(message); //sends the email
    }

}
