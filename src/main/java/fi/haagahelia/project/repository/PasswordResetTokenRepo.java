package fi.haagahelia.project.repository;

import org.springframework.data.repository.CrudRepository;
import fi.haagahelia.project.model.PasswordResetToken;
import fi.haagahelia.project.model.AppUser;

public interface PasswordResetTokenRepo extends CrudRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(AppUser user);

}
