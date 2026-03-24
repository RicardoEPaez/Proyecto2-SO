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

        SolicitudIO mejorOpcion = buscarMejor(temp, cabezalActual, direccionActual);

        // --- EL ARREGLO ESTÁ AQUÍ ---
        // Si no hubo nada en la dirección original (ej. llegamos a 180 y no hay más arriba)
        if (mejorOpcion == null) {
            // Cambiamos la dirección mentalmente para la segunda búsqueda
            direccionScan nuevaDireccion = (direccionActual == direccionScan.ARRIBA) ? 
                                            direccionScan.ABAJO : direccionScan.ARRIBA;
            
            mejorOpcion = buscarMejor(temp, cabezalActual, nuevaDireccion);
        }

        // Devolvemos el resto a la cola
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO s = temp.get(i);
            if (s != mejorOpcion) {
                colaIO.encolar(s);
            }
        }

        return mejorOpcion;
    }

    // Método auxiliar para no repetir código de búsqueda
    private SolicitudIO buscarMejor(ListaEnlazada<SolicitudIO> lista, int cabezal, direccionScan dir) {
        SolicitudIO mejor = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (int i = 0; i < lista.getTamano(); i++) {
            SolicitudIO actual = lista.get(i);
            int bloque = actual.getBloqueObjetivo();

            boolean enDireccion = (dir == direccionScan.ARRIBA && bloque >= cabezal) ||
                                  (dir == direccionScan.ABAJO && bloque <= cabezal);

            if (enDireccion) {
                int distancia = Math.abs(bloque - cabezal);
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    mejor = actual;
                }
            }
        }
        return mejor;
    }
}