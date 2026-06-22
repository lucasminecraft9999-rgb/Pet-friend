package com.petfriends.infnet.PetFriends_Transporte.repository;

import com.petfriends.infnet.PetFriends_Transporte.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataEntregaRepository extends JpaRepository<Entrega , UUID> {
    Optional<Entrega> findByPedidoId(UUID pedidoId);
}
