package aula.microservicos.loginseguranca.usuarios;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Acesso a tabela "usuarios" (a tabela do enunciado) usando JdbcTemplate.
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existe(String username) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?", Integer.class, username);
        return total != null && total > 0;
    }

    // devolve a senha gravada ou null se o usuario nao existir
    public String buscarSenha(String username) {
        List<String> encontrados = jdbc.query(
                "SELECT senha FROM usuarios WHERE username = ?",
                (rs, linha) -> rs.getString("senha"),
                username);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    public void inserir(String username, String senha) {
        jdbc.update("INSERT INTO usuarios (username, senha) VALUES (?, ?)", username, senha);
    }

    public void atualizarSenha(String username, String novaSenha) {
        jdbc.update("UPDATE usuarios SET senha = ? WHERE username = ?", novaSenha, username);
    }

    // GET / : todos os usuarios, com a situacao de bloqueio vinda de login_attempts
    public List<Usuario> listarTodos() {
        return jdbc.query(
                "SELECT u.username, "
                        + "COALESCE(l.locked, FALSE) AS locked, "
                        + "COALESCE(l.failed_attempts, 0) AS failed_attempts "
                        + "FROM usuarios u LEFT JOIN login_attempts l ON l.username = u.username "
                        + "ORDER BY u.username",
                (rs, linha) -> new Usuario(
                        rs.getString("username"),
                        rs.getBoolean("locked"),
                        rs.getInt("failed_attempts")));
    }
}
