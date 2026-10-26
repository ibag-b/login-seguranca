-- =====================================================================
-- 0) LIMPEZA
--    O data.sql roda a cada inicializacao (inclusive nos restarts do
--    DevTools). Os DROPs evitam erro de chave duplicada ao recriar.
--    A tabela authorities cai antes de users por causa da chave estrangeira.
-- =====================================================================
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS usuarios;

-- =====================================================================
-- 1) TABELAS DO ENUNCIADO (MS-AV-02)
--    Script copiado do enunciado. Unica alteracao: foi removida a virgula
--    sobrando antes do fecha-parenteses de login_attempts (o script do
--    enunciado tem "updated_at ...," e isso nao compila em SQL).
-- =====================================================================
CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- =====================================================================
-- 2) TABELAS DO SPRING SECURITY (JdbcUserDetailsManager)
--    Esquema padrao do Spring Security, igual ao da secao
--    "Definindo usuarios em banco de dados (JDBC)" do material da aula.
--    Sao os usuarios QUE CHAMAM a API (quem tem ROLE_ADMIN / ROLE_MANUTENCAO),
--    diferentes da tabela "usuarios" acima, que e a base do sistema de login.
-- =====================================================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);

-- ---------------------------------------------------------------------
-- Usuarios da API (senhas gravadas com BCrypt, prefixo {bcrypt})
--   manutencao / manutencao123  -> ROLE_MANUTENCAO  (POST / e GET /)
--   admin      / admin123       -> ROLE_ADMIN       (PUT /unlock/{userid})
-- ---------------------------------------------------------------------
INSERT INTO users (username, password, enabled)
VALUES ('manutencao', '{bcrypt}$2a$10$q6bIiagGk5fOeMglpUcAw.P3Vftrdxlyo12SsVB7EwD2rlM4Fo8YW', TRUE);

INSERT INTO users (username, password, enabled)
VALUES ('admin', '{bcrypt}$2a$10$qi2QE3cpBUrtzx3DdFARrewaaqHuLxUUY5kIkPjtoAcic2CxiWc7y', TRUE);

INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');

-- ---------------------------------------------------------------------
-- Usuarios do sistema de login (tabela do enunciado, senha em texto puro
-- porque a coluna definida no enunciado e VARCHAR(10))
-- ---------------------------------------------------------------------
INSERT INTO usuarios (username, senha) VALUES ('maria', 'maria123');
INSERT INTO usuarios (username, senha) VALUES ('joao', 'joao123');
INSERT INTO usuarios (username, senha) VALUES ('ana', 'ana123');

INSERT INTO login_attempts (username, failed_attempts, locked) VALUES ('maria', 0, FALSE);
INSERT INTO login_attempts (username, failed_attempts, locked) VALUES ('joao', 0, FALSE);
INSERT INTO login_attempts (username, failed_attempts, locked) VALUES ('ana', 0, FALSE);
