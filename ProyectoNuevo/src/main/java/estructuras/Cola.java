/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;
import java.util.concurrent.Semaphore;
        
/**
 *
 * @author Ramon-Carrasquel
 * @param <T>
 */
public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;
    
    private final Semaphore mutex;
    
    private final Semaphore elementosDisponibles;
    
    public Cola(){
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
        
        this.mutex = new Semaphore(1);
        this.elementosDisponibles = new Semaphore(0);
    }
    
    /// Definimos el metodo para agregar un elemento al final de la cola (encolar)
    public void encolar(T dato){
        try {
            mutex.acquire(); // Pedimos permiso para modificar la cola
            
            Nodo<T> nuevoNodo = new Nodo<>(dato);
            if (frente == null){ // Usamos comprobación directa para evitar deadlocks internos
                frente = nuevoNodo;
                finalCola = nuevoNodo;
            } else {
                finalCola.setSiguiente(nuevoNodo);
                finalCola = nuevoNodo;
            }
            tamano++;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release(); // Soltamos la cola
        }
        
        // ¡Señalamos que hay un nuevo elemento disponible!
        // Esto despertará a cualquier hilo que esté durmiendo en desencolar()
        elementosDisponibles.release(); 
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
        boolean eliminado = false;
        try {
            mutex.acquire();
            
            if (frente == null) {
                return false; // Salimos rápido pero pasando por el finally para liberar el mutex
            }

            // Escenario A: El elemento a eliminar que buscamos esta al frente de la cola
            if (frente.getContenido().equals(objetivo)) {
                frente = frente.getSiguiente();
                if (frente == null){
                    finalCola = null;
                }
                tamano--;
                eliminado = true;
            } else {
                // Escenario B: El elemento esta oculto en el medio o al final de la cola.
                Nodo<T> nodoAnterior = frente;
                Nodo<T> nodoActual = frente.getSiguiente();

                while (nodoActual != null) {
                    if (nodoActual.getContenido().equals(objetivo)) {
                        nodoAnterior.setSiguiente(nodoActual.getSiguiente());
                        if (nodoActual == finalCola) {
                            finalCola = nodoAnterior;
                        }
                        tamano--;
                        eliminado = true;
                        break;
                    }
                    nodoAnterior = nodoActual;
                    nodoActual = nodoActual.getSiguiente();
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        
        // Si logramos eliminar un elemento manualmente, debemos "robarle" un permiso
        // al semáforo para que la cuenta de elementos siga coincidiendo con la realidad.
        if (eliminado) {
            elementosDisponibles.tryAcquire(); 
        }
        
        return eliminado;
    }
    
    // Definimos el metodo para sacar el elemento del frente de la cola (desencolar)
    public T desencolar(){
        T dato = null;
        try {
            // 1. Si la cola está vacía, EL HILO DEL DISCO SE DUERME AQUÍ esperando un release()
            elementosDisponibles.acquire(); 
            
            // 2. Si hay elementos, pedimos permiso para modificar los punteros
            mutex.acquire();
            
            if (frente != null) {
                dato = frente.getContenido();
                frente = frente.getSiguiente();
                if (frente == null){
                    finalCola = null;
                }
                tamano--;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return dato;
    }
    
    // Para ver el primer elemento sin sacarlo de la cola
    public T obtenerFrente(){
        T dato = null;
        try {
            mutex.acquire();
            if (frente != null) {
                dato = frente.getContenido();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return dato;
    }
    
    // Para determinar si la cola esta vacia
    public boolean estaVacia(){
        boolean vacia = true;
        try {
            mutex.acquire();
            vacia = (frente == null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return vacia;
    }
    
    // Para oonocer el tamano de la cola
    public int getTamano(){
        int t = 0;
        try {
            mutex.acquire();
            t = tamano;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return t;
    }
}

