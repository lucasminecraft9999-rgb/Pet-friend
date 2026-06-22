package com.petfriends.infnet.PetFriends_Almoxarifado.service;

import com.petfriends.infnet.PetFriends_Almoxarifado.event.PedidoCriadoEstoqueEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReservaEstoqueListener {

    private final ItemEstoqueService itemEstoqueService;


    @KafkaListener(
            topics = "pedido-criado-estoque",
            groupId = "almoxarifado-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processar(PedidoCriadoEstoqueEvent event) {
        System.out.println("Evento recebido no Almoxarifado para o pedido: " + event.pedidoId());
        event.itens().forEach(item ->
                itemEstoqueService.reservar(item.sku(), item.quantidade())
        );
    }
}