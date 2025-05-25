package id.ac.ui.cs.advprog.papikos.kos.listener;

import id.ac.ui.cs.advprog.papikos.kos.dto.RentalEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KosEventListenerTest {

    // Spy the component to verify its method is called, but allow original method to execute for console output check
    @Spy
    @InjectMocks
    private KosEventListener kosEventListener;

    @Test
    void handleRentalCreatedEvent_logsAndProcessesEvent() throws InterruptedException {
        RentalEvent event = new RentalEvent();
        String kosId = UUID.randomUUID().toString();
        String rentalId = UUID.randomUUID().toString();
        event.setKosId(kosId);
        event.setRentalId(rentalId);
        event.setUserId(UUID.randomUUID().toString());
        event.setPrice(new BigDecimal("1200000"));
        event.setTimestamp("2024-05-20T10:00:00Z");

        // Capture System.out output
        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));

        // Call the method that should be triggered by RabbitListener
        kosEventListener.handleRentalCreatedEvent(event);

        // Restore System.out
        System.setOut(originalOut);

        // Verify the method was called (useful if it were mocked, for Spy it confirms execution)
        // verify(kosEventListener, times(1)).handleRentalCreatedEvent(event);
        // For a spy, you might want to verify a specific internal action if it had one,
        // but here we check the console output as a proxy for its work.

        String consoleOutput = bos.toString();
        assertTrue(consoleOutput.contains("KosService: Received rental.created event: " + event.toString()));
        assertTrue(consoleOutput.contains("KosService: Storing booking for kosId: " + kosId));
        assertTrue(consoleOutput.contains("rentalId: " + rentalId));
        assertTrue(consoleOutput.contains("KosService: Booking for rentalId " + rentalId + " processed."));

        // If there were actual service calls (e.g., to a database), you would mock those and verify them.
        // For example, if it called a kosBookingService.save(booking), you'd verify that.
    }
}

