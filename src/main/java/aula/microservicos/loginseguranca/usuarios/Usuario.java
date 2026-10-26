package aula.microservicos.loginseguranca.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;

// Representa uma linha da tabela "usuarios" (tabela do enunciado).
// A senha NAO e devolvida nas listagens (GET /) por seguranca.
@Schema(name = "Usuario", description = "Usuario cadastrado no sistema de login")
public class Usuario {

    @Schema(description = "Nome de usuario (chave primaria)", example = "maria")
    public String username;

    @Schema(description = "Indica se o usuario esta bloqueado por excesso de tentativas", example = "false")
    public boolean bloqueado;

    @Schema(description = "Quantidade de tentativas de login mal sucedidas acumuladas", example = "0")
    public int tentativasFalhas;

    public Usuario() {
    }

    public Usuario(String username, boolean bloqueado, int tentativasFalhas) {
        this.username = username;
        this.bloqueado = bloqueado;
        this.tentativasFalhas = tentativasFalhas;
    }
}
