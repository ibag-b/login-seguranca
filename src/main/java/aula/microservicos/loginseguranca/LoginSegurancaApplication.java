package aula.microservicos.loginseguranca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// 01 - Documentacao de Endpoint: a interface Swagger fica em
// http://localhost:8080/swagger-ui/index.html
@OpenAPIDefinition(info = @Info(
		title = "Sistema de Login",
		version = "1.0",
		description = "MS-AV-02 - Seguranca e Observabilidade: login, troca de senha, cadastro, desbloqueio e listagem de usuarios"))
// habilita o botao "Authorize" do Swagger para informar usuario/senha (HTTP Basic)
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@SpringBootApplication
public class LoginSegurancaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginSegurancaApplication.class, args);
	}

}
