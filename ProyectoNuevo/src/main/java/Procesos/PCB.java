/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Procesos;

/**
 * Representa el Bloque Control de Procesos. Gestiona la identidad del proceso, su estado de ejecucion y mantiene la 
 * referencia a la solicitud de E/S que esta intentando realizar
 * 
 * @author ricar
 */

public class PCB {
    private static int contadorId = 0;

    private final int id;
    private String nombre;
    private Estado estado;
    private final SolicitudIO solicitudAsociada;
    
    public PCB(String nombre, SolicitudIO solicitud) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.solicitudAsociada = solicitud;
        this.estado = Estado.NUEVO;
    }

    public static int peekNextId() {
        return contadorId;
    }

    // Getters y Setters...
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public SolicitudIO getSolicitudAsociada() { return solicitudAsociada; }
    
    @Override
    public String toString() {
        return "P" + id + ": " + nombre + "(" + estado + ")";
    }
}