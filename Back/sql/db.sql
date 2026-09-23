CREATE DATABASE padelconnect_db;
USE padelconnect_db;

DROP TABLE IF EXISTS inscripciones;
DROP TABLE IF EXISTS partidos;
DROP TABLE IF EXISTS usuarios;


CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(255) NOT NULL,
    nivel VARCHAR(255) NOT NULL
);

CREATE TABLE partidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha VARCHAR(255) NOT NULL,
    hora VARCHAR(255) NOT NULL,
    cancha VARCHAR(255) NOT NULL,
    direccion VARCHAR(255),
    zona VARCHAR(255) NOT NULL,
    latitud DOUBLE,
    longitud DOUBLE,
    nivel VARCHAR(255) NOT NULL,
    cupos_totales INT NOT NULL,
    precio_persona VARCHAR(255),
    tipo_cancha VARCHAR(255),
    genero VARCHAR(255),
    organizador_id BIGINT NOT NULL,
    CONSTRAINT fk_partido_organizador FOREIGN KEY (organizador_id) REFERENCES usuarios(id)
);

CREATE TABLE inscripciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partido_id BIGINT NOT NULL,
    jugador_id BIGINT NOT NULL,
    fecha_inscripcion DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    mensaje VARCHAR(300),
    CONSTRAINT fk_inscripcion_partido FOREIGN KEY (partido_id) REFERENCES partidos(id),
    CONSTRAINT fk_inscripcion_jugador FOREIGN KEY (jugador_id) REFERENCES usuarios(id),
    CONSTRAINT uq_partido_jugador UNIQUE (partido_id, jugador_id)
);

-- Usuarios de prueba. Contraseña de los 4: Padel123

INSERT INTO usuarios (id, nombre, email, password, telefono, nivel) VALUES
    (1, 'Gonzalo Gazzero', 'gonzalo@padeltest.com', '$2b$10$hbE1NDp3R5h95A1u.wgageyYmO/VP1bSUwVMvPm3jq2kR76qJGW1G', '3467435723', '5ta Categoría'),
    (2, 'Juanito Lopez',   'juanito@padeltest.com', '$2b$10$hbE1NDp3R5h95A1u.wgageyYmO/VP1bSUwVMvPm3jq2kR76qJGW1G', '3467111111', '6ta Categoría'),
    (3, 'Marcos Paredes',  'marcos@padeltest.com',  '$2b$10$hbE1NDp3R5h95A1u.wgageyYmO/VP1bSUwVMvPm3jq2kR76qJGW1G', '3467222222', '4ta Categoría'),
    (4, 'Andres Gazzero',  'andres@padeltest.com',  '$2b$10$hbE1NDp3R5h95A1u.wgageyYmO/VP1bSUwVMvPm3jq2kR76qJGW1G', '3467333333', '7ma Categoría');

-- Partidos de prueba 
-- Las fechas se calculan a partir de la fecha actual (CURDATE()) para evitar que al correr el script, no queden partidos de fechas viejas


-- Partido 1: abierto, sin solicitudes - Rosario
INSERT INTO partidos 
(id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(1, 
DATE_FORMAT(CURDATE() + INTERVAL 3 DAY, '%Y-%m-%d'),
 '19:00', 
 'Náutico Rosario', 
 'Av. Costanera 500', 
 'Rosario (Santa Fe)', 
 -32.9442, -60.6505, 
 '6ta Categoría', 
 4, '$6.000', 
 'Cemento', 
 'Mixto', 2);

-- Partido 2: abierto, sin solicitudes - Villa María
INSERT INTO partidos (id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(2, 
DATE_FORMAT(CURDATE() + INTERVAL 7 DAY, '%Y-%m-%d'), 
'20:00', 
'Club Villa María Pádel', 
'Bv. Sarmiento 850', 
'Villa María (Córdoba)', 
-32.4135041930881,
-63.248329032642, 
'7ma Categoría', 
2, 
'$5.000', 
'Césped Sintético', 
'Masculino', 4);

-- Partido 3: con una solicitud pendiente (con mensaje) - Monte Buey
INSERT INTO partidos (id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(3, DATE_FORMAT(CURDATE() + INTERVAL 1 DAY, '%Y-%m-%d'), 
'18:30', 
'Complejo Monte Buey', 
'Ruta 6 Km 4', 
'Monte Buey (Córdoba)', 
-32.9156374383737, 
-62.454900955381, 
'5ta Categoría', 
2, 
'$4.500',
'Césped Sintético', 
'Mixto', 1);

-- Partido 4: con un jugador ya aceptado y todavía con lugar - Córdoba Capital
INSERT INTO partidos (id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(4, DATE_FORMAT(CURDATE() + INTERVAL 14 DAY, '%Y-%m-%d'), 
'21:00', 
'Pádel Nueva Córdoba', 
'Bv. Illia 234', 
'Córdoba Capital (Córdoba)', 
-31.4201, -64.1888, 
'4ta Categoría', 
3, 
'$7.000', 
'Cemento', 
'Femenino', 3);

-- Partido 5: lleno (sirve para probar que no aparece en el listado) - Monte Buey
INSERT INTO partidos (id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(5, DATE_FORMAT(CURDATE() + INTERVAL 3 DAY, '%Y-%m-%d'), 
'10:00', 
'Complejo Monte Buey', 
'Ruta 6 Km 4', 
'Monte Buey (Córdoba)', 
-32.9156374383737, 
-62.454900955381, 
'6ta Categoría', 
2, '
$4.500', 
'Césped Sintético', 
'Mixto', 4);

-- Partido 6: de ayer (sirve para probar que no aparece en el listado) - Rosario
INSERT INTO partidos (id, fecha, hora, cancha, direccion, zona, latitud, longitud, nivel, cupos_totales, precio_persona, tipo_cancha, genero, organizador_id) 
VALUES
(6, DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y-%m-%d'), 
'18:00', 
'Náutico Rosario', 
'Av. Costanera 500', 
'Rosario (Santa Fe)', 
-32.9442, -60.6505, 
'5ta Categoría', 
4, 
'$6.000', 
'Cemento', 
'Mixto', 3);

-- El organizador de cada partido queda inscripto como ACEPTADO (igual que hace la app al crear un partido).

INSERT INTO inscripciones (partido_id, jugador_id, fecha_inscripcion, estado, mensaje) VALUES
    (1, 2, NOW(), 'ACEPTADO', NULL),
    (2, 4, NOW(), 'ACEPTADO', NULL),
    (3, 1, NOW(), 'ACEPTADO', NULL),
    (3, 2, NOW(), 'PENDIENTE', 'Hola! Somos 2, nos podemos sumar?'),
    (4, 3, NOW(), 'ACEPTADO', NULL),
    (4, 4, NOW(), 'ACEPTADO', NULL),
    (5, 4, NOW(), 'ACEPTADO', NULL),
    (5, 1, NOW(), 'ACEPTADO', NULL),
    (6, 3, NOW(), 'ACEPTADO', NULL);
