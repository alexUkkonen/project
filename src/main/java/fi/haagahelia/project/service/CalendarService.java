package fi.haagahelia.project.service;

import fi.haagahelia.project.model.Event;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import biweekly.ICalendar;
import biweekly.component.VEvent;

@Service
public class CalendarService {

    public List<Event> fetchEvents(String calUrl) {

        List<Event> myEvents = new ArrayList<>(); // Create a list to hold our custom Event objects, we will convert the VEvent objects from Biweekly into our own Event class.

        try {    
            HttpClient client = HttpClient.newHttpClient(); // Create an HttpClient instance
            HttpRequest request = HttpRequest.newBuilder() // Build an HttpRequest with the moodle calendar URL
                    .uri(URI.create(calUrl)) // URI is the bucket term that encompases URL and URN, but since we are using HttpClient it will almoust allways be a URL.
                    .build(); // This finishes the building of the request.
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // Send the request and get the response as a String (This includes status code and body)
        
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

            List<VEvent> vEvents = ical.getEvents(); // Get the list of events from the calendar

            for (VEvent vEvent : vEvents) {
                String title = vEvent.getSummary() != null ? vEvent.getSummary().getValue() : "Untitled Event"; // Get the title of the event, if it's null, use "Untitled Event" as a default title.
                java.util.Date dueDate = vEvent.getDateStart() != null ? vEvent.getDateStart().getValue() : null; // Get the start date of the event, if it's null, use null as the due date.

                if (title.toLowerCase().contains("attendance")) { //if the title contains "attendance", we skip this event, as it's not a deadline.
                    continue;
                }

                // Create the event and add it to the list!
                if (dueDate != null) {
                    Event myEvent = new Event(); // Create a new instance of our custom Event class
                    myEvent.setTitle(title); // Set the title of our Event to the title we got from the VEvent
                    myEvent.setDueDate(dueDate); // Set the due date of our Event to the start date we got from the VEvent (Moodle calendar events use the start date as the deadline)
                    myEvents.add(myEvent); // Add our Event to the list of events we will return
                }
            }

        } catch (Exception e) {            

            

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myEvents; // Return a list.
    }

}
