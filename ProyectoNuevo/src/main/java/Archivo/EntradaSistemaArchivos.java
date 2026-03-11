/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 * Clase base que representa cualquier elemento dentro del árbol del sistema de archivos.
 * Define las propiedades fundamentales compartidas entre Archivos y Directorios, permitiendo tratarlos de manera uniforme (Polimorfismo).
 * @author Ramon-Carrasquel
 */

public abstract class EntradaSistemaArchivos {
    
    // Visibilidad 'protected' para que las clases hijas (Archivo y Directorio) hereden y puedan acceder directamente a estos atributos.
    protected String nombre;
    protected Directorio dirPadre;

    /**
     * Constructor base para inicializar la entrada.
     * @param nombre El nombre identificador de la entrada.
     * @param dirPadre El directorio que contiene a esta entrada (null si es la raíz).
     */
    public EntradaSistemaArchivos(String nombre, Directorio dirPadre) {
        this.nombre = nombre;
        this.dirPadre = dirPadre;
    }

    // GETTERS Y SETTERS
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Directorio getPadre() {
        return dirPadre;
    }

    // Para que el JTree muestre el nombre
    @Override
    public String toString() {
        return nombre;
    }
}
