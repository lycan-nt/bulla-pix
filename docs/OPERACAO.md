# Operação

## Observabilidade e monitoramento

- **Logs** com `correlationId` (header `X-Correlation-Id` ou gerado automaticamente na API).
- **Actuator** no **pix-api** (`:8080`) e no **pix-worker** (`:8081`): `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.
- Pontos úteis de acompanhamento: taxa de `POST`, distribuição de status, falhas do parceiro e lag da fila (UI do RabbitMQ e métricas).

## Falhas temporárias e retry

- Mock do parceiro configurável: `app.partner.latency-ms` (~2000) e `app.partner.failure-rate`.
- Retry do listener RabbitMQ: até 3 tentativas com backoff exponencial.
- Contador `attemptCount` na transação; ao atingir `app.partner.max-attempts`, o status vai para `FAILED`.
- Circuit breaker `partnerPix` (Resilience4j) protege as chamadas ao parceiro.
- Mensagens rejeitadas após esgotar retries vão para a **DLQ** `pix.transactions.dlq`.

## Recuperação

- Após correção, reprocessar a DLQ republicando as mensagens para `pix.transactions`.
- O consumer é idempotente para status terminal (`COMPLETED` / `FAILED`).
- O `POST` idempotente evita duplicar a operação lógica com a mesma chave.

## Crescimento futuro

- Escalar **pix-api** e **pix-worker** com réplicas independentes.
- Outbox pattern para publicação at-least-once com consistência transacional.
- Particionamento por faixa de `transactionId` ou conta.
- Flyway, rate limiting e tracing distribuído (OpenTelemetry).
- Trocar o mock por um adaptador real do PSP / core bancário.

Ver também: [DECISOES.md](DECISOES.md) · [README](../README.md)
