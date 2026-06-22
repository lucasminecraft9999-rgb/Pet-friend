package com.petfriends.infnet.PetFriends_Transporte.controller;

import com.petfriends.infnet.PetFriends_Transporte.dtos.CriarEntregaRequest;
import com.petfriends.infnet.PetFriends_Transporte.model.Endereco;
import com.petfriends.infnet.PetFriends_Transporte.model.Entrega;
import com.petfriends.infnet.PetFriends_Transporte.service.EntregaService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/entregas")
@AllArgsConstructor
public class EntregaController {

    private final EntregaService service;


    @PostMapping
    public ResponseEntity<Entrega> criar(@RequestBody CriarEntregaRequest request) {
        Endereco endereco = new Endereco(
                request.logradouro(),
                request.numero(),
                request.cidade(),
                request.uf(),
                request.cep()
        );
        Entrega entrega = service.criar(request.pedidoId(), endereco);
        return ResponseEntity.status(HttpStatus.CREATED).body(entrega);
    }

    @GetMapping
    public ResponseEntity<List<Entrega>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrega> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Entrega> buscarPorPedidoId(@PathVariable UUID pedidoId) {
        return ResponseEntity.ok(service.buscarPorPedidoId(pedidoId));
    }

    @PostMapping("/{id}/despachar")
    public ResponseEntity<Entrega> despachar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.despachar(id));
    }
}