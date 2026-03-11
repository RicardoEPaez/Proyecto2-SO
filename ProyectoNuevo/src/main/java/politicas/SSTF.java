/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

import Procesos.SolicitudIO;
import estructuras.Cola;
import estructuras.ListaEnlazada;

/**
 * Implementación de la política de planificación de disco SSTF (Shortest Seek Time First - Tiempo de Búsqueda Más Corto Primero).
 * Esta clase selecciona la solicitud de entrada/salida que se encuentra más cerca de la 
 * posición actual del cabezal del disco, minimizando el movimiento y el tiempo de búsqueda.
 * Aunque es mucho más eficiente que FIFO en términos de rendimiento, puede llegar a causar 
 * inanición (starvation) en solicitudes que estén muy lejos si siguen llegando solicitudes cercanas.
 * @author ricar
 */
public class SSTF implements Planificacion{
    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, direccionScan direccionActual) {
        if (colaIO.estaVacia()) return null;

        // Vaciamos la cola a una lista temporal para poder iterarla
        ListaEnlazada<SolicitudIO> temp = new ListaEnlazada<>();
        while (!colaIO.estaVacia()) {
            temp.agregar(colaIO.desencolar());
        }

        SolicitudIO mejorOpcion = null;
        int minimaDistancia = Integer.MAX_VALUE;

        // Buscamos la solicitud con la menor distancia al cabezal
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.get(i);
            int distancia = Math.abs(actual.getBloqueObjetivo() - cabezalActual);
            if (distancia < minimaDistancia) {
                minimaDistancia = distancia;
                mejorOpcion = actual;
            }
        }

        // Volvemos a encolar todas las solicitudes excepto la elegida
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.get(i);
            if (actual != mejorOpcion) {
                colaIO.encolar(actual);
            }
        }

        return mejorOpcion;
    }
}
