package id.ac.ui.cs.advprog.papikos.kos.listener;

import id.ac.ui.cs.advprog.papikos.kos.dto.RentalEvent;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KosEventListenerTest {

    @Mock
    private KosService kosService;

    @InjectMocks
    private KosEventListener kosEventListener;

    @Test
    void handleRentalCreatedEvent_logsAndProcessesEvent() {
        RentalEvent event = new RentalEvent();
        String kosId = UUID.randomUUID().toString();
        // String rentalId = UUID.randomUUID().toString(); // rentalId is not used by KosEventListener
        event.setKosId(kosId);
        // event.setRentalId(rentalId); // Not strictly needed for this listener's current logic
        event.setUserId(UUID.randomUUID().toString());
        event.setPrice(new BigDecimal("1200000"));
        event.setTimestamp("2024-05-20T10:00:00Z");

        kosEventListener.handleRentalCreatedEvent(event);

        verify(kosService, times(1)).updateOccupiedRooms(UUID.fromString(kosId), 1);
    }
}
