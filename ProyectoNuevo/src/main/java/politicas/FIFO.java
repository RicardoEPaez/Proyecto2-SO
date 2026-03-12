/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

import Procesos.SolicitudIO;
import estructuras.Cola;

/**
 * Implementación de la política de planificación de disco FIFO (First In, First Out).
 * @author ricar
 */
public class FIFO implements Planificacion{
     @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, direccionScan direccionActual) {
        return colaIO.desencolar();
    } 
}
