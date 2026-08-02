package com.bullla.pix.api;

import com.bullla.pix.api.dto.CreatePixRequest;
import com.bullla.pix.api.dto.PixResponse;
import com.bullla.pix.application.CreatePixCommand;
import com.bullla.pix.application.CreatePixTransactionUseCase;
import com.bullla.pix.application.GetPixTransactionUseCase;
import com.bullla.pix.domain.PixTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix")
@Tag(name = "PIX", description = "Solicitação e consulta de transações PIX")
@SecurityRequirement(name = "bearerAuth")
public class PixController {

    private final CreatePixTransactionUseCase createPixTransactionUseCase;
    private final GetPixTransactionUseCase getPixTransactionUseCase;

    public PixController(
            CreatePixTransactionUseCase createPixTransactionUseCase,
            GetPixTransactionUseCase getPixTransactionUseCase
    ) {
        this.createPixTransactionUseCase = createPixTransactionUseCase;
        this.getPixTransactionUseCase = getPixTransactionUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Solicitar PIX",
            description = "Recebe a solicitação, persiste e publica na fila para processamento assíncrono. Retorna 202 Accepted."
    )
    public ResponseEntity<PixResponse> create(@Valid @RequestBody CreatePixRequest request) {
        PixTransaction created = createPixTransactionUseCase.execute(
                new CreatePixCommand(
                        request.transactionId(),
                        request.amount(),
                        request.pixKey(),
                        request.description()
                )
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(PixResponse.from(created));
    }

    @GetMapping("/{transactionId}")
    @Operation(
            summary = "Consultar status do PIX",
            description = "Retorna o status atual da transação pelo transactionId."
    )
    public PixResponse get(@PathVariable String transactionId) {
        return PixResponse.from(getPixTransactionUseCase.execute(transactionId));
    }
}
