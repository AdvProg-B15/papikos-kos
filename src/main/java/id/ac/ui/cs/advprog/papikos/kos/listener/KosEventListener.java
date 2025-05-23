package id.ac.ui.cs.advprog.papikos.kos.listener;

import id.ac.ui.cs.advprog.papikos.kos.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.papikos.kos.dto.RentalEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class KosEventListener {

    @RabbitListener(queues = RabbitMQConfig.KOS_QUEUE_NAME)
    public void handleRentalCreatedEvent(RentalEvent event) {
        System.out.println("KosService: Received rental.created event: " + event);
        // Logic to store the booking for the specific kos (property)
        // e.g., save to a KosBookingEntity in a database
        System.out.println("KosService: Storing booking for kosId: " + event.getKosId() +
                ", rentalId: " + event.getRentalId());
        // Simulate saving
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        System.out.println("KosService: Booking for rentalId " + event.getRentalId() + " processed.");
    }
}