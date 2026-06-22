package com.petfriends.infnet.PetFriends_Almoxarifado.service;

import com.petfriends.infnet.PetFriends_Almoxarifado.Repositorio.ItemEstoqueRepository;
import com.petfriends.infnet.PetFriends_Almoxarifado.model.ItemEstoque;
import com.petfriends.infnet.PetFriends_Almoxarifado.model.Quantidade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ItemEstoqueService {

    private final ItemEstoqueRepository repository;

    public ItemEstoque criar(String sku, int quantidadeInicial) {
        ItemEstoque item = ItemEstoque.builder()
                .id(UUID.randomUUID())
                .sku(sku)
                .disponivel(Quantidade.of(quantidadeInicial))
                .reservada(Quantidade.zero())
                .build();
        return repository.save(item);
    }

    public ItemEstoque buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));
    }

    public ItemEstoque buscarPorSku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado: " + sku));
    }

    public List<ItemEstoque> listarTodos() {
        return repository.findAll();
    }

    public ItemEstoque reservar(String sku, int quantidade) {
        ItemEstoque item = buscarPorSku(sku);
        item.reservar(Quantidade.of(quantidade));
        return repository.save(item);
    }

}
