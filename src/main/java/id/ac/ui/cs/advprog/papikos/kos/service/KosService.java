package id.ac.ui.cs.advprog.papikos.kos.service;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.Pemilik;
import id.ac.ui.cs.advprog.papikos.kos.repository.KosRepository;
import id.ac.ui.cs.advprog.papikos.kos.repository.PemilikRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KosService {
    private final KosRepository kosRepository;
    private final PemilikRepository pemilikRepository;

    public KosService(KosRepository kosRepository, PemilikRepository pemilikRepository) {
        this.kosRepository = kosRepository;
        this.pemilikRepository = pemilikRepository;
    }

    public List<Kos> findAllKos() {
        return kosRepository.findAll();
    }

    public List<Pemilik> findAllPemilik() {
        return pemilikRepository.findAll();
    }
}
