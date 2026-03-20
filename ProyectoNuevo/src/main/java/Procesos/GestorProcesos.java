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
                // ¡Lo encontramos! Lo cambiamos de BLOQUEADO a LISTO
                // (Asumiendo que tienes Estado.LISTO en tu enum)
                p.setEstado(Estado.LISTO); 
                System.out.println("[Gestor] ¡Exito! El proceso " + p.getNombre() + " (ID: " + idProceso + ") ha vuelto al estado LISTO.");
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
    public static void iniciarCPU() {
        new Thread(() -> {
            while (true) {
                try {
                    // 1. Verificar si la CPU está libre (nadie en EJECUTANDO)
                    boolean cpuLibre = true;
                    for (int i = 0; i < cantidadProcesos; i++) {
                        if (listaProcesos[i] != null && listaProcesos[i].getEstado() == Estado.EJECUTANDO) {
                            cpuLibre = false;
                            break;
                        }
                    }
                    
                    // 2. Si la CPU está libre, buscamos al primer proceso LISTO
                    if (cpuLibre) {
                        for (int i = 0; i < cantidadProcesos; i++) {
                            if (listaProcesos[i] != null && listaProcesos[i].getEstado() == Estado.LISTO) {
                                
                                // ¡Encontramos uno! Lo metemos a la CPU
                                listaProcesos[i].setEstado(Estado.EJECUTANDO);
                                System.out.println("[CPU] " + listaProcesos[i].getNombre() + " entro a EJECUTANDO.");
                                
                                // Simulamos que la CPU lo procesa por 2 segundos
                                Thread.sleep(2000); 
                                
                                // Como es una tarea de archivos, lo manda a BLOQUEADO (Esperando al disco)
                                listaProcesos[i].setEstado(Estado.BLOQUEADO);
                                System.out.println("[CPU] " + listaProcesos[i].getNombre() + " necesita el Disco. Pasa a BLOQUEADO.");
                                
                                break; // Terminamos este ciclo para volver a empezar
                            }
                        }
                    }
                    
                    // Descanso de 1 segundo antes de volver a revisar la fila
                    Thread.sleep(1000); 
                    
                } catch (InterruptedException e) {
                    System.out.println("[CPU] Detenida.");
                    break;
                }
            }
        }).start();
    }
}

