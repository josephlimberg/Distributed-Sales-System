CREATE DATABASE grupoJoseph_local;
USE grupoJoseph_local;

-- Base de datos: 
CREATE TABLE detalle_venta (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,  -- Relación con cabecera en nube
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL
);