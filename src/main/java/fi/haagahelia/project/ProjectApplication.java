package fi.haagahelia.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fi.haagahelia.project.service.CalendarService;

import java.util.List;

@SpringBootApplication
public class ProjectApplication {

	calUrl = "https://hhmoodle.haaga-helia.fi/calendar/export_execute.php?userid=86119&authtoken=8ef7ead9d6c135dae7ef931f57bd8768ac262d82&preset_what=courses&preset_time=custom";

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);


		CalendarService calendarService = new CalendarService();
		List<Event> events = calendarService.fetchEvents(calUrl);

	}

}
