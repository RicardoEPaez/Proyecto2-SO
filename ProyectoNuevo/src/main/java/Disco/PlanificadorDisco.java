/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Disco;

import Journaling.GestorJournaling;
import Procesos.GestorProcesos;
import Procesos.SolicitudIO;
import estructuras.Cola;
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
    
    public PlanificadorDisco(Cola<SolicitudIO> colaCompartida) {
        // Por defecto, empezamos con FIFO y en la posición 0
        this.politicaActual = new FIFO();
        this.cabezalActual = 0;
        this.direccionActual = direccionScan.ARRIBA;
        
        this.colaCompartida = colaCompartida;
        
        this.enFuncionamiento = true;
    }
    
    
    // ==========================================
    // EL MOTOR DEL DISCO (HILO CONSUMIDOR)
    // ==========================================
    @Override
    public void run() {
        System.out.println("[Hardware] Disco encendido y esperando peticiones...");
        
        while (enFuncionamiento) {
            try {
                // Revisamos de forma segura si la cola tiene solicitudes
                if (!colaCompartida.estaVacia()) {
                    
                    // Extraemos la solicitud aplicando tu política actual
                    SolicitudIO seleccionada = seleccionarSiguiente(colaCompartida);
                    
                    if (seleccionada != null) {
                        int destino = seleccionada.getBloqueObjetivo();
                        Journaling.GestorJournaling.registrarOperacion(new Journaling.RegistroJournal(
                            seleccionada.getTipo().toString(), 
                            seleccionada.getRuta(), 
                            seleccionada.getBloqueObjetivo()
                        ));
                        System.out.println("[Disco] Moviendo cabezal a bloque " + destino + " (Usando: " + getPoliticaActual() + ")");
                        Journaling.GestorJournaling.confirmarOperacion();
                        
                        // SIMULACIÓN DE TIEMPO FÍSICO: El brazo del disco se mueve (500ms)
                        Thread.sleep(500); 
                        
                        
                        System.out.println("[Disco] Operación en bloque " + destino + " finalizada.");
                        // NOTA: Aquí es donde (más adelante) le avisarás al PCB que pase de BLOQUEADO a LISTO.
                        GestorProcesos.notificarFinIO(seleccionada.getIdProceso());
                    }
                } else {
                    // Si no hay peticiones, el disco descansa un momento para no saturar la CPU
                    Thread.sleep(100);
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
