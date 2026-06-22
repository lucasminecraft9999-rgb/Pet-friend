package com.petfriends.infnet.PetFriends_Transporte.dtos;

import java.util.UUID;

public record CriarEntregaRequest(
        UUID pedidoId,
        String logradouro,
        String numero,
        String cidade,
        String uf,
        String cep
) {}
