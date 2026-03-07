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
public class ListaEnlazada<T> {
    private Nodo<T> inicio; 
    private int tamano;
    
    public ListaEnlazada(){
        this.inicio = null;
        this.tamano = 0;
    }
    
    // Para insertar al final de la lista
    public void agregar(T dato){
        Nodo<T> nuevo = new Nodo <>(dato);
        if (estaVacia()) {
            inicio = nuevo;
        } else {
            Nodo<T> aux = inicio;
            while (aux.getSiguiente() != null){
                aux = aux.getSiguiente();
            }
            aux.setSiguiente(nuevo);
        }
        tamano++;
    }
    
    public boolean eliminar(T dato) {
        if (estaVacia()) return false;
        
        if (inicio.getContenido().equals(dato)){
            inicio = inicio.getSiguiente();
            tamano--;
            return true; 
        }
        
        Nodo<T> actual = inicio;
        if (actual == null) return false; 

        while (actual.getSiguiente() != null && !actual.getSiguiente().getContenido().equals(dato)){
            actual = actual.getSiguiente();
        }
        
        if (actual.getSiguiente() != null){
            actual.setSiguiente(actual.getSiguiente().getSiguiente());
            tamano--;
            return true; // Encontrado y eliminado
        }
        
        return false; // No se encontró
    }
    
    // Obtener elementos por indice para recorrer la lista en bucles
    public T get(int indice){
        if (indice < 0 || indice >= tamano) return null;
        
        Nodo<T> actual = inicio;
        for (int i = 0; i < indice; i++){
            actual = actual.getSiguiente();
        }
        return actual.getContenido();
    }
    
    /**
     * Reemplaza el contenido de un nodo en un índice específico.
     * Muy útil para la simulación de disco, pues permite sobrescribir un bloque específico de un archivo durante una operación de actualización.
     * @param indice La posición del nodo que se va a modificar.
     * @param nuevoContenido El nuevo dato que se guardará en esa posición.
     * @return true si la modificación fue exitosa, false si el índice no es válido.
     */
    public boolean modificarEnIndice(int indice, T nuevoContenido) {
        if (indice < 0 || indice >= tamano) {
            return false; // Índice fuera de rango
        }

        Nodo<T> actual = inicio;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        
        // Simplemente cambiamos el contenido del nodo existente
        actual.setContenido(nuevoContenido);
        return true;
    }
    
    // Para determinar si la lista esta vacia
    public boolean estaVacia(){
        return inicio == null;
    }
    
    public int getTamano(){
        return tamano;
    }
    
    // Getter del nodo inicio, el cual es util si se necesita iterar manualmente fuera de la clase
    public Nodo<T> getInicio(){
        return inicio;
    }
    
    // Método para la GUI: Devuelve un arreglo simple con los elementos
    public Object[] toArray() {
        Object[] arreglo = new Object[tamano];
        Nodo<T> actual = inicio;
        
        for (int i = 0; i < tamano; i++) {
            if (actual != null) {
                arreglo[i] = actual.getContenido();
                actual = actual.getSiguiente();
            }
        }
        return arreglo;
    }
}
