package id.ac.ui.cs.advprog.papikos.kos.listener;

import id.ac.ui.cs.advprog.papikos.kos.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.papikos.kos.dto.RentalEvent;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KosEventListener {

    private final KosService kosService;

    public KosEventListener(KosService kosService) {
        this.kosService = kosService;
    }

    @RabbitListener(queues = RabbitMQConfig.KOS_QUEUE_NAME)
    public void handleRentalCreatedEvent(RentalEvent event) {
        kosService.updateOccupiedRooms(UUID.fromString(event.getKosId()), 1);
    }
}