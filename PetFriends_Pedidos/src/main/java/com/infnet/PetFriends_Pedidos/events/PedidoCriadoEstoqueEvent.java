package com.infnet.PetFriends_Pedidos.events;

import java.util.List;
import java.util.UUID;

public record PedidoCriadoEstoqueEvent(UUID pedidoId, List<ItemPedido> itens) {
    public record ItemPedido(String sku, int quantidade) {}
}
