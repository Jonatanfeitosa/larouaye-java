package br.com.fiap.larouaye.classificador;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PLANO B determinístico: palavras-chave por categoria. Entra em ação quando o Flask está fora do ar,
 * para que as orientações continuem funcionando (o Java não depende da IA para responder).
 */
@Component
public class ClassificadorPorRegras {

    private static final Map<String, List<String>> PALAVRAS = new LinkedHashMap<>();

    static {
        PALAVRAS.put("MATRICULA_ONUS", List.of("matricula", "onus", "divida", "penhora", "hipoteca", "certidao", "gravame"));
        PALAVRAS.put("ESCRITURA_REGISTRO", List.of("escritura", "registro", "cartorio", "tabeliao", "tabelionato", "emolumento"));
        PALAVRAS.put("CONTRATO", List.of("contrato", "sinal", "arras", "promessa", "clausula", "distrato", "multa"));
        PALAVRAS.put("FINANCIAMENTO", List.of("financiamento", "financiar", "parcela", "banco", "credito", "caixa", "juros"));
        PALAVRAS.put("FGTS_SUBSIDIOS", List.of("fgts", "subsidio", "minha casa", "casa verde", "programa habitacional"));
        PALAVRAS.put("IMPOSTOS_TAXAS", List.of("itbi", "iptu", "imposto", "taxa", "tributo", "custo"));
        PALAVRAS.put("LEILAO", List.of("leilao", "arremat", "lance"));
        PALAVRAS.put("IMOVEL_USADO", List.of("usado", "vistoria", "reforma", "condominio"));
        PALAVRAS.put("FRAUDE_GOLPES", List.of("golpe", "fraude", "falsificad", "irrf", "imposto de renda", "dependente"));
    }

    public ResultadoClassificacao classificar(String pergunta) {
        String texto = normalizar(pergunta);
        Map<String, Integer> pontos = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<String, List<String>> e : PALAVRAS.entrySet()) {
            int p = 0;
            for (String palavra : e.getValue()) {
                if (texto.contains(palavra)) {
                    p++;
                }
            }
            if (p > 0) {
                pontos.put(e.getKey(), p);
                total += p;
            }
        }
        if (pontos.isEmpty()) {
            return new ResultadoClassificacao("FORA_DO_ESCOPO", 0.0, List.of(), null, "REGRAS");
        }
        List<ResultadoClassificacao.Candidata> candidatas = new ArrayList<>();
        final int soma = total;
        pontos.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> candidatas.add(new ResultadoClassificacao.Candidata(e.getKey(), (double) e.getValue() / soma)));
        ResultadoClassificacao.Candidata melhor = candidatas.get(0);
        // Confiança conservadora: no plano B só "responde direto" quando UMA categoria domina com folga.
        double confianca = Math.min(0.85, melhor.confianca());
        return new ResultadoClassificacao(melhor.categoria(), confianca, candidatas, null, "REGRAS");
    }

    /** minúsculas e sem acento, para comparar "matrícula" com "matricula". */
    public static String normalizar(String s) {
        String semAcento = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(java.util.Locale.ROOT);
    }
}
