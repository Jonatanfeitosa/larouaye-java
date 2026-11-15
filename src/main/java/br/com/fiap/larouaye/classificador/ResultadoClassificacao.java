package br.com.fiap.larouaye.classificador;

import java.util.List;

/**
 * O que a IA (ou o plano B por regras) respondeu. É só uma SUGESTÃO: quem decide o que mostrar é o Java.
 *
 * @param categoria      código da categoria no vocabulário do modelo (ex.: MATRICULA_ONUS)
 * @param confianca      de 0 a 1
 * @param maisProvaveis  as categorias mais prováveis, da maior para a menor confiança
 * @param etapaSugerida  código da etapa sugerida pelo segundo modelo (pode ser nulo)
 * @param origem         "FLASK" quando veio do modelo; "REGRAS" quando foi o plano B
 */
public record ResultadoClassificacao(String categoria, double confianca, List<Candidata> maisProvaveis,
                                     String etapaSugerida, String origem) {

    /** Uma categoria candidata com sua confiança. */
    public record Candidata(String categoria, double confianca) {
    }
}
