package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import fi.haagahelia.project.model.Event;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.service.CalendarService;


@Controller
public class calendarController {

    private final CalendarService calendarService;
    private final AppUserRepo appUserRepo;

    public calendarController(CalendarService calendarService, AppUserRepo appUserRepo) {
        this.calendarService = calendarService;
        this.appUserRepo = appUserRepo;
    }

    @GetMapping("/calendar")
    public String showChalendar(Model model, Principal principal) {

        // get username of logged in user
        String currentUsername = principal.getName();

        // find the user in database
        AppUser currentUser = appUserRepo.findByUsername(currentUsername);

        //get attatched moodle URL
        String userMoodleUrl = currentUser.getMoodleUrl();

        //grab the events from the list.
        List<Event> upcomingEvents = calendarService.fetchEvents(userMoodleUrl);

        model.addAttribute("events", upcomingEvents);

        return "calendar";
    }
    

}
