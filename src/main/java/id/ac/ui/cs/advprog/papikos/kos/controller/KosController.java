package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.response.ApiResponse;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class KosController {
    private final KosService kosService;

    public KosController(KosService kosService) {
        this.kosService = kosService;
    }

    public ResponseEntity<ApiResponse<List<Kos>>> getAllKos() {
        List<Kos> kos = new ArrayList<>(); // TODO: Change with real implementation

        // Apply builder
        ApiResponse<List<Kos>> res = ApiResponse.<List<Kos>>builder()
                .status(HttpStatus.OK)
                .message("fetched")
                .data(kos)
                .build();

        return ResponseEntity.ok(res);
    }
}
