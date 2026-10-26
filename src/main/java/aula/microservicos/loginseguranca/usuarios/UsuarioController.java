package aula.microservicos.loginseguranca.usuarios;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import aula.microservicos.loginseguranca.usuarios.UsuarioService.ResultadoLogin;
import aula.microservicos.loginseguranca.usuarios.UsuarioService.ResultadoTroca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

// Endpoints exatamente como pedidos no enunciado da MS-AV-02:
//   POST /login                  (publico)
//   PUT  /trocarsenha/{userid}   (publico)
//   POST /                       (ROLE_MANUTENCAO)
//   PUT  /unlock/{userid}        (ROLE_ADMIN)
//   GET  /                       (ROLE_MANUTENCAO)
@RestController
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // ------------------------------------------------------------------
    // POST /login - publico
    // ------------------------------------------------------------------
    @Operation(summary = "Efetua login", tags = "login",
            description = "Valida usuario e senha na tabela usuarios. Apos mais de uma falha o usuario e bloqueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario e senha validos"),
            @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos ou usuario bloqueado", content = @Content) })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest dados) {

        ResultadoLogin resultado = service.autenticar(dados.username, dados.senha);

        switch (resultado) {
            case SUCESSO:
                return ResponseEntity.ok(Map.of("mensagem", "Login efetuado com sucesso"));
            case BLOQUEADO:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuario bloqueado por excesso de tentativas"));
            default:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuario ou senha invalidos"));
        }
    }

    // ------------------------------------------------------------------
    // PUT /trocarsenha/{userid} - publico
    // ------------------------------------------------------------------
    @Operation(summary = "Troca a senha de um usuario", tags = "login",
            description = "Confere a senha atual na base e, se estiver correta, grava a nova senha")
    @Parameters(@Parameter(name = "userid", description = "Nome do usuario (username)", example = "maria"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "401", description = "Senha atual invalida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content) })
    @PutMapping("/trocarsenha/{userid}")
    public ResponseEntity<Map<String, String>> trocarSenha(@PathVariable String userid,
            @Valid @RequestBody TrocarSenhaRequest dados) {

        ResultadoTroca resultado = service.trocarSenha(userid, dados.atual, dados.nova);

        switch (resultado) {
            case SUCESSO:
                return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
            case USUARIO_INEXISTENTE:
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Usuario nao encontrado: " + userid));
            default:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Senha atual invalida"));
        }
    }

    // ------------------------------------------------------------------
    // POST / - somente ROLE_MANUTENCAO
    // ------------------------------------------------------------------
    @Operation(summary = "Cadastra um novo usuario", tags = "administracao",
            description = "Somente usuarios com a role ROLE_MANUTENCAO podem cadastrar",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuario ja existe", content = @Content) })
    @PostMapping("/")
    public ResponseEntity<Map<String, String>> cadastrar(@Valid @RequestBody NovoUsuarioRequest dados) {

        boolean criado = service.cadastrar(dados.username, dados.senha);

        if (!criado) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Usuario ja cadastrado: " + dados.username));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Usuario cadastrado", "username", dados.username));
    }

    // ------------------------------------------------------------------
    // PUT /unlock/{userid} - somente ROLE_ADMIN
    // ------------------------------------------------------------------
    @Operation(summary = "Desbloqueia um usuario", tags = "administracao",
            description = "Somente usuarios com a role ROLE_ADMIN podem desbloquear",
            security = @SecurityRequirement(name = "basicAuth"))
    @Parameters(@Parameter(name = "userid", description = "Nome do usuario a desbloquear", example = "maria"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content) })
    @PutMapping("/unlock/{userid}")
    public ResponseEntity<Map<String, String>> desbloquear(@PathVariable String userid) {

        boolean desbloqueado = service.desbloquear(userid);

        if (!desbloqueado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Usuario nao encontrado: " + userid));
        }

        return ResponseEntity.ok(Map.of("mensagem", "Usuario desbloqueado: " + userid));
    }

    // ------------------------------------------------------------------
    // GET / - somente ROLE_MANUTENCAO
    // ------------------------------------------------------------------
    @Operation(summary = "Lista todos os usuarios", tags = "administracao",
            description = "Somente usuarios com a role ROLE_MANUTENCAO podem listar",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content) })
    @GetMapping("/")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
