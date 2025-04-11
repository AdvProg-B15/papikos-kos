package id.ac.ui.cs.advprog.papikos.kos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity(name = "Pemilik")
@Table(name = "pemilik", schema = "papikos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Pemilik {
    @Id
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "email", unique = true)
    private Integer email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pemilik pemilik = (Pemilik) o;
        return Objects.equals(id, pemilik.id) && Objects.equals(name, pemilik.name) && Objects.equals(address, pemilik.address) &&
            Objects.equals(email, pemilik.email);
    }
}
