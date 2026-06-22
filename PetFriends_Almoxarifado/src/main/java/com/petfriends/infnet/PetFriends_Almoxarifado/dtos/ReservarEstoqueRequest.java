package com.petfriends.infnet.PetFriends_Almoxarifado.dtos;

public record ReservarEstoqueRequest(
        String sku ,
        int quantidade
) {
}
