package fi.haagahelia.project.repository;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.project.model.AppUser;

public interface AppUserRepo extends CrudRepository<AppUser, Long> {

    AppUser findByUsername(String username); // we tell AppUser how to find user by username, this is used in authentication process.

    AppUser findByEmail(String email); //we tell AppUser how to find user by email, this is used in password reset process.
}
