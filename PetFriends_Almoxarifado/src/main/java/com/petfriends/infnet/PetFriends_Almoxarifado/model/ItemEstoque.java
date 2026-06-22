
package com.petfriends.infnet.PetFriends_Almoxarifado.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "item_estoque")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEstoque {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "qtd_disponivel"))
    private Quantidade disponivel;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "qtd_reservada"))
    private Quantidade reservada;

    public void reservar(Quantidade quantidade) {
        if (quantidade.maiorQue(disponivel))
            throw new IllegalStateException("Estoque insuficiente para o SKU " + sku);
        disponivel = disponivel.subtrair(quantidade);
        reservada = reservada.somar(quantidade);
    }
}