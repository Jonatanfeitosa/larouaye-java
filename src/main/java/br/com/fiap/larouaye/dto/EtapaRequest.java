package br.com.fiap.larouaye.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Entrada: etapa da compra. */
public class EtapaRequest {

    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "[A-Z_]{3,30}", message = "Use de 3 a 30 letras maiúsculas ou sublinhado.")
    private String codigo;

    @NotNull(message = "Informe a ordem.")
    @Positive(message = "A ordem deve ser positiva.")
    private Integer ordem;

    @NotBlank(message = "Informe o nome.")
    @Size(min = 2, max = 100, message = "O nome deve ter de 2 a 100 letras.")
    private String nome;

    @NotBlank(message = "Informe a descrição.")
    @Size(min = 2, max = 400, message = "A descrição deve ter de 2 a 400 letras.")
    private String descricao;

    @Valid
    @Size(min = 0, max = 30, message = "Use no máximo 30 documentos.")
    private List<EtapaDocumentoRequest> documentos;

    public EtapaRequest() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<EtapaDocumentoRequest> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<EtapaDocumentoRequest> documentos) {
        this.documentos = documentos;
    }
}
