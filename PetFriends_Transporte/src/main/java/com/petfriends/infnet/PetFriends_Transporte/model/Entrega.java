package com.petfriends.infnet.PetFriends_Transporte.model;

import com.petfriends.infnet.PetFriends_Transporte.model.Enum.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "entrega")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Entrega {


    @Id
    private UUID id;

    @Column(name = "pedido_id" ,nullable = false)
    private UUID pedidoId;

    @Embedded
    private Endereco endereco;

   @Enumerated(EnumType.STRING)
   private Status status;

    public void despachar() {
        if (status != Status.AGUARDANDO_COLETA)
            throw new IllegalStateException("Entrega não pode ser despachada nesse status");
        status = Status.EM_TRANSITO;
    }
}
