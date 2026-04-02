package fi.haagahelia.project.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.List;

import fi.haagahelia.project.model.Event;
import fi.haagahelia.project.service.*;


@Controller
public class calendarController {

    private final CalendarService calendarService;

    public calendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/calendar")
    public String showChalendar(Model model) {

        // TODO: change to a class bound to user.
        String userMoodleUrl = "https://hhmoodle.haaga-helia.fi/calendar/export_execute.php?userid=86119&authtoken=8ef7ead9d6c135dae7ef931f57bd8768ac262d82&preset_what=courses&preset_time=custom";

        List<Event> upcomingEvents = calendarService.fetchEvents(userMoodleUrl);

        model.addAttribute("events", upcomingEvents);

        return "calendar";
    }
    

}
