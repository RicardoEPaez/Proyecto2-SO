/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

import Procesos.SolicitudIO;
import estructuras.Cola;

/**
 * Interfaz que define el contrato para las políticas de planificación de disco.
 * Cualquier algoritmo de planificación (como FIFO, SSTF, SCAN, C-SCAN) debe 
 * implementar esta interfaz para garantizar que el sistema operativo simulado 
 * pueda solicitar la siguiente operación de E/S de manera estandarizada.
 * @author ricar
 */
public interface Planificacion {
    SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, direccionScan direccionActual);
}
