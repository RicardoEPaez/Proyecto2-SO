/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

import java.util.concurrent.Semaphore;
        
/**
 * Estructura de datos Cola protegida para concurrencia.
 * Se encarga únicamente de mantener la integridad de sus datos mediante Exclusión Mutua (Mutex).
 * La lógica de sincronización (dormir/despertar) recae sobre el Productor y el Consumidor.
 * @author Ramon-Carrasquel
 * @param <T>
 */
public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;
    
    // Candado exclusivo para proteger los punteros frente y finalCola
    private final Semaphore mutex;
    
    public Cola(){
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
        
        this.mutex = new Semaphore(1);
    }
    
    // Agregar un elemento al final de la cola
    public void encolar(T dato){
        try {
            mutex.acquire(); // Bloqueamos la cola para evitar que otro hilo la modifique a la vez
            
            Nodo<T> nuevoNodo = new Nodo<>(dato);
            if (frente == null){ 
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
            mutex.release(); // Soltamos la cola pase lo que pase
        }
    }
    
    /**
     * Extraer un elemento específico de la cola sin importar su posición.
     * @param objetivo El elemento que se desea buscar y eliminar de la cola.
     * @return true si el elemento fue encontrado y eliminado exitosamente, false si la cola estaba vacía o el elemento no existe.
     */
    public boolean eliminar(T objetivo) {
        boolean eliminado = false;
        try {
            mutex.acquire();
            
            if (frente == null) {
                return false; 
            }

            // Escenario A: El elemento a eliminar está al frente
            if (frente.getContenido().equals(objetivo)) {
                frente = frente.getSiguiente();
                if (frente == null){
                    finalCola = null;
                }
                tamano--;
                eliminado = true;
            } else {
                // Escenario B: El elemento está en el medio o al final
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
        
        return eliminado;
    }
    
    // Sacar el elemento del frente de la cola
    public T desencolar(){
        T dato = null;
        try {
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
        return dato; // Retornará null de forma segura si la cola estaba vacía
    }
    
    // Ver el primer elemento sin sacarlo de la cola
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
    
    // Determinar si la cola está vacía
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
    
    // Conocer el tamaño de la cola
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

