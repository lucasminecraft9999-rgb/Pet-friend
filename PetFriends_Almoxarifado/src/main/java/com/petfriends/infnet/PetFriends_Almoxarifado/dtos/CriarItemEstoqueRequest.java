package com.petfriends.infnet.PetFriends_Almoxarifado.dtos;

public record CriarItemEstoqueRequest(
        String sku ,
        int quantidadeInicial
) {
}
