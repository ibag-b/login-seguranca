package aula.microservicos.loginseguranca.usuarios;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aula.microservicos.loginseguranca.metricas.LoginMetricas;

// Regras de negocio do sistema de login (tabelas "usuarios" e "login_attempts").
@Service
public class UsuarioService {

    // resultado possivel de uma tentativa de login
    public enum ResultadoLogin {
        SUCESSO, SENHA_INVALIDA, USUARIO_INEXISTENTE, BLOQUEADO
    }

    public enum ResultadoTroca {
        SUCESSO, SENHA_ATUAL_INVALIDA, USUARIO_INEXISTENTE
    }

    private final UsuarioRepository usuarioRepo;
    private final LoginAttemptRepository attemptRepo;
    private final LoginMetricas metricas;
    private final int maxTentativas;

    public UsuarioService(UsuarioRepository usuarioRepo, LoginAttemptRepository attemptRepo,
            LoginMetricas metricas, @Value("${seguranca.max-tentativas:2}") int maxTentativas) {
        this.usuarioRepo = usuarioRepo;
        this.attemptRepo = attemptRepo;
        this.metricas = metricas;
        this.maxTentativas = maxTentativas;
    }

    // POST /login
    @Transactional
    public ResultadoLogin autenticar(String username, String senha) {

        String senhaGravada = usuarioRepo.buscarSenha(username);

        if (senhaGravada == null) {
            metricas.loginInvalido();
            return ResultadoLogin.USUARIO_INEXISTENTE;
        }

        // usuario bloqueado nao consegue entrar nem com a senha certa
        if (attemptRepo.estaBloqueado(username)) {
            metricas.loginInvalido();
            return ResultadoLogin.BLOQUEADO;
        }

        if (senhaGravada.equals(senha)) {
            attemptRepo.desbloquear(username); // zera o contador de falhas
            metricas.loginValido();
            return ResultadoLogin.SUCESSO;
        }

        // senha errada: acumula a falha e, se passar do limite, bloqueia
        int falhas = attemptRepo.registrarFalha(username);
        if (falhas >= maxTentativas) {
            attemptRepo.bloquear(username);
        }
        metricas.loginInvalido();
        return ResultadoLogin.SENHA_INVALIDA;
    }

    // PUT /trocarsenha/{userid}
    @Transactional
    public ResultadoTroca trocarSenha(String username, String atual, String nova) {

        String senhaGravada = usuarioRepo.buscarSenha(username);

        if (senhaGravada == null) {
            return ResultadoTroca.USUARIO_INEXISTENTE;
        }

        if (!senhaGravada.equals(atual)) {
            return ResultadoTroca.SENHA_ATUAL_INVALIDA;
        }

        usuarioRepo.atualizarSenha(username, nova);
        return ResultadoTroca.SUCESSO;
    }

    // POST / (apenas ROLE_MANUTENCAO)
    @Transactional
    public boolean cadastrar(String username, String senha) {
        if (usuarioRepo.existe(username)) {
            return false; // ja existe
        }
        usuarioRepo.inserir(username, senha);
        if (!attemptRepo.existeRegistro(username)) {
            attemptRepo.criarRegistro(username);
        }
        return true;
    }

    // PUT /unlock/{userid} (apenas ROLE_ADMIN)
    @Transactional
    public boolean desbloquear(String username) {
        if (!usuarioRepo.existe(username)) {
            return false;
        }
        if (!attemptRepo.existeRegistro(username)) {
            attemptRepo.criarRegistro(username);
        }
        attemptRepo.desbloquear(username);
        return true;
    }

    // GET / (apenas ROLE_MANUTENCAO)
    public List<Usuario> listar() {
        return usuarioRepo.listarTodos();
    }
}
