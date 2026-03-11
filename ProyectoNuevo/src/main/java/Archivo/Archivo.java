/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

import org.json.JSONObject;

/**
 * Representa un archivo individual dentro del Sistema de Archivos Simulado.
 * Almacena la metadata necesaria para su ubicación en el disco virtual y su renderizado en la interfaz.
 * @author Ramon-Carrasquel
 */
public class Archivo extends EntradaSistemaArchivos {
    
    private int tamanoEnBloques;
    private int primerBloque;
    private int idProcesoCreador;
    private String color; // Para la UI

    /**
     * Constructor principal del archivo.
     * @param nombre El nombre exacto del archivo con su extensión.
     * @param dirPadre El directorio donde estará contenido este archivo.
     * @param tamanoEnBloques La cantidad de bloques que ocupará en el disco virtual.
     * @param primerBloque El índice del bloque inicial en la tabla de asignación.
     * @param idProcesoCreador El ID (PID) del proceso que solicitó su creación.
     * @param color El color asignado para representarlo visualmente en la UI.
     */
    public Archivo(String nombre, Directorio dirPadre, int tamanoEnBloques, int primerBloque, int idProcesoCreador, String color) {
        super(nombre, dirPadre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
        this.idProcesoCreador = idProcesoCreador;
        this.color = color;
    }
    
    /**
     * Serializa la metadata del archivo a formato JSON para la persistencia.
     * Mantiene las claves estándar para compatibilidad con el GestorJSON.
     * @return Objeto JSON con las propiedades del archivo.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        
        // Construimos el JSON.
        json.put("tipo", "ARCHIVO");
        json.put("nombre", super.getNombre()); // Usamos super para denotar herencia
        json.put("tamanoEnBloques", this.tamanoEnBloques);
        json.put("primerBloque", this.primerBloque);
        json.put("idProcesoCreador", this.idProcesoCreador);
        json.put("color", this.color);
        
        return json;
    }
    
    // Getters y Setters
    public int getTamanoEnBloques() { 
        return tamanoEnBloques; 
    }
    
    public int getPrimerBloque() { 
        return primerBloque; 
    }
    
    public int getIdProcesoCreador() { 
        return idProcesoCreador; 
    }
    
    public String getColor() { 
        return color; 
    }
    
    public void setPrimerBloque(int bloque) {
        this.primerBloque = bloque;
    }
}