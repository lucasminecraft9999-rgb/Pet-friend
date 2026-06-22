package com.infnet.PetFriends_Pedidos.controller;

import com.infnet.PetFriends_Pedidos.model.Pedido;
import com.infnet.PetFriends_Pedidos.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(service.criar(pedido));
    }

    @PostMapping("/{id}/fechar")
    public ResponseEntity<Pedido> fechar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.fechar(id));
    }
}
