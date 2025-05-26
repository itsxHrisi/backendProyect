
ALTER TABLE IF EXISTS sesiones_activas         DROP CONSTRAINT IF EXISTS fk_sesiones_usuario; 
ALTER TABLE IF EXISTS usuarios_roles           DROP CONSTRAINT IF EXISTS fk_usuarios_roles_usuarios;
ALTER TABLE IF EXISTS usuarios_roles           DROP CONSTRAINT IF EXISTS fk_usuarios_roles_roles;
ALTER TABLE IF EXISTS GrupoFamiliar            DROP CONSTRAINT IF EXISTS fk_grupo_admin;
ALTER TABLE IF EXISTS Gasto                    DROP CONSTRAINT IF EXISTS fk_gasto_usuario;
ALTER TABLE IF EXISTS Gasto                    DROP CONSTRAINT IF EXISTS fk_gasto_grupo;
ALTER TABLE IF EXISTS Invitacion               DROP CONSTRAINT IF EXISTS fk_invitacion_grupo;
ALTER TABLE IF EXISTS Mensaje                  DROP CONSTRAINT IF EXISTS fk_mensaje_emisor;
ALTER TABLE IF EXISTS Mensaje                  DROP CONSTRAINT IF EXISTS fk_mensaje_receptor;
ALTER TABLE IF EXISTS Mensaje                  DROP CONSTRAINT IF EXISTS fk_mensaje_grupo;
ALTER TABLE IF EXISTS usuarios                 DROP CONSTRAINT IF EXISTS fk_usuarios_grupo_familiar;

DROP TABLE IF EXISTS sesiones_activas;
DROP TABLE IF EXISTS usuarios_roles;
DROP TABLE IF EXISTS Gasto;
DROP TABLE IF EXISTS Invitacion;
DROP TABLE IF EXISTS Mensaje;
DROP TABLE IF EXISTS ingresos;        -- ← añadida
DROP TABLE IF EXISTS SubtipoGasto;

DROP TABLE IF EXISTS GrupoFamiliar;  -- ahora antes de usuarios
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS roles;

-- =============================================
-- 2️⃣ Crear tablas base
-- =============================================

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE GrupoFamiliar (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    grupo_familiar_id BIGINT,
    telefono VARCHAR(20),
    CONSTRAINT fk_usuarios_grupo_familiar FOREIGN KEY (grupo_familiar_id)
        REFERENCES GrupoFamiliar(id) ON DELETE SET NULL
);

ALTER TABLE GrupoFamiliar
    ADD COLUMN administrador_id BIGINT,
    ADD CONSTRAINT fk_grupo_admin FOREIGN KEY (administrador_id)
        REFERENCES usuarios(id) ON DELETE SET NULL;

CREATE TABLE usuarios_roles (
    idUsuario BIGINT NOT NULL,
    idRol    INTEGER NOT NULL,
    PRIMARY KEY (idUsuario, idRol),
    CONSTRAINT fk_usuarios_roles_usuarios FOREIGN KEY (idUsuario)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_roles FOREIGN KEY (idRol)
        REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE SubtipoGasto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    tipo_gasto VARCHAR(50) NOT NULL
);

CREATE TABLE Gasto (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    grupo_id   BIGINT NOT NULL,
    tipo_gasto VARCHAR(50) NOT NULL,
    subtipo    VARCHAR(50) NOT NULL,
    cantidad   DECIMAL(10,2) NOT NULL,
    fecha      DATE NOT NULL,
    CONSTRAINT fk_gasto_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_gasto_grupo   FOREIGN KEY (grupo_id)
        REFERENCES GrupoFamiliar(id) ON DELETE CASCADE
);
CREATE TABLE ingresos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    grupo_id   BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cantidad   NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_ingreso_usuario
      FOREIGN KEY (usuario_id)
      REFERENCES usuarios(id)
      ON DELETE CASCADE,
    CONSTRAINT fk_ingreso_grupo
      FOREIGN KEY (grupo_id)
      REFERENCES grupofamiliar(id)
      ON DELETE CASCADE
);

CREATE TABLE Invitacion (
    id BIGSERIAL PRIMARY KEY,
    nickname_destino  VARCHAR(255) NOT NULL,
    grupo_id BIGINT NOT NULL,
    estado   VARCHAR(20)  NOT NULL,
    fecha_envio TIMESTAMP NOT NULL,
    CONSTRAINT fk_invitacion_grupo FOREIGN KEY (grupo_id)
        REFERENCES GrupoFamiliar(id) ON DELETE CASCADE
);

CREATE TABLE Mensaje (
    id BIGSERIAL PRIMARY KEY,
    emisor_id   BIGINT NOT NULL,
    receptor_id BIGINT NOT NULL,
    grupo_id    BIGINT,
    contenido   TEXT NOT NULL,
    fecha       TIMESTAMP NOT NULL,
    CONSTRAINT fk_mensaje_emisor    FOREIGN KEY (emisor_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_receptor  FOREIGN KEY (receptor_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensaje_grupo     FOREIGN KEY (grupo_id)
        REFERENCES GrupoFamiliar(id) ON DELETE CASCADE
);

-- =============================================
-- 3️⃣ Crear tabla de sesiones activas
-- =============================================

CREATE TABLE sesiones_activas (
    id SERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_sesion VARCHAR(255) UNIQUE NOT NULL,
    ip_origen VARCHAR(45),
    dispositivo VARCHAR(255),
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    CONSTRAINT fk_sesiones_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =============================================
-- 4️⃣ Población inicial
-- =============================================

INSERT INTO roles(nombre) VALUES 
  ('ROL_USER'), 
  ('ROL_ADMIN'),
  ('ROL_PADRE'),
  ('ROL_HIJO');

