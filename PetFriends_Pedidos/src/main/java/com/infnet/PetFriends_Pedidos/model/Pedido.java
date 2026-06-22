package com.infnet.PetFriends_Pedidos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    public enum Status { NOVO, FECHADO, EM_PREPARACAO, CANCELADO, EM_TRANSITO, ENTREGUE, DEVOLVIDO, EXTRAVIADO }

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "logradouro")
    private String logradouro;
    private String numero;
    private String cidade;
    private String uf;
    private String cep;

    @ElementCollection
    @CollectionTable(name = "pedido_itens", joinColumns = @JoinColumn(name = "pedido_id"))
    private List<ItemPedido> itens;

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemPedido {
        private String sku;
        private int quantidade;
    }

    public void fechar() {
        this.status = Status.FECHADO;
    }

    public void despachar() {
        this.status = Status.EM_TRANSITO;
    }
}
