package com.srm.msbackend.controllers;

import com.srm.msbackend.models.ExtratoTransacaoModel;
import com.srm.msbackend.services.ExtratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/extratos")
@Tag(name = "Extratos", description = "Consulta de transações com filtros e formatos de exportação")
@SecurityRequirement(name = "bearerAuth")
public class ExtratoController {
    private final ExtratoService extratoService;

    public ExtratoController(ExtratoService extratoService) {
        this.extratoService = extratoService;
    }

    @GetMapping
    @PreAuthorize("@fundoAuthorizationService.temScope(authentication, 'extratos:read')")
    @Operation(
            summary = "Consultar extrato",
            description = "Retorna transações filtradas por período, fundo, empresa e moeda. " +
                    "Operadores visualizam somente transações dos fundos aos quais estão associados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extrato gerado",
                    content = @Content(schema = @Schema(implementation = ExtratoTransacaoModel.class))),
            @ApiResponse(responseCode = "400", description = "Formato ou período inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    public ResponseEntity<?> listar(
            @Parameter(description = "Data inicial inclusiva", in = ParameterIn.QUERY, example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicial,
            @Parameter(description = "Data final inclusiva", in = ParameterIn.QUERY, example = "2026-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFinal,
            @Parameter(description = "ID do fundo", in = ParameterIn.QUERY, example = "1")
            @RequestParam(required = false) Long fundoId,
            @Parameter(description = "ID da empresa", in = ParameterIn.QUERY, example = "1")
            @RequestParam(required = false) Long empresaId,
            @Parameter(description = "ID da moeda", in = ParameterIn.QUERY, example = "1")
            @RequestParam(required = false) Long moedaId,
            @Parameter(description = "Formato de saída: json, csv ou parquet", example = "json")
            @RequestParam(defaultValue = "json") String formato,
            Authentication authentication) {
        List<ExtratoTransacaoModel> transacoes = extratoService.listar(
                dataInicial, dataFinal, fundoId, empresaId, moedaId, authentication);

        return switch (formato.toLowerCase()) {
            case "json" -> ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(transacoes);
            case "csv" -> ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato.csv\"")
                    .body(gerarCsv(transacoes).getBytes(StandardCharsets.UTF_8));
            case "parquet" -> ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.apache.parquet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato.parquet\"")
                    .body(gerarParquet(transacoes));
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Formato de relatório inválido: " + formato);
        };
    }

    private String gerarCsv(List<ExtratoTransacaoModel> transacoes) {
        StringBuilder csv = new StringBuilder(
                "id,valor,realizadaEm,status,contaOrigemId,contaDestinoId\n");
        for (ExtratoTransacaoModel transacao : transacoes) {
            csv.append(valor(transacao.id())).append(',')
                    .append(valor(transacao.valor())).append(',')
                    .append(valor(transacao.realizadaEm())).append(',')
                    .append(valor(transacao.status())).append(',')
                    .append(valor(transacao.contaOrigemId())).append(',')
                    .append(valor(transacao.contaDestinoId())).append('\n');
        }
        return csv.toString();
    }

    private byte[] gerarParquet(List<ExtratoTransacaoModel> transacoes) {
        String schemaJson = """
                {
                  "type": "record",
                  "name": "ExtratoTransacao",
                  "fields": [
                    {"name": "id", "type": ["null", "long"], "default": null},
                    {"name": "valor", "type": ["null", "double"], "default": null},
                    {"name": "realizadaEm", "type": ["null", "string"], "default": null},
                    {"name": "status", "type": ["null", "string"], "default": null},
                    {"name": "contaOrigemId", "type": ["null", "long"], "default": null},
                    {"name": "contaDestinoId", "type": ["null", "long"], "default": null}
                  ]
                }
                """;
        org.apache.avro.Schema schema = new org.apache.avro.Schema.Parser().parse(schemaJson);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                new InMemoryOutputFile(output))
                .withSchema(schema)
                .withConf(new Configuration(false))
                .build()) {
            for (ExtratoTransacaoModel transacao : transacoes) {
                GenericRecord record = new GenericData.Record(schema);
                record.put("id", transacao.id());
                record.put("valor", transacao.valor() == null ? null : transacao.valor().doubleValue());
                record.put("realizadaEm", valor(transacao.realizadaEm()));
                record.put("status", valor(transacao.status()));
                record.put("contaOrigemId", transacao.contaOrigemId());
                record.put("contaDestinoId", transacao.contaDestinoId());
                writer.write(record);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gerar o relatório Parquet", exception);
        }
        return output.toByteArray();
    }

    private String valor(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static final class InMemoryOutputFile implements OutputFile {
        private final ByteArrayOutputStream output;

        private InMemoryOutputFile(ByteArrayOutputStream output) {
            this.output = output;
        }

        @Override
        public PositionOutputStream create(long blockSizeHint) {
            return stream();
        }

        @Override
        public PositionOutputStream createOrOverwrite(long blockSizeHint) {
            output.reset();
            return stream();
        }

        @Override
        public boolean supportsBlockSize() {
            return false;
        }

        @Override
        public long defaultBlockSize() {
            return 0;
        }

        private PositionOutputStream stream() {
            return new PositionOutputStream() {
                @Override
                public long getPos() {
                    return output.size();
                }

                @Override
                public void write(int value) {
                    output.write(value);
                }

                @Override
                public void write(byte[] bytes, int offset, int length) {
                    output.write(bytes, offset, length);
                }
            };
        }
    }
}
