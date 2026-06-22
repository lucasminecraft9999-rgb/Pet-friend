package com.petfriends.infnet.PetFriends_Transporte.service;

import com.petfriends.infnet.PetFriends_Transporte.event.PedidoCriadoTransporteEvent;
import com.petfriends.infnet.PetFriends_Transporte.model.Endereco;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CriacaoEntregaListener {

    private final EntregaService entregaService;


    @KafkaListener(
            topics = "pedido-criado-transporte",
            groupId = "transporte-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processar(PedidoCriadoTransporteEvent event) {
        System.out.println("Evento recebido no Transporte para o pedido: " + event.pedidoId());

        Endereco endereco = new Endereco(
                event.enderecoEntrega().logradouro(),
                event.enderecoEntrega().numero(),
                event.enderecoEntrega().cidade(),
                event.enderecoEntrega().uf(),
                event.enderecoEntrega().cep()
        );

        entregaService.criar(event.pedidoId(), endereco);
    }
}