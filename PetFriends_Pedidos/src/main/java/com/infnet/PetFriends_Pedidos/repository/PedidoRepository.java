package com.infnet.PetFriends_Pedidos.repository;

import com.infnet.PetFriends_Pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido , UUID> {
}
