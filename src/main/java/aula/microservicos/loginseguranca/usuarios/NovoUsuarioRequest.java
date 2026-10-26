package aula.microservicos.loginseguranca.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Corpo da requisicao de POST / -> {"username": "", "senha": ""}
@Schema(name = "NovoUsuarioRequest", description = "Dados para cadastrar um novo usuario")
public class NovoUsuarioRequest {

    @Schema(description = "Nome do novo usuario", example = "carlos", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "username e obrigatorio")
    @Size(max = 100, message = "username deve ter no maximo 100 caracteres")
    public String username;

    @Schema(description = "Senha do novo usuario (no maximo 10 caracteres)", example = "carlos123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "senha e obrigatoria")
    @Size(max = 10, message = "senha deve ter no maximo 10 caracteres")
    public String senha;
}
