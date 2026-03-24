/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *Nodo generico para listas y colas
 * @author Ramon-Carrasquel
 * @param <T> El tipo de dato que guardará el nodo
 */
public class Nodo<T> {
    private T contenido;
    private Nodo<T> siguiente;
    
    // Constructor
    public Nodo(T contenido){
        this.contenido = contenido;
        this.siguiente = null;
    }
    
    // Getters y Setters
    public T getContenido(){
        return contenido;
    }
    
    public void setContenido(T contenido){
        this.contenido = contenido;
    }
    
    public Nodo<T> getSiguiente(){
        return siguiente;
    }
    
    public void setSiguiente(Nodo<T> siguiente){
        this.siguiente = siguiente;
    }
}
