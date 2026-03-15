/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Disco;

import Procesos.GestorProcesos;
import Procesos.SolicitudIO;
import estructuras.Cola;
import java.util.concurrent.Semaphore;
import politicas.FIFO;
import politicas.Planificacion;
import politicas.direccionScan;

/**
 *
 * @author ricar
 */
public class PlanificadorDisco implements Runnable {
    private Planificacion politicaActual;
    
    // Estado del disco que las políticas necesitan conocer
    private int cabezalActual;
    private direccionScan direccionActual;
    public static final int MAX_BLOQUES = 100; // El tamaño total del disco

    private Cola<SolicitudIO> colaCompartida;
    private volatile boolean enFuncionamiento;
    private Semaphore semaforoPeticiones;
    
    public PlanificadorDisco(Cola<SolicitudIO> colaCompartida) {
        // Por defecto, empezamos con FIFO y en la posición 0
        this.politicaActual = new FIFO();
        this.cabezalActual = 0;
        this.direccionActual = direccionScan.ARRIBA;
        
        this.colaCompartida = colaCompartida;
        this.semaforoPeticiones = new Semaphore(0);
        this.enFuncionamiento = true;
    }
    
    
    public void registrarNuevaPeticion() {
        semaforoPeticiones.release(); // Aumenta el semáforo en 1 y despierta al disco
    }
    
    
     // ==========================================
    // EL MOTOR DEL DISCO (HILO CONSUMIDOR)
    // ==========================================
    @Override
    public void run() {
        System.out.println("[Hardware] Disco encendido y esperando peticiones...");
        
        while (enFuncionamiento) {
            try {
                // NUEVO: El disco se queda DORMIDO aquí sin gastar CPU hasta que alguien haga release()
                semaforoPeticiones.acquire(); 
                
                // Si despertó, es porque seguro hay algo en la cola
                SolicitudIO seleccionada = seleccionarSiguiente(colaCompartida);
                
                if (seleccionada != null) {
                    int destino = seleccionada.getBloqueObjetivo();
                    Journaling.GestorJournaling.registrarOperacion(new Journaling.RegistroJournal(
                        seleccionada.getTipo().toString(), 
                        seleccionada.getRuta(), 
                        seleccionada.getBloqueObjetivo()
                    ));
                    
                    System.out.println("[Disco] Moviendo cabezal a bloque " + destino + " (Usando: " + getPoliticaActual() + ")");
                    
                    // SIMULACIÓN DE TIEMPO FÍSICO (Movimiento del brazo)
                    Thread.sleep(500); 
                    
                    // CORRECCIÓN: El commit ahora está DESPUÉS del movimiento físico
                    Journaling.GestorJournaling.confirmarOperacion();
                    
                    System.out.println("[Disco] Operación en bloque " + destino + " finalizada.");
                    
                    // Notifica al PCB
                    GestorProcesos.notificarFinIO(seleccionada.getIdProceso());
                }
                
            } catch (InterruptedException e) {
                System.err.println("[Hardware] Disco interrumpido de emergencia.");
                enFuncionamiento = false;
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[Hardware] Disco apagado.");
    }
    
    /**
     * Cambia la estrategia de planificación en tiempo de ejecución.
     * @param nuevaPolitica La nueva política a utilizar.
     */
    public void setPolitica(Planificacion nuevaPolitica) {
        this.politicaActual = nuevaPolitica;
        System.out.println("Política de planificación cambiada a: " + nuevaPolitica.getClass().getSimpleName());
    }

    /**
     * Delega a la política actual la selección de la siguiente solicitud de la cola.
     * @param colaIO La cola de solicitudes pendientes.
     * @return La solicitud elegida, o null si no hay ninguna.
     */
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO) {
        if (colaIO.estaVacia()) {
            return null;
        }
        
        SolicitudIO seleccionada = politicaActual.seleccionarSiguiente(colaIO, this.cabezalActual, this.direccionActual);
        
        // Una vez seleccionada, actualizamos el estado del disco
        if (seleccionada != null) {
            // Actualizamos la posición del cabezal
            int bloqueDestino = seleccionada.getBloqueObjetivo();
            
            // Actualizamos la dirección (para SCAN/CSCAN)
            if (bloqueDestino > this.cabezalActual) {
                this.direccionActual = direccionScan.ARRIBA;
            } else if (bloqueDestino < this.cabezalActual) {
                this.direccionActual = direccionScan.ABAJO;
            }
            
            // Si llega a un extremo, SCAN/CSCAN lo gestionarán internamente
            if (this.cabezalActual == 0) this.direccionActual = direccionScan.ARRIBA;
            if (this.cabezalActual == MAX_BLOQUES -1) this.direccionActual = direccionScan.ABAJO;
            
            this.cabezalActual = bloqueDestino;
        }
        
        return seleccionada;
    }
    
    public void detenerDisco(){
        this.enFuncionamiento = false;
    }
    
    
    public void setDireccion(direccionScan direccion) {
        this.direccionActual = direccion;
    }
    
    public direccionScan getDireccion() {
        return this.direccionActual;
    }
    
    public void setCabezal(int posicion) {
        this.cabezalActual = posicion;
    }
    
    // Getters para que la UI pueda mostrar el estado
    public int getCabezalActual() { return cabezalActual; }
    public String getPoliticaActual() { return this.politicaActual.getClass().getSimpleName(); }
}
