package aula.microservicos.loginseguranca.usuarios;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Acesso a tabela "login_attempts" (a tabela de controle do enunciado):
// quantidade de falhas, bloqueio e datas.
@Repository
public class LoginAttemptRepository {

    private final JdbcTemplate jdbc;

    public LoginAttemptRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existeRegistro(String username) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_attempts WHERE username = ?", Integer.class, username);
        return total != null && total > 0;
    }

    public void criarRegistro(String username) {
        jdbc.update("INSERT INTO login_attempts (username, failed_attempts, locked, updated_at) "
                + "VALUES (?, 0, FALSE, CURRENT_TIMESTAMP)", username);
    }

    public boolean estaBloqueado(String username) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_attempts WHERE username = ? AND locked = TRUE",
                Integer.class, username);
        return total != null && total > 0;
    }

    public int tentativasFalhas(String username) {
        Integer total = jdbc.queryForObject(
                "SELECT COALESCE(MAX(failed_attempts), 0) FROM login_attempts WHERE username = ?",
                Integer.class, username);
        return total == null ? 0 : total;
    }

    // registra mais uma falha e devolve o total acumulado de falhas
    public int registrarFalha(String username) {
        if (!existeRegistro(username)) {
            criarRegistro(username);
        }
        jdbc.update("UPDATE login_attempts SET failed_attempts = failed_attempts + 1, "
                + "last_failed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
                + "WHERE username = ?", username);
        return tentativasFalhas(username);
    }

    public void bloquear(String username) {
        jdbc.update("UPDATE login_attempts SET locked = TRUE, locked_at = CURRENT_TIMESTAMP, "
                + "updated_at = CURRENT_TIMESTAMP WHERE username = ?", username);
    }

    // usado tanto no login valido quanto no PUT /unlock/{userid}
    public void desbloquear(String username) {
        jdbc.update("UPDATE login_attempts SET failed_attempts = 0, locked = FALSE, locked_at = NULL, "
                + "updated_at = CURRENT_TIMESTAMP WHERE username = ?", username);
    }

    // metrica: total de usuarios bloqueados neste momento
    public long contarBloqueados() {
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE", Long.class);
        return total == null ? 0L : total;
    }
}
