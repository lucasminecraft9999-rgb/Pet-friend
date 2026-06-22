package com.infnet.PetFriends_Pedidos.events;

import java.util.UUID;

public record PedidoCriadoTransporteEvent(UUID pedidoId , EnderecoDTO enderecoDTO ) {
    public record EnderecoDTO(String logradouro, String numero, String cidade, String uf, String cep) {}
}
