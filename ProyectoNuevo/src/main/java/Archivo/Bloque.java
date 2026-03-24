/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 * Representa la unidad mínima de almacenamiento dentro del Disco Simulado (SD).
 * Esta clase es fundamental para implementar la política de "Asignación Encadenada", ya que cada bloque guarda la referencia al siguiente bloque del archivo.
 * @author Ramon-Carrasquel
 */
public class Bloque {
    
    // Constante que indica que este bloque es el final de la cadena de un archivo
    public static final int FIN_DE_ARCHIVO = -1;

    private int id;
    private boolean ocupado;
    private int siguienteBloque; 
    private int idProceso; // Esto es para saber el proceso que esta ocupando

    /**
     * Constructor para inicializar un bloque del disco virtual.
     * Por defecto, un bloque nace libre, sin apuntar a otro bloque y sin proceso dueño.
     * @param id El índice o identificador único de este bloque en la matriz del disco.
     */
    public Bloque(int id) {
        this.id = id;
        this.ocupado = false;
        this.siguienteBloque = FIN_DE_ARCHIVO;
        this.idProceso = -1; // el -1 es porque no hay un proceso asignado
    }
    
    /**
     * Asigna este bloque a un proceso específico, marcándolo como ocupado en el disco.
     * @param idProceso El ID del proceso que está ocupando este bloque.
     */
    public void ocupar(int idProceso) {
        this.ocupado = true;
        this.idProceso = idProceso;
    }

    /**
     * Libera el bloque, reseteando su estado para que pueda ser usado por otro archivo.
     * Rompe cualquier enlace de la asignación encadenada y elimina el proceso dueño.
     */
    public void liberar() {
        this.ocupado = false;
        this.siguienteBloque = FIN_DE_ARCHIVO;
        this.idProceso = -1;
    }
    
    /**
     * Verifica si este bloque representa el final de un archivo.
     * Muy útil al momento de recorrer la cadena de bloques para leer o eliminar.
     * @return true si no hay un bloque siguiente (es el último), false en caso contrario.
     */
    public boolean esUltimoBloque() {
        return this.siguienteBloque == FIN_DE_ARCHIVO;
    }
    
    // GETTERS Y SETTERS
    public int getId() { 
        return id; 
    }
    
    public boolean isOcupado() { 
        return ocupado; 
    }
    
    public int getSiguienteBloque() { 
        return siguienteBloque; 
    }
    
    public void setSiguienteBloque(int siguienteBloque) { 
        this.siguienteBloque = siguienteBloque; 
    }
    
    public int getIdProceso() { 
        return idProceso; 
    }
}
