// service/EntregaService.java
package com.petfriends.infnet.PetFriends_Transporte.service;

import com.petfriends.infnet.PetFriends_Transporte.model.Endereco;
import com.petfriends.infnet.PetFriends_Transporte.model.Entrega;
import com.petfriends.infnet.PetFriends_Transporte.model.Enum.Status;
import com.petfriends.infnet.PetFriends_Transporte.repository.EntregaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EntregaService {

    private final EntregaRepository repository;

    public Entrega criar(UUID pedidoId, Endereco enderecoDestino) {
        Entrega entrega = Entrega.builder()
                .id(UUID.randomUUID())
                .pedidoId(pedidoId)
                .endereco(enderecoDestino)
                .status(Status.AGUARDANDO_COLETA)
                .build();
        return repository.save(entrega);
    }

    public Entrega buscarPorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrega não encontrada: " + id));
    }

    public Entrega buscarPorPedidoId(UUID pedidoId) {
        return repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega não encontrada para o pedido: " + pedidoId));
    }

    public List<Entrega> listarTodas() {
        return repository.findAll();
    }

    public Entrega despachar(UUID id) {
        Entrega entrega = buscarPorId(id);
        entrega.despachar();
        return repository.save(entrega);
    }
}