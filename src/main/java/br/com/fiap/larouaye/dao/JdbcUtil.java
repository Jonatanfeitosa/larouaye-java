package br.com.fiap.larouaye.dao;

import br.com.fiap.larouaye.exception.PersistenciaException;
import br.com.fiap.larouaye.exception.ViolacaoIntegridadeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pequenas funções comuns aos DAOs (valores nulos, datas e tradução de erros do Oracle). */
final class JdbcUtil {

    private JdbcUtil() {
    }

    static void setLong(PreparedStatement ps, int posicao, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicao, Types.NUMERIC);
        } else {
            ps.setLong(posicao, valor);
        }
    }

    static void setString(PreparedStatement ps, int posicao, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(posicao, Types.VARCHAR);
        } else {
            ps.setString(posicao, valor);
        }
    }

    static void setData(PreparedStatement ps, int posicao, LocalDate valor) throws SQLException {
        if (valor == null) {
            ps.setNull(posicao, Types.DATE);
        } else {
            ps.setDate(posicao, java.sql.Date.valueOf(valor));
        }
    }

    static Long getLong(ResultSet rs, String coluna) throws SQLException {
        long valor = rs.getLong(coluna);
        return rs.wasNull() ? null : valor;
    }

    static LocalDateTime getDataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp t = rs.getTimestamp(coluna);
        return t == null ? null : t.toLocalDateTime();
    }

    static LocalDate getData(ResultSet rs, String coluna) throws SQLException {
        java.sql.Date d = rs.getDate(coluna);
        return d == null ? null : d.toLocalDate();
    }

    /**
     * Traduz o erro do Oracle numa exceção do domínio.
     * ORA-00001 chave única | ORA-02292 existem filhos | ORA-02291 pai não existe | ORA-02290 CHECK violado.
     */
    static RuntimeException traduzir(SQLException e, String operacao) {
        switch (e.getErrorCode()) {
            case 1:
                return new ViolacaoIntegridadeException("Já existe um registro com esses dados.", e);
            case 2292:
                return new ViolacaoIntegridadeException("Este registro tem itens ligados a ele e não pode ser removido.", e);
            case 2291:
                return new ViolacaoIntegridadeException("Um dos registros informados não existe.", e);
            case 2290:
                return new ViolacaoIntegridadeException("Os dados enviados não respeitam as regras do banco.", e);
            default:
                return new PersistenciaException(operacao, e);
        }
    }
}
