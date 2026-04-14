package fi.haagahelia.project.model;

import jakarta.persistence.*;

@Entity
public class AppUser { // named AppUser because User is used by some SQL stuff.

    @Id //We create the table containing the users information.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash; 

    @Column(nullable = false)
    private String email;

    @Column(length = 1000)
    private String moodleUrl;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMoodleUrl() {
        return moodleUrl;
    }

    public void setMoodleUrl(String moodleURL) {
        this.moodleUrl = moodleURL;
    }

    public AppUser() { // Create getters and setters
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

}
