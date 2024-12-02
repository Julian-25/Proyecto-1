package com.biblioteca;
class Libro {
    int idlibro;
    String nombre;
    String autor;
    String categoria;
    boolean disponible;

    public Libro(int id_libro, String nombre, String autor, String categoria, boolean disponible){
        this.idlibro = id_libro;
        this.nombre = nombre;
        this.autor = autor;
        this.categoria = categoria;
        this.disponible = true;
    }
public void cambiarDisponibilidad(boolean disponible){
    this.disponible = disponible;
}
@Override
    public String toString(){
        return "Nombre: " + nombre + ", Autor: " + autor + ", Categoría: " + categoria + ", Disponible: " + (disponible ? "Sí" : "No");
    }
}
