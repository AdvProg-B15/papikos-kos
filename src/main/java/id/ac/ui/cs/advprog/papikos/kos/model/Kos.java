package id.ac.ui.cs.advprog.papikos.kos.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Kos")
@Table(name = "kos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Kos {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "uuid", name = "id", nullable = false)
    private UUID id;

    @Column(columnDefinition = "uuid", name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "num_rooms", nullable = false)
    private Integer numRooms;

    @Column(name = "monthly_rent_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRentPrice;

    @Column(name = "is_listed", nullable = false)
    private Boolean isListed = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kos kos = (Kos) o;
        return Objects.equals(id, kos.id) &&
                Objects.equals(ownerUserId, kos.ownerUserId) &&
                Objects.equals(name, kos.name) &&
                Objects.equals(address, kos.address) &&
                Objects.equals(description, kos.description) &&
                Objects.equals(numRooms, kos.numRooms) &&
                Objects.equals(monthlyRentPrice, kos.monthlyRentPrice) &&
                Objects.equals(isListed, kos.isListed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerUserId, name, address, description, numRooms, monthlyRentPrice, isListed);
    }
}
