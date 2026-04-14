package fi.haagahelia.project.repository;

import org.springframework.data.repository.CrudRepository;
import fi.haagahelia.project.model.PasswordResetToken;
import fi.haagahelia.project.model.AppUser;

public interface PasswordResetTokenRepo extends CrudRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token); // we tell PasswordResetToken how to find token by token string, this is used in password reset process.
    PasswordResetToken findByUser(AppUser user); // we tell PasswordResetToken how to find token by user, this is used in password reset process to check if there is already a token for the user.

}
