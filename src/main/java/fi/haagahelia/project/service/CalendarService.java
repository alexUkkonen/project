package fi.haagahelia.project.service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CalendarService {

    public List<Event> fetchEvents(String calUrl) {
        // TODO: 1. Fetch the data using Java's built-in HttpClient

        HttpClient client = HttpClient.newHttpClient(); // Create an HttpClient instance
        HttpRequest request = HttpRequest.newBuilder() // Build an HttpRequest with the moodle calendar URL
                .uri(URI.create(calUrl))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // Send the request and get the response as a String (This includes status code and body)

        // TODO: 2. Parse the iCal data using the Biweekly library
        // TODO: 3. Loop through events (assignments)
        return new ArrayList<>(); // Return an empty list for now
    }

}
