# Operação

## Observabilidade e monitoramento

- **Correlation ID ponta a ponta**: o `pix-api` gera/propaga `correlationId` (header `X-Correlation-Id`, MDC nos logs). Ao publicar na fila, o `correlationId` viaja como header AMQP (`PixMessagingTopology.CORRELATION_ID_HEADER`) e o `pix-worker` o recoloca no MDC ao consumir — junto com o `transactionId` da mensagem. Assim um único `correlationId` conecta o log do `POST /pix` ao log do processamento no worker, mesmo em serviços/processos diferentes.
- **Actuator** no **pix-api** (`:8080`) e no **pix-worker** (`:8081`): `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`. No **pix-worker**, também `/actuator/circuitbreakers` e `/actuator/circuitbreakerevents` (Resilience4j).
- **Métricas de negócio** (Micrometer, expostas em `/actuator/prometheus` no **pix-worker**):
  - `pix_transactions_result_total{status="completed|failed"}` — contagem de transações por status final.
  - `pix_transactions_retry_total` — quantas vezes uma transação foi reagendada após falha temporária do parceiro.
  - `pix_partner_call_seconds{outcome="success|failure"}` — latência (count/sum/max) de cada chamada ao parceiro mock, já segmentada por resultado.
- **Health do circuit breaker**: `management.health.circuitbreakers.enabled=true` no **pix-worker** inclui o estado do circuit breaker `partnerPix` (`CLOSED`/`OPEN`/`HALF_OPEN`) em `/actuator/health`, sem precisar consultar métricas manualmente.
- Pontos úteis de acompanhamento: taxa de `POST` (métrica padrão `http_server_requests_seconds` no **pix-api**), distribuição de status (`pix_transactions_result_total`), falhas do parceiro (`pix_partner_call_seconds{outcome="failure"}` e estado do circuit breaker) e lag da fila (UI do RabbitMQ).

## Falhas temporárias e retry

- Mock do parceiro configurável: `app.partner.latency-ms` (~2000) e `app.partner.failure-rate`.
- Retry do listener RabbitMQ: até 3 tentativas com backoff exponencial.
- Contador `attemptCount` na transação; ao atingir `app.partner.max-attempts`, o status vai para `FAILED`.
- Circuit breaker `partnerPix` (Resilience4j) protege as chamadas ao parceiro.
- Mensagens rejeitadas após esgotar retries vão para a **DLQ** `pix.transactions.dlq`.

## Alertas e dashboards sugeridos

Não implementados neste teste (sem stack de Prometheus/Grafana provisionada), mas é o que eu ligaria em produção a partir das métricas acima:

| Sinal | Métrica | Sugestão de alerta |
|-------|---------|---------------------|
| Parceiro lento | `pix_partner_call_seconds` (p95) | Alertar se p95 > ~3s (SLA esperado é ~2s) |
| Parceiro instável | `rate(pix_partner_call_seconds_count{outcome="failure"}[5m])` | Alertar se taxa de falha > `app.partner.failure-rate` esperado por período sustentado |
| Circuit breaker aberto | `resilience4j_circuitbreaker_state{state="open"}` / `/actuator/health` | Alertar imediatamente em `OPEN` (indica parceiro degradado) |
| Transações represadas | `pix_transactions_retry_total` (taxa de crescimento) | Alertar se crescer sem transações saindo de `PROCESSING` |
| Fila crescendo | Profundidade da fila `pix.transactions` (RabbitMQ) | Alertar se lag > N mensagens por mais de X minutos → indica consumers insuficientes |
| Mensagens perdidas | Profundidade da `pix.transactions.dlq` | Alertar em qualquer mensagem na DLQ (requer investigação manual) |

Dashboard mínimo sugerido: taxa de `POST /pix`, distribuição de `pix_transactions_result_total` por status, latência do parceiro (p50/p95), estado do circuit breaker e profundidade das filas principal/DLQ.

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
