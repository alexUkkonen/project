package fi.haagahelia.project;

import fi.haagahelia.project.config.EncryptionUtil;
import fi.haagahelia.project.model.AppUser;
import fi.haagahelia.project.model.Event;
import fi.haagahelia.project.repository.AppUserRepo;
import fi.haagahelia.project.service.CalendarService;
import fi.haagahelia.project.web.calendarController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CalendarControllerTest {

    @Test
    void showChalendar_populatesModelWithEventsAndReturnsCalendarView() {
        CalendarService calendarService = mock(CalendarService.class);
        AppUserRepo repo = mock(AppUserRepo.class);
        EncryptionUtil encryptionUtil = mock(EncryptionUtil.class);

        AppUser user = new AppUser();
        user.setMoodleUrl("encrypted-url");
        when(repo.findByUsername("john")).thenReturn(user);
        when(encryptionUtil.decrypt("encrypted-url")).thenReturn("https://moodle.example.com");

        Event event = new Event();
        event.setTitle("Assignment 1");
        when(calendarService.fetchEvents("https://moodle.example.com")).thenReturn(List.of(event));

        calendarController controller = new calendarController(calendarService, repo, encryptionUtil);
        Model model = mock(Model.class);
        Principal principal = () -> "john";

        String view = controller.showChalendar(model, principal);

        assertThat(view).isEqualTo("calendar");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(eq("events"), captor.capture());
        assertThat(captor.getValue()).containsExactly(event);
    }
}
