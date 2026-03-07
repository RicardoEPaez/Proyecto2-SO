/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author Ramon-Carrasquel
 * @param <T>
 */
public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;
    private final Object bloqueoCola = new Object(); // Llave para evitar condiciones de carrera
    
    public Cola(){
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }
    
    // Definimos el metodo para agregar un elemento al final de la cola (encolar)
    public void encolar(T dato){
        synchronized(bloqueoCola){
            Nodo<T> nuevoNodo = new Nodo<>(dato);
            if (estaVacia()){
                frente = nuevoNodo;
                finalCola = nuevoNodo;
            }else{
                finalCola.setSiguiente(nuevoNodo);
                finalCola = nuevoNodo;
            }
            tamano++;
        }
    }
    
    /**
     * Metodo para extraer un elemento específico de la cola sin importar su posición.
     * Aunque rompe el comportamiento estandar de una cola, este método es vital para el Simulador.
     * Permite al Planificador retirar procesos especificos que hayan sido cancelados o requieran ser movidos de estado antes de llegar al frente.
     *
     * @param objetivo El elemento (por ejemplo, un Proceso) que se desea eliminar.
     * @return true si se encontró y sacó de la cola, false si no existe.
     */
    public boolean eliminar(T objetivo) {
        synchronized (bloqueoCola) {
            if (estaVacia()) {
                return false;
            }

            // Escenario A: El elemento a eliminar que buscamos esta al frente de la cola, es decir, es el primero.
            // Aprovechamos la lógica que ya existe en el metodo desencolar().
            if (frente.getContenido().equals(objetivo)) {
                desencolar();
                return true;
            }

            // Escenario B: El elemento esta oculto en el medio o al final de la cola.
            // Usamos dos punteros para recorrer y "puentear" el nodo a eliminar.
            Nodo<T> nodoAnterior = frente;
            Nodo<T> nodoActual = frente.getSiguiente();

            while (nodoActual != null) {
                if (nodoActual.getContenido().equals(objetivo)) {
                    // Desconectamos el nodo objetivo enlazando el anterior con el siguiente
                    nodoAnterior.setSiguiente(nodoActual.getSiguiente());

                    // Si resultaba ser el último nodo, corregimos el puntero final
                    if (nodoActual == finalCola) {
                        finalCola = nodoAnterior;
                    }

                    tamano--;
                    return true;
                }
                
                // Seguimos recorriendo la cola
                nodoAnterior = nodoActual;
                nodoActual = nodoActual.getSiguiente();
            }

            return false; // El elemento no se encontró en la cola.
        }
    }
    
    // Definimos el metodo para sacar el elemento del frente de la cola (desencolar)
    public T desencolar(){
        synchronized(bloqueoCola){
            if (estaVacia()){
                return null;
            }
            T dato = frente.getContenido();
            frente = frente.getSiguiente();
            if (frente == null){
                finalCola = null;
            }
            tamano--;
            return dato;
        }
    }
    
    // Para ver el primer elemento sin sacarlo de la cola
    public T obtenerFrente(){
        synchronized (bloqueoCola) {
            return estaVacia() ? null : frente.getContenido();
        }
    }
    
    // Para determinar si la cola esta vacia
    public boolean estaVacia(){
        synchronized (bloqueoCola) {
            return frente == null;
        }
        
    }
    
    // Para oonocer el tamano de la cola
    public int getTamano(){
        synchronized (bloqueoCola) {
            return tamano;
        }
    }
}
