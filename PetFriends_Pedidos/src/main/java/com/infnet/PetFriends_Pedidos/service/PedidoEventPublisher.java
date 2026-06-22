package com.infnet.PetFriends_Pedidos.service;

import com.infnet.PetFriends_Pedidos.events.PedidoCriadoEstoqueEvent;
import com.infnet.PetFriends_Pedidos.events.PedidoCriadoTransporteEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PedidoEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarParaEstoque(PedidoCriadoEstoqueEvent event) {
        kafkaTemplate.send("pedido-criado-estoque", event.pedidoId().toString(), event);
    }

    public void publicarParaTransporte(PedidoCriadoTransporteEvent event) {
        kafkaTemplate.send("pedido-criado-transporte", event.pedidoId().toString(), event);
    }
}
