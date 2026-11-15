package br.com.fiap.larouaye.classificador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Tenta o Flask; se falhar, usa as regras. Essa é a implementação que o resto do sistema enxerga. */
@Primary
@Component
public class ClassificadorComFallback implements ClassificadorPort {

    private static final Logger log = LoggerFactory.getLogger(ClassificadorComFallback.class);

    private final ClassificadorFlask flask;
    private final ClassificadorPorRegras regras;

    public ClassificadorComFallback(ClassificadorFlask flask, ClassificadorPorRegras regras) {
        this.flask = flask;
        this.regras = regras;
    }

    @Override
    public ResultadoClassificacao classificar(String pergunta) {
        try {
            return flask.classificar(pergunta);
        } catch (ClassificadorIndisponivelException e) {
            log.warn("IA indisponível, usando o plano B por regras: {}", e.getMessage());
            return regras.classificar(pergunta);
        }
    }
}
