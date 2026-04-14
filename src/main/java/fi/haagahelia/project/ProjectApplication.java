package fi.haagahelia.project;

import fi.haagahelia.project.model.Event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fi.haagahelia.project.service.CalendarService;

import java.util.List;

@SpringBootApplication
public class ProjectApplication {


	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);

		String calUrl = "";

		CalendarService calendarService = new CalendarService();
		List<Event> events = calendarService.fetchEvents(calUrl);

	}

}
