package fi.haagahelia.project.service;

import fi.haagahelia.project.model.Event;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import biweekly.ICalendar;
import biweekly.component.VEvent;

public class CalendarService {

    public List<Event> fetchEvents(String calUrl) {

        try {    
            HttpClient client = HttpClient.newHttpClient(); // Create an HttpClient instance
            HttpRequest request = HttpRequest.newBuilder() // Build an HttpRequest with the moodle calendar URL
                    .uri(URI.create(calUrl)) // URI is the bucket term that encompases URL and URN, but since we are using HttpClient it will almoust allways be a URL.
                    .build(); // This finishes the building of the request.
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // Send the request and get the response as a String (This includes status code and body)

        // TODO: 2. Parse the iCal data using the Biweekly library
        
            if (response.statusCode() != 200) { // 200 is the status code for a successful HTTP request, if it's not 200, we print an error message and return an empty list.
                System.out.println("Failed to fetch calendar data. Status code: " + response.statusCode());
                return new ArrayList<>(); // Return an empty list if the request failed
            }

            String icalData = response.body(); // Get the body of the response, which should be the iCal data as a String
            ICalendar ical = biweekly.Biweekly.parse(icalData).first(); // Parse the iCal data using Biweekly and get the first calendar (there should only be one)

            if (ical == null) { // If parsing failed and we didn't get a calendar, print an error message and return an empty list.
                System.out.println("No calendar data found in the response.");
                return new ArrayList<>(); // Return an empty list if parsing failed
            }
        // TODO: 3. Loop through events (assignments)

            List<Event> myEvents = new ArrayList<>(); // Create a list to hold our custom Event objects, we will convert the VEvent objects from Biweekly into our own Event class.

            List<VEvent> vEvents = ical.getEvents(); // Get the list of events from the calendar

            System.out.println("Uppcoming deadlines:"); // Print a header for the upcoming deadlines
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (VEvent vEvent : vEvents) {
                String title = vEvent.getSummary().getValue(); // Get the title of the event
                Date dueDate = vEvent.getDateStart().getValue(); // Get the start date of the event, which is the due date for assignments

                Event myEvent = new Event();
                myEvent.setTitle(title);
                myEvent.setDueDate(dueDate);
                myEvents.add(myEvent); // Add the event to our list of custom Event objects

                System.out.println("Assignment: " + title);
                System.out.println("Due: " + formatter.format(dueDate)); // This is proof of concept showing that the iCal works.
                System.out.println("-------------------------"); // TODO: Remove this later, it's just for testing purposes
                // TODO: make GUI to show these events in a nice way, maby a navigatable calendar view, or a list of upcoming deadlines.
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // Return an empty list for now
    }

}
