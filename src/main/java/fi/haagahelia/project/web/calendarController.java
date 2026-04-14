package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import fi.haagahelia.project.model.Event;
import fi.haagahelia.project.config.EncryptionUtil;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.service.CalendarService;


@Controller
public class calendarController {

    private final CalendarService calendarService;
    private final AppUserRepo appUserRepo;
    private final EncryptionUtil encryptionUtil;

    public calendarController(CalendarService calendarService, AppUserRepo appUserRepo, EncryptionUtil encryptionUtil) {
        this.calendarService = calendarService;
        this.appUserRepo = appUserRepo;
        this.encryptionUtil = encryptionUtil;
    }

    @GetMapping("/calendar")
    public String showChalendar(Model model, Principal principal) {

        // get username of logged in user
        String currentUsername = principal.getName();

        // find the user in database
        AppUser currentUser = appUserRepo.findByUsername(currentUsername);

        //get attatched moodle URL
        String userMoodleUrl = currentUser.getMoodleUrl();

        //Decrypt the Url
        String decryptedUrl = encryptionUtil.decrypt(userMoodleUrl);

        //grab the events from the list.
        List<Event> upcomingEvents = calendarService.fetchEvents(decryptedUrl);

        model.addAttribute("events", upcomingEvents);

        return "calendar";
    }
    

}
