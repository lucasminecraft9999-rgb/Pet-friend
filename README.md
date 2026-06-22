# PetFriends — Arquitetura de Microsserviços Event-Driven
 
Projeto acadêmico que demonstra a decomposição de um monólito de e-commerce pet em microsserviços, aplicando **DDD (Domain-Driven Design)**, **Domain Events** e comunicação assíncrona via **Apache Kafka**.
 
## Sumário
 
- [Visão geral da arquitetura](#visão-geral-da-arquitetura)
- [Microsserviços](#microsserviços)
- [Domain-Driven Design (DDD)](#domain-driven-design-ddd)
- [Domain Events](#domain-events)
- [Comunicação assíncrona com Kafka](#comunicação-assíncrona-com-kafka)
- [Como rodar o projeto](#como-rodar-o-projeto)
- [Testando o fluxo via Postman](#testando-o-fluxo-via-postman)
- [Observabilidade](#observabilidade)
- [Erros comuns e soluções](#erros-comuns-e-soluções)
---
 
## Visão geral da arquitetura
 
O sistema é composto por três microsserviços independentes, cada um com seu próprio banco de dados (H2 em memória) e seu próprio ciclo de vida, comunicando-se de forma **assíncrona via Kafka** sempre que uma ação em um serviço precisa refletir em outro.
 
```
PetFriends_Web (ReactJS)
        │ REST síncrono
        ▼
PetFriends_Pedidos ──────► Kafka ──────► PetFriends_Almoxarifado
        │                                       (reserva de estoque)
        │
        └──────────────► Kafka ──────► PetFriends_Transporte
                                               (criação de entrega)
```
 
O agregado `Pedido` percorre os seguintes estados: `Novo → Fechado → Em Preparação → Em Trânsito → Entregue / Devolvido / Cancelado / Extraviado`. As transições de estado `Fechado` e `Despachar Pedido` são os gatilhos que disparam os eventos de domínio consumidos pelos outros dois microsserviços.
 
---
 
## Microsserviços
 
| Microsserviço | Porta | Banco H2 | Responsabilidade |
|---|---|---|---|
| `PetFriends_Pedidos` | 8083 | `jdbc:h2:mem:pedidos` | Gerencia o ciclo de vida do pedido e **publica** eventos de domínio |
| `PetFriends_Almoxarifado` | 8081 | `jdbc:h2:mem:almoxarifado` | Controla estoque e **consome** eventos para reservar itens |
| `PetFriends_Transporte` | 8082 | `jdbc:h2:mem:transporte` | Gerencia entregas e **consome** eventos para criar a entrega |
 
---
 
## Domain-Driven Design (DDD)
 
### PetFriends_Almoxarifado
 
- **Aggregate Root**: `ItemEstoque` — controla a invariante de que a quantidade disponível nunca pode ficar negativa nem ser reservada além do existente.
- **Value Object**: `Quantidade` — encapsula a regra de não aceitar valores negativos e as operações de somar/subtrair de forma imutável.
```java
@Entity
public class ItemEstoque {
    @Id private UUID id;
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
```
 
### PetFriends_Transporte
 
- **Aggregate Root**: `Entrega` — controla o ciclo de vida do transporte de um pedido (`AGUARDANDO_COLETA → EM_TRANSITO → ENTREGUE/CANCELADA`).
- **Value Object**: `Endereco` — agrupa logradouro, número, cidade, UF e CEP sem identidade própria.
```java
@Entity
public class Entrega {
    @Id private UUID id;
    private UUID pedidoId;
 
    @Embedded
    private Endereco enderecoDestino;
 
    @Enumerated(EnumType.STRING)
    private Status status;
 
    public void despachar() {
        if (status != Status.AGUARDANDO_COLETA)
            throw new IllegalStateException("Entrega não pode ser despachada nesse status");
        status = Status.EM_TRANSITO;
    }
}
```
 
---
 
## Domain Events
 
### Por que eventos, e não só REST síncrono?
 
O `PetFriends_Web` consome Clientes, Produtos e Pedidos de forma síncrona via REST. A funcionalidade mais afetada pelos eventos de domínio é a **consulta de status do pedido logo após a criação**: a resposta HTTP de criação do pedido retorna rápido, mas a reserva de estoque e a criação da entrega só acontecem depois, de forma assíncrona — gerando uma janela de **consistência eventual**.
 
### ID do agregado vs. payload completo
 
- **Somente o ID**: mensagem leve, mas obriga o consumidor a fazer uma chamada síncrona de volta ao serviço de origem, reintroduzindo acoplamento e dependência de disponibilidade.
- **Payload completo** (event-carried state transfer): o evento já carrega tudo que o consumidor precisa, sem chamadas de retorno — mais resiliente, porém exige mais cuidado com versionamento de contrato.
Optamos pela segunda abordagem, com **payloads parciais e específicos para cada consumidor**.
 
### Eventos publicados pelo PetFriends_Pedidos
 
**Para o Almoxarifado** — apenas SKU e quantidade, sem dados de endereço/cliente:
 
```java
public record PedidoCriadoEstoqueEvent(UUID pedidoId, List<ItemPedido> itens) {
    public record ItemPedido(String sku, int quantidade) {}
}
```
 
**Para o Transporte** — apenas o ID do pedido e o endereço de entrega, sem dados de estoque/preço:
 
```java
public record PedidoCriadoTransporteEvent(UUID pedidoId, EnderecoDTO enderecoEntrega) {
    public record EnderecoDTO(String logradouro, String numero, String cidade, String uf, String cep) {}
}
```
 
Cada consumidor recebe **somente o subconjunto de dados que precisa** do agregado `Pedido`, através de tópicos Kafka distintos.
 
---
 
## Comunicação assíncrona com Kafka
 
### Tópicos
 
| Tópico | Produtor | Consumidor | Consumer Group |
|---|---|---|---|
| `pedido-criado-estoque` | PetFriends_Pedidos | PetFriends_Almoxarifado | `almoxarifado-service` |
| `pedido-criado-transporte` | PetFriends_Pedidos | PetFriends_Transporte | `transporte-service` |
 
### Fluxo completo
 
1. `PedidoController.fechar()` → `PedidoService.fechar()` muda o status do pedido para `FECHADO`.
2. `PedidoEventPublisher` publica os dois eventos via `KafkaTemplate`.
3. Cada microsserviço consumidor tem um `@KafkaListener` que **escuta automaticamente** o tópico configurado — não há chamada manual nem via Controller.
4. `ReservaEstoqueListener` (Almoxarifado) chama `ItemEstoqueService.reservar()` para cada item recebido.
5. `CriacaoEntregaListener` (Transporte) chama `EntregaService.criar()` com o endereço recebido.
### Configuração — pontos de atenção (erros comuns que já corrigimos no projeto)
 
Ao configurar Producer/Consumer, **os imports precisam vir exatamente destes pacotes**:
 
```java
// Consumer
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
 
// Producer
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
```
 
> ⚠️ **Nunca** importar essas classes de `com.fasterxml.jackson.databind.*` — é um erro comum de auto-import da IDE que gera `KafkaException: ... is not an instance of Serializer/Deserializer`.
 
### `application.properties` de cada microsserviço
 
```properties
spring.kafka.bootstrap-servers=kafka:29092
```
 
> Dentro da rede do Docker Compose, os containers se comunicam pelo **nome do serviço** (`kafka`), nunca por `localhost`. `localhost:9092` só funciona quando a aplicação roda fora do Docker (ex.: direto pela IDE, com Kafka exposto na máquina host).
 
---
 
## Como rodar o projeto
 
### Estrutura de pastas
 
```
petfriend_infnet/
├── docker-compose.yml
├── PetFriends_Almoxarifado/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── PetFriends_Transporte/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── PetFriends_Pedidos/
    ├── Dockerfile
    ├── pom.xml
    └── src/
```
 
### Subindo tudo com Docker Compose
 
```bash
docker-compose up --build
```
 
Isso builda as 3 imagens Spring Boot (multi-stage build com Maven) e sobe, na ordem correta: `zookeeper` → `kafka` → `kafka-ui` → `almoxarifado` / `transporte` / `pedidos`.
 
### Comandos úteis
 
```bash
docker ps                          # ver containers rodando
docker-compose logs -f pedidos     # ver logs de um serviço específico
docker-compose down                # parar tudo
docker-compose down -v             # parar tudo e limpar volumes
```
 
### Acessos
 
| Serviço | URL |
|---|---|
| PetFriends_Almoxarifado | http://localhost:8081 |
| PetFriends_Transporte | http://localhost:8082 |
| PetFriends_Pedidos | http://localhost:8083 |
| Kafka UI | http://localhost:8090 |
| H2 Console (cada serviço) | `http://localhost:<porta>/h2-console` |
 
---
 
## Testando o fluxo via Postman
 
Importe a collection `PetFriends_Kafka.postman_collection.json` e siga a ordem:
 
1. **Almoxarifado → Criar Item de Estoque** (SKU `RACAO-10KG`, 50 unidades)
2. **Pedidos → Criar Pedido** — copie o `id` da resposta para a variável `pedidoId`
3. **Pedidos → Fechar Pedido** — dispara os dois eventos no Kafka
4. **Almoxarifado → Buscar Item por SKU** — confirme que `reservada` aumentou automaticamente
5. **Transporte → Buscar Entrega por Pedido** — confirme que a entrega foi criada automaticamente
Se os passos 4 e 5 mostrarem os dados refletidos **sem chamada manual** aos endpoints `/reservar` ou `/entregas`, o Kafka está funcionando corretamente.
 
---
 
## Observabilidade
 
Conceitos aplicáveis a esta arquitetura para evolução futura com Zipkin, Micrometer e ELK Stack:
 
- **API Gateway**: ponto único de entrada que centraliza roteamento, autenticação e rate limiting — facilita o cliente, mas pode se tornar ponto único de falha.
- **ID de Correlação**: identificador único propagado em headers HTTP e em mensagens Kafka, permitindo rastrear uma requisição por todos os serviços envolvidos.
- **Micrometer + Zipkin**: o Micrometer instrumenta métricas e gera os spans de tracing, que são exportados e visualizados no Zipkin como árvore de chamadas distribuída.
- **Agregador de Logs (ELK Stack)**: centraliza logs de todos os microsserviços em um único repositório pesquisável, correlacionando-os pelo ID de Correlação.
---
 
## Erros comuns e soluções
 
| Erro | Causa | Solução |
|---|---|---|
| `Column 'valor' is duplicated in mapping` | Dois campos `@Embedded` do mesmo tipo sem `@AttributeOverride` | Adicionar `@AttributeOverride(name = "valor", column = @Column(name = "..."))` em cada campo |
| `Cannot resolve constructor 'Entity(...)'` | Uso de `@Builder` sem `@AllArgsConstructor`/`@NoArgsConstructor` | Adicionar as três anotações Lombok junto com `@Builder` |
| `... is not an instance of Serializer/Deserializer` | Import de `StringSerializer`/`JsonSerializer` do pacote Jackson em vez do Kafka | Usar `org.apache.kafka.common.serialization.*` e `org.springframework.kafka.support.serializer.*` |
| `Connection to node -1 (localhost/127.0.0.1:9092) could not be established` | `bootstrap-servers` configurado como `localhost:9092` dentro de um container Docker | Trocar para `kafka:29092` (nome do serviço no `docker-compose.yml`) |
| `500 Internal Server Error` ao chamar endpoint com `{{pedidoId}}` | Variável do Postman vazia (UUID inválido) | Copiar o `id` retornado na criação do recurso e colar na variável antes de continuar o fluxo |
 
---
 
## Stack tecnológica
 
- Java 17
- Spring Boot 3.x (Web, Data JPA, Kafka)
- H2 Database (em memória)
- Apache Kafka + Zookeeper
- Lombok
- Docker / Docker Compose
- Postman (testes manuais)
