package aula.microservicos.loginseguranca.seguranca;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// 03 - Seguranca
//
// Os usuarios QUE CHAMAM a API (com as roles ROLE_MANUTENCAO e ROLE_ADMIN)
// ficam nas tabelas padrao do Spring Security (users / authorities) e sao
// carregados pelo JdbcUserDetailsManager, como pede o enunciado.
//
// Regras de acesso:
//   POST /login                -> publico
//   PUT  /trocarsenha/{userid} -> publico
//   POST /                     -> ROLE_MANUTENCAO
//   GET  /                     -> ROLE_MANUTENCAO
//   PUT  /unlock/{userid}      -> ROLE_ADMIN
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // API JdbcUserDetailsManager: le usuarios e roles direto do banco
    @Bean
    public UserDetailsManager userDetailsManager(DataSource datasource) {
        return new JdbcUserDetailsManager(datasource);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // permite que o console do H2 seja desenhado dentro de um frame
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // endpoints publicos do enunciado
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                        // documentacao, console do banco e metricas
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        // endpoints protegidos por role
                        .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .anyRequest().authenticated())
                // autenticacao HTTP Basic (usuario e senha no cabecalho da requisicao)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
