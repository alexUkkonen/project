package fi.haagahelia.project.repository;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.project.model.AppUser;

public interface AppUserRepo extends CrudRepository<AppUser, Long> {

    AppUser findByUsername(String username); // Made the AppUser in to a repo.
}
