package aula.microservicos.loginseguranca.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Corpo da requisicao de POST /login -> {"username": "", "senha": ""}
@Schema(name = "LoginRequest", description = "Credenciais enviadas no login")
public class LoginRequest {

    @Schema(description = "Nome do usuario", example = "maria", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "username e obrigatorio")
    public String username;

    @Schema(description = "Senha do usuario", example = "maria123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "senha e obrigatoria")
    public String senha;
}
