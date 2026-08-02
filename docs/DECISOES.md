# Decisões técnicas

## Gargalos identificados

1. Pipeline síncrono de ponta a ponta (API → stored procedure → legado → core → parceiro), com P95 em torno de 4s.
2. Latência da instituição parceira (~2s) no caminho crítico da API — e essa latência não pode ser alterada.
3. Falhas temporárias do parceiro propagadas diretamente ao cliente.
4. Baixa elasticidade: o throughput fica limitado pelo tempo em que a thread da request fica ocupada.

## Decisões arquiteturais

| Decisão | Motivo |
|---------|--------|
| Processamento assíncrono com RabbitMQ | Tira a latência do parceiro do caminho do `POST` |
| Hexagonal / Clean Architecture leve (`com.bullla.pix`) | Isola o domínio e facilita testes |
| Idempotência por `transactionId` | Permite retry seguro do cliente |
| Mock do parceiro + Resilience4j | Simula latência/falhas e protege a integração |
| JWT HS256 de demonstração | Segurança mínima dado o prazo do teste |
| **pix-api** + **pix-worker** + biblioteca **pix-core** | Separa HTTP do processamento; escala consumers à parte |

## Trade-offs

- **Consistência eventual** em vez de resposta síncrona final: o cliente precisa consultar o status.
- **RabbitMQ** em vez de Kafka: menos complexidade operacional no prazo do teste.
- **Dois processos** (`pix-api` e `pix-worker`) em vez de monólito único: melhor isolamento, com um Compose a mais para operar.
- **`ddl-auto=update`** em vez de Flyway: velocidade no teste; migrações versionadas ficam como evolução.

## Comportamento em alto volume

- A API escala horizontalmente (stateless + JWT).
- Vários consumers competem na mesma fila; mais réplicas do **pix-worker** aumentam o throughput de processamento.
- A fila absorve picos enquanto o parceiro continua lento (~2s por chamada).
- A DLQ isola mensagens que esgotaram as tentativas, para análise posterior.

## Premissas

- Componentes externos podem ser simplificados ou mockados (conforme o enunciado).
- Status da transação: `RECEIVED` → `PROCESSING` → `COMPLETED` | `FAILED`.
- `POST /pix` retorna **202 Accepted**.
- Mesmo `transactionId` com payload diferente retorna **409 Conflict**.
- O `transactionId` é gerado e enviado pelo cliente (chave de idempotência).

## Estrutura multi-módulo

São **dois serviços** e **uma biblioteca**, não três microsserviços:

| Módulo | Em runtime |
|--------|------------|
| `pix-core` | JAR compartilhado (domínio, use cases, JPA, topologia RabbitMQ), embutido nos dois apps |
| `pix-api` | Processo HTTP na porta `8080` — JWT, REST, enfileiramento; listeners Rabbit desligados |
| `pix-worker` | Processo na porta `8081` — consome a fila, mock do parceiro, retry/DLQ e Resilience4j |

## Melhorias futuras (não bloqueantes)

- OpenAPI/Swagger mais rico (`@ApiResponse`, `@Parameter`, exemplos nos DTOs).
- Flyway, Outbox pattern, rate limiting e tracing distribuído (OpenTelemetry).

Ver também: [OPERACAO.md](OPERACAO.md) · [README](../README.md)
