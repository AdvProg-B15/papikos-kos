package id.ac.ui.cs.advprog.papikos.kos.repository;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KosRepository extends JpaRepository<Kos, UUID> {
    List<Kos> findKosByOwnerUserId(UUID ownerUserId);
    List<Kos> findKosByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String address, String description);
}
