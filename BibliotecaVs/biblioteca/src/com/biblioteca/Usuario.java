package com.biblioteca;

import java.sql.Connection;

class Usuario {
    String nombre;
    String documento;
    int prestamosPendientes;
    int id;
    

    public Usuario(String nombre, String documento, int prestamosPendientes, int id){
        this.nombre = nombre;
        this.documento = documento;
        this.prestamosPendientes = prestamosPendientes;
        this.id = id;

    }
    public String getNombre(){
        return nombre;
    }

    public String getDocumento(){
        return documento;
    }
    

    public void prestarLibro(Libro libro){
        try {
            if (libro == null) {
                    throw new IllegalArgumentException("El libro no existe");

            }else if (!libro.disponible) {
                    throw new IllegalStateException("El libro: " +libro.nombre +" ya esta prestado.");
            }
            libro.disponible = false;
            prestamosPendientes++;
            System.out.println("El libro: " +libro.nombre +" ha sido prestado.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }

    }

    public void devolverLibro(Libro libro){
try {
    if (libro == null) {
        throw new IllegalArgumentException("El libro no existe");
        
    }else if(!libro.disponible){
        throw new IllegalStateException("El libro: " +libro.nombre +" no esta prestado.");
    }
    libro.disponible = true;
    prestamosPendientes --;
    System.out.println("El libro: " +libro.nombre +" ha sido devuelto");
    
} catch (IllegalArgumentException | IllegalStateException e) {
    System.out.println(e.getMessage());
}
    }
}