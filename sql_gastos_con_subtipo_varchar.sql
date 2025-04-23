
-- 1️⃣ Eliminar claves foráneas para evitar conflictos
ALTER TABLE IF EXISTS usuarios DROP CONSTRAINT IF EXISTS fk_usuarios_grupo_familiar;
ALTER TABLE IF EXISTS GrupoFamiliar DROP CONSTRAINT IF EXISTS fk_grupo_admin;
ALTER TABLE IF EXISTS Gasto DROP CONSTRAINT IF EXISTS gasto_usuario_id_fkey;
ALTER TABLE IF EXISTS Gasto DROP CONSTRAINT IF EXISTS gasto_grupo_id_fkey;
-- Eliminamos solo las necesarias, ya no existe gasto_subtipo_id_fkey
ALTER TABLE IF EXISTS Invitacion DROP CONSTRAINT IF EXISTS invitacion_grupo_id_fkey;
ALTER TABLE IF EXISTS Mensaje DROP CONSTRAINT IF EXISTS mensaje_emisor_id_fkey;
ALTER TABLE IF EXISTS Mensaje DROP CONSTRAINT IF EXISTS mensaje_receptor_id_fkey;
ALTER TABLE IF EXISTS Mensaje DROP CONSTRAINT IF EXISTS mensaje_grupo_id_fkey;
ALTER TABLE IF EXISTS usuarios_roles DROP CONSTRAINT IF EXISTS fk_usuarios_roles_usuarios;
ALTER TABLE IF EXISTS usuarios_roles DROP CONSTRAINT IF EXISTS fk_usuarios_roles_roles;

-- 2️⃣ Eliminar tablas
DROP TABLE IF EXISTS usuarios_roles;
DROP TABLE IF EXISTS Gasto;
DROP TABLE IF EXISTS Invitacion;
DROP TABLE IF EXISTS Mensaje;
-- Ya no hace falta esta tabla
DROP TABLE IF EXISTS SubtipoGasto;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS GrupoFamiliar;
DROP TABLE IF EXISTS roles;

-- Crear tablas
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
    FOREIGN KEY (grupo_familiar_id) REFERENCES GrupoFamiliar(id)
);

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios_roles (
    idUsuario BIGINT NOT NULL,
    idRol INTEGER NOT NULL,
    PRIMARY KEY (idUsuario, idRol),
    CONSTRAINT fk_usuarios_roles_usuarios FOREIGN KEY (idUsuario)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_roles FOREIGN KEY (idRol)
        REFERENCES roles (id) ON DELETE CASCADE
);

ALTER TABLE GrupoFamiliar
ADD COLUMN administrador_id BIGINT,
ADD CONSTRAINT fk_grupo_admin FOREIGN KEY (administrador_id) REFERENCES usuarios(id);

-- 🎯 Aquí se hace el cambio: `subtipo` es ahora un VARCHAR
CREATE TABLE Gasto (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    grupo_id BIGINT NOT NULL,
    tipo_gasto VARCHAR(50) NOT NULL,
    subtipo VARCHAR(50) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (grupo_id) REFERENCES GrupoFamiliar(id)
);

CREATE TABLE Invitacion (
    id BIGSERIAL PRIMARY KEY,
    email_destino VARCHAR(255) NOT NULL,
    grupo_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_envio TIMESTAMP NOT NULL,
    FOREIGN KEY (grupo_id) REFERENCES GrupoFamiliar(id)
);

CREATE TABLE Mensaje (
    id BIGSERIAL PRIMARY KEY,
    emisor_id BIGINT NOT NULL,
    receptor_id BIGINT NOT NULL,
    grupo_id BIGINT,
    contenido TEXT NOT NULL,
    fecha TIMESTAMP NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    FOREIGN KEY (emisor_id) REFERENCES usuarios(id),
    FOREIGN KEY (receptor_id) REFERENCES usuarios(id),
    FOREIGN KEY (grupo_id) REFERENCES GrupoFamiliar(id)
);

INSERT INTO roles (nombre) VALUES 
  ('ROL_USER'), 
  ('ROL_ADMIN'),
  ('ROL_PADRE'),
  ('ROL_HIJO');
