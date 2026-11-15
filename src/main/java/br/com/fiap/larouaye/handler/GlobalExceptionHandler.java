package br.com.fiap.larouaye.handler;

import br.com.fiap.larouaye.dto.ErroResponse;
import br.com.fiap.larouaye.exception.DadosInvalidosException;
import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.exception.RecursoNaoEncontradoException;
import br.com.fiap.larouaye.exception.RegraDeNegocioException;
import br.com.fiap.larouaye.exception.ViolacaoIntegridadeException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global: TODO erro vira o mesmo JSON (ErroResponse), sem stack trace e sem SQL.
 * Mapa de status: 400 dados inválidos | 404 não existe | 405 método errado | 409 regra de negócio | 415 tipo de conteúdo | 500 inesperado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, HttpServletRequest req, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, req.getRequestURI(), campos));
    }

    // ---------- 400 ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        String resumo = campos.values().stream().collect(Collectors.joining(" "));
        return resposta(HttpStatus.BAD_REQUEST, resumo.isBlank() ? "Dados inválidos." : resumo, req, campos);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(DadosInvalidosException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErroResponse> requisicaoMalFormada(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "A requisição está mal formada. Confira o JSON e os parâmetros enviados.", req, null);
    }

    // ---------- 404 ----------
    @ExceptionHandler({RecursoNaoEncontradoException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroResponse> naoEncontrado(Exception ex, HttpServletRequest req) {
        String msg = ex instanceof RecursoNaoEncontradoException ? ex.getMessage() : "Recurso não encontrado.";
        return resposta(HttpStatus.NOT_FOUND, msg, req, null);
    }

    // ---------- 405 e 415 ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método " + ex.getMethod() + " não é permitido neste endereço.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoNaoSuportado(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo como application/json.", req, null);
    }

    // ---------- 409 ----------
    @ExceptionHandler({RegraDeNegocioException.class, ViolacaoIntegridadeException.class})
    public ResponseEntity<ErroResponse> conflito(RuntimeException ex, HttpServletRequest req) {
        if (ex instanceof ViolacaoIntegridadeException) {
            LOG.warn("Violação de integridade em {}: {}", req.getRequestURI(), ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
        }
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // ---------- 500 ----------
    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroResponse> persistencia(PersistenciaException ex, HttpServletRequest req) {
        LOG.error("Erro de persistência em {}", req.getRequestURI(), ex); // detalhe SÓ no log
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Tivemos um problema ao guardar ou buscar os dados. Tente de novo em instantes.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        LOG.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Algo deu errado do nosso lado. Tente de novo em instantes.", req, null);
    }
}
