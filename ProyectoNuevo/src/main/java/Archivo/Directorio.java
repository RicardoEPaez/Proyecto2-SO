/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

import org.json.JSONArray; 
import org.json.JSONObject; 
import estructuras.ListaEnlazada;

/**
 * Representa una carpeta en el simulador. 
 * Actúa como un contenedor que puede almacenar múltiples archivos u otros subdirectorios.
 * @author Ramon-Carrasquel
 */
public class Directorio extends EntradaSistemaArchivos {
    
    private ListaEnlazada<EntradaSistemaArchivos> contenido;

    /**
     * Crea un nuevo directorio.
     * @param nombre El nombre asignado a la carpeta.
     * @param dirPadre El directorio que lo contiene (null si es la raíz).
     */
    public Directorio(String nombre, Directorio dirPadre) {
        super(nombre, dirPadre);
        this.contenido = new ListaEnlazada<>();
    }

    /**
     * Elimina un elemento enviando la referencia directa del objeto.
     * @param entrada El archivo o subdirectorio a remover.
     * @return true si se eliminó exitosamente, false si no se encontró.
     */
    public boolean eliminarEntrada(EntradaSistemaArchivos entrada) {
        return this.getContenido().eliminar(entrada);
    }

    /**
     * Busca y elimina un elemento dentro de esta carpeta usando su nombre.
     * @param nombre El nombre exacto de la entrada a eliminar.
     * @return true si fue hallado y borrado, false en caso contrario.
     */
    public boolean eliminarEntrada(String nombre) {
        for (int i = 0; i < getContenido().getTamano(); i++) {
            EntradaSistemaArchivos entradaActual = getContenido().get(i);
            if (entradaActual.getNombre().equals(nombre)) {
                return getContenido().eliminar(entradaActual);
            }
        }
        return false; 
    }
    
    /**
     * Comprueba si ya existe un elemento con el nombre indicado (ignorando mayúsculas).
     * @param nombre El nombre a verificar.
     * @return true si el nombre ya está en uso, false si está libre.
     */
    public boolean contieneNombre(String nombre) {
        for (int i = 0; i < this.getContenido().getTamano(); i++) {
            if (this.getContenido().get(i).getNombre().equalsIgnoreCase(nombre)) {
                return true; 
            }
        }
        return false; 
    }

    /**
     * Agrega un nuevo archivo o subdirectorio verificando que no haya colisión de nombres.
     * @param entrada El objeto a insertar.
     * @return true si se agregó correctamente, false si el nombre ya existía.
     */
    public boolean agregarEntrada(EntradaSistemaArchivos entrada) {
        if (this.contieneNombre(entrada.getNombre())) {
            System.err.println("Error: Ya existe una entrada con el nombre '" + entrada.getNombre() + "' en el directorio '" + this.getNombre() + "'.");
            return false;
        }
        
        this.getContenido().agregar(entrada);
        return true;
    }

    public ListaEnlazada<EntradaSistemaArchivos> getContenido() {
        return contenido;
    }
    
    /**
     * Serializa recursivamente este directorio y todo su árbol de contenido.
     * @return Objeto JSON estructurado.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("tipo", "DIRECTORIO");
        json.put("nombre", this.getNombre());

        JSONArray contenidoJson = new JSONArray();
        for (int i = 0; i < this.contenido.getTamano(); i++) {
            EntradaSistemaArchivos entrada = this.contenido.get(i);
            
            // Evalúa el tipo de 'entrada' y hace el cast automáticamente a 'dir' o 'arch'
            switch (entrada) {
                case Directorio dir -> contenidoJson.put(dir.toJson());
                case Archivo arch -> contenidoJson.put(arch.toJson());
                default -> { } // No hace nada si por alguna razón no es ni archivo ni directorio
            }
        }
        json.put("contenido", contenidoJson);
        
        return json;
    }
}
