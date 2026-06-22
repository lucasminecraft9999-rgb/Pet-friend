package com.petfriends.infnet.PetFriends_Almoxarifado.Repositorio;

import com.petfriends.infnet.PetFriends_Almoxarifado.model.ItemEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataItemEstoqueRepository extends JpaRepository<ItemEstoque , UUID> {
    Optional<ItemEstoque> findBySku(String sku);
}
