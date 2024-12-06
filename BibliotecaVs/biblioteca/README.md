# Proyecto Biblioteca

Este proyecto es una aplicación de gestión de biblioteca que permite gestionar usuarios, libros y préstamos. Está implementado en Java utilizando la librería *FlatLaf* para la interfaz gráfica, sin el uso de JavaFX, y conecta a una base de datos *MySQL* para almacenar los usuarios y libros.

## Descripción

El sistema permite a los usuarios realizar las siguientes acciones:

- *Iniciar sesión* con un documento registrado previamente.
- *Registrar un nuevo usuario* ingresando su nombre y documento.
- *Ver libros disponibles* en la biblioteca.
- *Ver libros prestados* por el usuario.
- *Prestar un libro* si está disponible.
- *Devolver un libro* si el usuario ya lo ha prestado.

## Tecnologías utilizadas

- *Java*: Lenguaje de programación principal.
- *FlatLaf*: Librería de diseño para la interfaz gráfica.
- *MySQL*: Base de datos para almacenar información de usuarios, libros y préstamos.

## Requisitos previos

1. Tener instalado *Java 11 o superior* en tu máquina.
2. Tener un servidor *MySQL* en funcionamiento.
3. Tener acceso a un *IDE* (como Eclipse) para ejecutar el proyecto.

## Instalación

### 1. Configuración de MySQL

Primero, asegúrate de tener una base de datos llamada bibliotecabase. Luego, crea las siguientes tablas en tu base de datos:

```sql
CREATE DATABASE bibliotecabase;

USE bibliotecabase;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    documento VARCHAR(50) UNIQUE
);

CREATE TABLE libros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    autor VARCHAR(100),
    categoria VARCHAR(100),
    disponible BOOLEAN DEFAULT TRUE,
    imagen VARCHAR(255)
);

CREATE TABLE prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    id_libro INT,
    fecha_prestamo DATE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_libro) REFERENCES libros(id)
);
