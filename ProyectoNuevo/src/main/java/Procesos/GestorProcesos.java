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
    private static final int MAX_PROCESOS = 250;
    private static PCB[] listaProcesos = new PCB[MAX_PROCESOS];
    private static int cantidadProcesos = 0; // Contador para saber cuántos procesos hay
    public static volatile int velocidadSimulacion = 1000;
    
    // --- NUEVAS VARIABLES PARA LA PAUSA ---
    private static volatile boolean cpuPausada = false;
    private static final Object lockPausaCPU = new Object();
    
    // Método para agregar un nuevo proceso al arreglo
    public static void agregarProceso(PCB proceso) {
        if (cantidadProcesos < MAX_PROCESOS) {
            listaProcesos[cantidadProcesos] = proceso;
            cantidadProcesos++;
        } else {
            System.err.println("[Gestor] Error: Se alcanzo el limite maximo de procesos.");
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
                
                // --- CAMBIO AQUI ---
                // Lo pasamos a TERMINADO para que la CPU lo deje en paz
                p.setEstado(Estado.TERMINADO); 
                System.out.println("[Gestor] ¡Exito! El proceso " + p.getNombre() + " (ID: " + idProceso + ") ha TERMINADO su ejecución.");
                
                return; // Terminamos la búsqueda
            }
        }
        System.out.println("[Gestor] Error: No se encontró el proceso con ID " + idProceso);
    }
    
    // MÉTODOS NUEVOS PARA LA INTERFAZ GRÁFICA
    
    /**
     * Recorre el arreglo de procesos y devuelve un String con la información
     * de todos los procesos que coincidan con el estado solicitado.
     * @param estadoBuscado El estado que queremos filtrar (ej. Estado.LISTO)
     * @return String formateado para mostrar en el JTextArea
     */
    public static String obtenerListadoPorEstado(Estado estadoBuscado) {
        StringBuilder sb = new StringBuilder();
        boolean hayProcesos = false;
        
        for (int i = 0; i < cantidadProcesos; i++) {
            PCB p = listaProcesos[i];
            if (p != null && p.getEstado() == estadoBuscado) {
                // Añadimos el proceso al texto. Llama automáticamente a p.toString()
                sb.append("   - ").append(p.toString()).append("\n");
                hayProcesos = true;
            }
        }
        
        if (!hayProcesos) {
            sb.append("   (Vacío)\n");
        }
        
        return sb.toString();
    }
    
    // LA CPU (PLANIFICADOR VIRTUAL)  
    // LA CPU (PLANIFICADOR VIRTUAL)  
    public static void iniciarCPU() {
        // ¡MÉTODO DESACTIVADO! 
        // Ya no usamos este ciclo infinito porque en la "Opción B"
        // cada petición lanza su propio HiloProceso que simula la CPU
        // de forma correcta sin causar condiciones de carrera.
        System.out.println("[Gestor] Hilo de CPU central desactivado. Usando Hilos de Proceso individuales.");
    }
    
    // --- GETTERS NECESARIOS PARA EL REPORTE CSV ---
    
    public static PCB[] getTodosLosProcesos() {
        return listaProcesos;
    }

    public static int getCantidadProcesos() {
        return cantidadProcesos;
    }
    
    // --- NUEVOS MÉTODOS PARA CONTROLAR LA PAUSA ---
    public static void pausarSimulacion() {
        cpuPausada = true;
    }
    
    public static void reanudarSimulacion() {
        cpuPausada = false;
        synchronized (lockPausaCPU) {
            lockPausaCPU.notifyAll(); // ¡Despierta a la CPU!
        }
    }
    
    private static void verificarPausaCPU() {
        synchronized (lockPausaCPU) {
            while (cpuPausada) {
                try {
                    // La CPU se "duerme" aquí sin consumir recursos de tu PC real
                    lockPausaCPU.wait(); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    }

