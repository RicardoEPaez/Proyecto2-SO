/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Procesos;

/**
 *
 * @author ricar
 */

/**
 * Actúa como el Gestor Principal del Sistema Operativo.
 * Maneja la lista de procesos (PCBs) usando un arreglo estático y recibe las interrupciones.
 */
public class GestorProcesos {
    
    // Usamos un arreglo fijo en lugar de ArrayList
    private static final int MAX_PROCESOS = 100;
    private static PCB[] listaProcesos = new PCB[MAX_PROCESOS];
    private static int cantidadProcesos = 0; // Contador para saber cuántos procesos hay

    // Método para agregar un nuevo proceso al arreglo
    public static void agregarProceso(PCB proceso) {
        if (cantidadProcesos < MAX_PROCESOS) {
            listaProcesos[cantidadProcesos] = proceso;
            cantidadProcesos++;
        } else {
            System.err.println("[Gestor] Error: Se alcanzó el límite máximo de procesos.");
        }
    }

    /**
     * ESTE ES EL REQUERIMIENTO 3: El método sincronizado (Interrupción)
     * El Planificador de Disco llamará a este método cuando termine su tarea.
     */
    public static synchronized void notificarFinIO(int idProceso) {
        System.out.println("[Interrupción] Recibida señal del disco. Buscando PCB ID: " + idProceso);
        
        // Buscamos el proceso iterando sobre nuestro arreglo
        for (int i = 0; i < cantidadProcesos; i++) {
            PCB p = listaProcesos[i];
            
            if (p != null && p.getId() == idProceso) {
                // ¡Lo encontramos! Lo cambiamos de BLOQUEADO a LISTO
                // (Asumiendo que tienes Estado.LISTO en tu enum)
                p.setEstado(Estado.LISTO); 
                System.out.println("[Gestor] ¡Éxito! El proceso " + p.getNombre() + " (ID: " + idProceso + ") ha vuelto al estado LISTO.");
                return; // Terminamos la búsqueda
            }
        }
        System.out.println("[Gestor] Error: No se encontró el proceso con ID " + idProceso);
    }
}

