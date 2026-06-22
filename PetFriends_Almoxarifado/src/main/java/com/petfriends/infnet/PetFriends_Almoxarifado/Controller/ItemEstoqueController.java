package com.petfriends.infnet.PetFriends_Almoxarifado.Controller;

import com.petfriends.infnet.PetFriends_Almoxarifado.dtos.CriarItemEstoqueRequest;
import com.petfriends.infnet.PetFriends_Almoxarifado.dtos.ReservarEstoqueRequest;
import com.petfriends.infnet.PetFriends_Almoxarifado.model.ItemEstoque;
import com.petfriends.infnet.PetFriends_Almoxarifado.service.ItemEstoqueService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itens-estoque")
@AllArgsConstructor
public class ItemEstoqueController {

    private final ItemEstoqueService service;

    @PostMapping
    public ResponseEntity<ItemEstoque> criar(@RequestBody CriarItemEstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(request.sku(), request.quantidadeInicial()));
    }

    @GetMapping
    public ResponseEntity<List<ItemEstoque>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemEstoque> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ItemEstoque> buscarPorSku(@PathVariable String sku) {
        return ResponseEntity.ok(service.buscarPorSku(sku));
    }

    @PostMapping("/reservar")
    public ResponseEntity<ItemEstoque> reservar(@RequestBody ReservarEstoqueRequest request) {
        return ResponseEntity.ok(service.reservar(request.sku(), request.quantidade()));
    }
}
