package aula.microservicos.loginseguranca.metricas;

import org.springframework.stereotype.Component;

import aula.microservicos.loginseguranca.usuarios.LoginAttemptRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

// 02 - Observabilidade: os tres indicadores pedidos no enunciado.
//
//   logins.validos      -> contador  (no Prometheus: logins_validos_total)
//   logins.invalidos    -> contador  (no Prometheus: logins_invalidos_total)
//   usuarios.bloqueados -> medidor   (no Prometheus: usuarios_bloqueados)
//
// Os dois primeiros sobem a cada tentativa de login; o terceiro le a tabela
// login_attempts sempre que o Prometheus coleta as metricas.
@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(MeterRegistry registry, LoginAttemptRepository attemptRepo) {

        this.loginsValidos = Counter.builder("logins.validos")
                .description("Quantidade de logins realizados com sucesso")
                .register(registry);

        this.loginsInvalidos = Counter.builder("logins.invalidos")
                .description("Quantidade de tentativas de login invalidas")
                .register(registry);

        Gauge.builder("usuarios.bloqueados", attemptRepo, repo -> repo.contarBloqueados())
                .description("Total de usuarios bloqueados neste momento")
                .register(registry);
    }

    public void loginValido() {
        this.loginsValidos.increment();
    }

    public void loginInvalido() {
        this.loginsInvalidos.increment();
    }
}
