/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

import org.json.JSONObject;
import java.util.concurrent.Semaphore;

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

    private int numLectores;
    private final Semaphore mutexLectores;
    private final Semaphore semaforoEscritura;
    
    
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
        
        this.numLectores =0;
        this.mutexLectores = new Semaphore(1);
        this.semaforoEscritura = new Semaphore(1);
    }
    
   // ==========================================================
    // MÉTODOS PARA LECTORES (Varios pueden entrar a la vez)
    // ==========================================================
    
    public void empezarLectura() {
        try {
            mutexLectores.acquire(); // Protegemos el contador
            numLectores++;
            if (numLectores == 1) {
                // Si soy el PRIMER lector, tranco la puerta para que ningún ESCRITOR entre.
                semaforoEscritura.acquire();
            }
            mutexLectores.release(); // Soltamos el contador para que otro lector pueda entrar
            
            System.out.println("[Archivo] Proceso leyendo " + this.getNombre() + ". Total lectores: " + numLectores);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void terminarLectura() {
        try {
            mutexLectores.acquire(); // Protegemos el contador
            numLectores--;
            System.out.println("[Archivo] Proceso dejó de leer " + this.getNombre() + ". Total lectores: " + numLectores);
            
            if (numLectores == 0) {
                // Si soy el ÚLTIMO lector en irme, quito el candado de escritura.
                semaforoEscritura.release();
            }
            mutexLectores.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // ==========================================================
    // MÉTODOS PARA ESCRITORES (Acceso totalmente exclusivo)
    // ==========================================================
    
    public void empezarEscritura() {
        try {
            System.out.println("[Archivo] Proceso solicitando ESCRIBIR en " + this.getNombre() + "...");
            // El escritor exige acceso exclusivo total. 
            // Si hay lectores leyendo u otro escritor, SE DUERME AQUÍ hasta que terminen.
            semaforoEscritura.acquire();
            System.out.println("[Archivo] Candado puesto. Escribiendo de forma EXCLUSIVA en " + this.getNombre());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void terminarEscritura() {
        System.out.println("[Archivo] Escritura terminada en " + this.getNombre() + ". Liberando archivo.");
        // Termina de escribir y abre la puerta para los demás
        semaforoEscritura.release(); 
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