/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 * Clase encargada exclusivamente de administrar la lógica del Sistema de Archivos (El CRUD completo).
 * Maneja la jerarquía de carpetas y archivos, independiente del almacenamiento físico.
 * @author Ramon-Carrasquel
 */
public class GestorArchivos {
    
    private Directorio directorioRaiz;

    public GestorArchivos() {
        // Inicializa un árbol de directorios vacío, sin importar de qué tamaño es el disco.
        this.directorioRaiz = new Directorio("Raiz", null);
    }

    // OPERACIONES CRUD (Simulación puramente Lógica)

    // CREATE (Archivo): Crea un archivo lógico en el árbol de directorios.
    public boolean crearArchivo(String nombre, Directorio padre, int tamano, int bloqueArranque, int idProceso, String color) {
        if (padre.contieneNombre(nombre)) return false;
        
        Archivo nuevoArchivo = new Archivo(nombre, padre, tamano, bloqueArranque, idProceso, color);
        return padre.agregarEntrada(nuevoArchivo);
    }

    // CREATE (Directorio): Crea una nueva subcarpeta.
    public boolean crearDirectorio(String nombre, Directorio padre) {
        if (padre.contieneNombre(nombre)) return false;
        
        Directorio nuevoDir = new Directorio(nombre, padre);
        return padre.agregarEntrada(nuevoDir);
    }

    // UPDATE (MODIFICAR)
    // Cambia el nombre de un archivo o directorio existente.
    public boolean renombrarEntrada(EntradaSistemaArchivos entrada, String nuevoNombre) {
        Directorio padre = entrada.getPadre();
        if (padre != null && padre.contieneNombre(nuevoNombre)) {
            return false; // Ya existe algo con ese nombre en esta carpeta
        }
        entrada.setNombre(nuevoNombre);
        return true;
    }

    // DELETEE Elimina el archivo lógicamente del directorio.
    // NOTA: El espacio físico en disco deberá ser liberado por quien llame a este método.
    public boolean eliminarArchivo(Archivo archivoAEliminar) {
        return archivoAEliminar.getPadre().eliminarEntrada(archivoAEliminar);
    }

    // DELETE (Directorio): Elimina un directorio y todo su contenido de forma recursiva.
    public boolean eliminarDirectorio(Directorio dirAEliminar) {
        if (dirAEliminar == directorioRaiz) return false; // No se puede borrar la raíz
        
        // Vaciamos el contenido recursivamente
        while (dirAEliminar.getContenido().getTamano() > 0) {
            EntradaSistemaArchivos entradaActual = dirAEliminar.getContenido().get(0);
            
            switch (entradaActual) {
                case Archivo arch -> eliminarArchivo(arch);
                case Directorio subDir -> eliminarDirectorio(subDir);
                default -> {}
            }
        }
        
        return dirAEliminar.getPadre().eliminarEntrada(dirAEliminar);
    }
    
    // GETTERS
    public Directorio getDirectorioRaiz() { 
        return directorioRaiz; 
    }
}
