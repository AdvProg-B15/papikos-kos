package id.ac.ui.cs.advprog.papikos.kos.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RentalEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String kosOwnerId;
    private String kosId;
    private String rentalId;
    private BigDecimal price;
    private String timestamp;
}
