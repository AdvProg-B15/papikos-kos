package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;

import java.util.List;
import java.util.UUID;

public class KosServiceImpl implements KosService {
    @Override
    public Kos createKos(Kos kos, UUID ownerUserId) {
        return null;
    }

    @Override
    public List<Kos> findAllKos() {
        return List.of();
    }

    @Override
    public Kos findKosById(UUID kosId) {
        return null;
    }

    @Override
    public List<Kos> findKosByOwnerUserId(UUID ownerUserId) {
        return List.of();
    }

    @Override
    public Kos updateKos(UUID kosId, Kos updatedKosData, UUID requestingUserId) {
        return null;
    }

    @Override
    public void deleteKos(UUID kosId, UUID requestingUserId) {

    }

    @Override
    public List<Kos> searchKos(String keyword) {
        return List.of();
    }
}
