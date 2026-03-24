/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Procesos;

import Disco.PlanificadorDisco;
import estructuras.Cola;

/**
 * Representa el hilo de ejecución de un proceso de usuario (Productor).
 * Simula a un proceso real ejecutándose en CPU que, en algún momento, necesita solicitar una operación de Entrada/Salida al disco.
 * @author Ramon-Carrasquel
 */
public class HiloProceso implements Runnable {
    
    private PCB proceso;
    private Cola<SolicitudIO> colaCompartida;
    private PlanificadorDisco disco;
    private Interfaz.InterfazProyecto interfaz;

    /**
     * Constructor del hilo.
     * @param proceso El PCB con la información del proceso y su solicitud.
     * @param colaCompartida La misma cola que lee el PlanificadorDisco.
     * @param disco Referencia al disco para poder "despertarlo".
     * @param interfaz para conectar con la interfaz del proyecto
     */
    public HiloProceso(PCB proceso, Cola<SolicitudIO> colaCompartida, PlanificadorDisco disco, Interfaz.InterfazProyecto interfaz) {
        this.proceso = proceso;
        this.colaCompartida = colaCompartida;
        this.disco = disco;
        this.interfaz = interfaz; // NUEVO
    }

    @Override
    public void run() {
        try {
            // 1. Simulación de ráfaga de CPU
            // El proceso pasa a EJECUTANDO simulando que el SO le dio CPU
            proceso.setEstado(Estado.EJECUTANDO);
            interfaz.actualizarPantallaProcesos();
            System.out.println("[CPU] El proceso " + proceso.getNombre() + " (PID: " + proceso.getId() + ") está ejecutándose.");
            
            // Hacemos que el hilo duerma un tiempo aleatorio entre 100ms y 1000ms.
            // Esto es CLAVE para la concurrencia: garantiza que si lanzas 5 procesos al mismo tiempo, 
            // no lleguen exactamente en el mismo milisegundo a la cola, simulando un entorno real.
            int tiempoCPU = (int) (Math.random() * 900) + 100;
            Thread.sleep(tiempoCPU);

            // 2. Generación de la Solicitud de E/S
            SolicitudIO peticion = proceso.getSolicitudAsociada();
            
            if (peticion != null) {
                System.out.println(">>> [Proceso " + proceso.getId() + "] Requiere I/O: " 
                        + peticion.getTipo() + " en bloque " + peticion.getBloqueObjetivo() + " <<<");
                
                // 3. SECCIÓN CRÍTICA: Encolar la petición (Productor)
                // Tu clase Cola.java ya maneja los semáforos internos (mutex), así que esto es seguro.
                colaCompartida.encolar(peticion);
                
                // 4. Despertar al Disco
                // Le avisamos al disco que hay un nuevo cliente en la fila.
                disco.registrarNuevaPeticion();
                
                // 5. Cambio de Estado
                // Como las operaciones de disco son lentas, el proceso cede la CPU y se bloquea.
                proceso.setEstado(Estado.BLOQUEADO);
                interfaz.actualizarPantallaProcesos();
                System.out.println("[Gestor] Proceso " + proceso.getId() + " pasa a BLOQUEADO esperando al disco.");
                
            } else {
                // Si por alguna razón el proceso no tenía solicitud, termina directamente
                proceso.setEstado(Estado.TERMINADO);
                System.out.println("[Gestor] Proceso " + proceso.getId() + " termino su ejecución sin requerir I/O.");
            }

        } catch (InterruptedException e) {
            System.err.println("[Proceso " + proceso.getId() + "] Fue interrumpido forzosamente.");
            Thread.currentThread().interrupt(); // Restablece el estado de interrupción
        }
    }
}
