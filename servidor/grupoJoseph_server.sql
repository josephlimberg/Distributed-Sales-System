
-- Base de datos:
CREATE DATABASE grupoJoseph_cloud;
USE grupoJoseph_cloud;

-- Base de datos:
CREATE TABLE cabecera_venta (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_tienda INT NOT NULL,
    fecha DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    id_cliente VARCHAR(20) NOT NULL,
    token VARCHAR(6) NOT NULL,
    token_validado BOOLEAN DEFAULT FALSE,
    token_expiracion DATETIME NOT NULL
);
