package com.infnet.PetFriends_Pedidos.service;

import com.infnet.PetFriends_Pedidos.events.PedidoCriadoEstoqueEvent;
import com.infnet.PetFriends_Pedidos.events.PedidoCriadoTransporteEvent;
import com.infnet.PetFriends_Pedidos.model.Pedido;
import com.infnet.PetFriends_Pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoEventPublisher publisher;

    public PedidoService(PedidoRepository repository, PedidoEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public Pedido criar(Pedido pedido) {
        pedido = Pedido.builder()
                .id(UUID.randomUUID())
                .status(Pedido.Status.NOVO)
                .logradouro(pedido.getLogradouro())
                .numero(pedido.getNumero())
                .cidade(pedido.getCidade())
                .uf(pedido.getUf())
                .cep(pedido.getCep())
                .itens(pedido.getItens())
                .build();
        return repository.save(pedido);
    }

    public Pedido fechar(UUID id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));
        pedido.fechar();
        repository.save(pedido);


        List<PedidoCriadoEstoqueEvent.ItemPedido> itensEstoque = pedido.getItens().stream()
                .map(i -> new PedidoCriadoEstoqueEvent.ItemPedido(i.getSku(), i.getQuantidade()))
                .collect(Collectors.toList());
        publisher.publicarParaEstoque(new PedidoCriadoEstoqueEvent(pedido.getId(), itensEstoque));


        publisher.publicarParaTransporte(new PedidoCriadoTransporteEvent(
                pedido.getId(),
                new PedidoCriadoTransporteEvent.EnderecoDTO(
                        pedido.getLogradouro(), pedido.getNumero(),
                        pedido.getCidade(), pedido.getUf(), pedido.getCep())
        ));

        return pedido;
    }
}