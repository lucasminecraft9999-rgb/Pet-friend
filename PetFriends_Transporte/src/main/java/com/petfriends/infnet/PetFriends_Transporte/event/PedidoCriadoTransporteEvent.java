package com.petfriends.infnet.PetFriends_Transporte.event;

import java.util.UUID;

public record PedidoCriadoTransporteEvent(
        UUID pedidoId ,
        EnderecoDTO enderecoEntrega
) {
    public record EnderecoDTO(String logradouro , String numero , String cidade , String uf , String cep) {
    }
}
