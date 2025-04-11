package id.ac.ui.cs.advprog.papikos.kos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity(name = "Kos")
@Table(name = "kos", schema = "papikos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Kos {
    @Id
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "address")
    private String address;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Kos kos = (Kos) o;
        return Objects.equals(id, kos.id) && Objects.equals(name, kos.name) && Objects.equals(price, kos.price) &&
            Objects.equals(address, kos.address) && Objects.equals(totalRooms, kos.totalRooms);
    }
}
