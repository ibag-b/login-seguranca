package aula.microservicos.loginseguranca.usuarios;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Corpo da requisicao de PUT /trocarsenha/{userid} -> {"atual": "", "nova": ""}
@Schema(name = "TrocarSenhaRequest", description = "Senha atual e nova senha")
public class TrocarSenhaRequest {

    @Schema(description = "Senha atual do usuario", example = "maria123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "atual e obrigatoria")
    public String atual;

    @Schema(description = "Nova senha (no maximo 10 caracteres)", example = "nova123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "nova e obrigatoria")
    @Size(max = 10, message = "nova senha deve ter no maximo 10 caracteres")
    public String nova;
}
