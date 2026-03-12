/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

import Procesos.SolicitudIO;
import estructuras.Cola;
import estructuras.ListaEnlazada;

/**
 * Implementación de la política de planificación de disco SCAN (Algoritmo del Elevador).
 * Esta clase simula el movimiento de un elevador: el cabezal se desplaza en una 
 * dirección específica (ARRIBA o ABAJO) atendiendo todas las solicitudes que 
 * encuentre en su trayectoria hasta que no queden más en ese sentido. 
 * Es una política eficiente que evita cambios bruscos de dirección y reduce el 
 * tiempo medio de respuesta en comparación con FIFO.
 * @author ricar
 */
public class SCAN implements Planificacion {
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, direccionScan direccionActual) {
        if (colaIO.estaVacia()) return null;

        ListaEnlazada<SolicitudIO> temp = new ListaEnlazada<>();
        while (!colaIO.estaVacia()) {
            temp.agregar(colaIO.desencolar());
        }

        SolicitudIO mejorOpcion = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.get(i);
            int bloque = actual.getBloqueObjetivo();

            // Lógica SCAN: Solo atiende si está en la dirección actual
            boolean enDireccion = (direccionActual == direccionScan.ARRIBA && bloque >= cabezalActual) ||
                                 (direccionActual == direccionScan.ABAJO && bloque <= cabezalActual);

            if (enDireccion) {
                int distancia = Math.abs(bloque - cabezalActual);
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    mejorOpcion = actual;
                }
            }
        }

        // Si no encontró nada en esa dirección, hay que devolver todo y 
        // el simulador debería cambiar la dirección (el "rebote").
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.get(i);
            if (actual != mejorOpcion) {
                colaIO.encolar(actual);
            }
        }

        return mejorOpcion;
    }
}
