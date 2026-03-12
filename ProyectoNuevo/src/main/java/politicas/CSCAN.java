/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

import Disco.PlanificadorDisco;
import Procesos.SolicitudIO;
import estructuras.Cola;
import estructuras.ListaEnlazada;

/**
 * Implementación de CSCAN
 * @author ricar
 */
public class CSCAN implements Planificacion {

    private PlanificadorDisco planificador;

    public CSCAN(PlanificadorDisco p) {
        this.planificador = p;
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, direccionScan direccionActual) {
        
        if (colaIO.estaVacia()) return null;
        
        ListaEnlazada<SolicitudIO> mayor = new ListaEnlazada<>();
        ListaEnlazada<SolicitudIO> menor = new ListaEnlazada<>();
        
        // 1. Separar las solicitudes
        while (!colaIO.estaVacia()) {
            SolicitudIO actual = colaIO.desencolar();
            if (actual.getBloqueObjetivo() < cabezalActual) {
                menor.agregar(actual);
            } else {
                mayor.agregar(actual);
            }
        }
        
        // 2. Forzar dirección ascendente en el disco
        if (planificador.getDireccion() == direccionScan.ABAJO) {
            planificador.setDireccion(direccionScan.ARRIBA);
        }
        
        // 3. Ordenar ambas listas (Bubble Sort usando tus métodos get y modificarEnIndice)
        ordenarLista(mayor);
        ordenarLista(menor);
        
        // 4. Lógica de Salto Circular
        if (mayor.estaVacia()) {
            planificador.setCabezal(0); // Reinicia el cabezal al inicio del disco
            mayor = menor;
            menor = null;
        }
        
        if (mayor == null || mayor.estaVacia()) return null;

        // 5. Extraer la primera solicitud de la lista 'mayor'
        SolicitudIO seleccionado = mayor.get(0);
        mayor.eliminar(seleccionado); // Usamos tu método eliminar(T dato)
        
        // 6. Devolver el resto de las solicitudes a la cola original
        // Primero re-encolamos las de 'mayor'
        for (int i = 0; i < mayor.getTamano(); i++) {
            colaIO.encolar(mayor.get(i));
        }
        
        // Luego re-encolamos las de 'menor'
        if (menor != null) {
            for (int i = 0; i < menor.getTamano(); i++) {
                colaIO.encolar(menor.get(i));
            }
        }
        
        return seleccionado;
    }

    /**
     * Ordena la lista de solicitudes de menor a mayor bloque objetivo.
     * Utiliza el método modificarEnIndice de tu clase ListaEnlazada.
     */
    private void ordenarLista(ListaEnlazada<SolicitudIO> lista) {
        int n = lista.getTamano();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (lista.get(j).getBloqueObjetivo() > lista.get(j + 1).getBloqueObjetivo()) {
                    SolicitudIO temp = lista.get(j);
                    lista.modificarEnIndice(j, lista.get(j + 1));
                    lista.modificarEnIndice(j + 1, temp);
                }
            }
        }
    }
}