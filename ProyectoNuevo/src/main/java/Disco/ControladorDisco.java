/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Disco;

import Archivo.Bloque;
/**
 * Representa el disco físico del simulador.
 * Se encarga exclusivamente de administrar los bloques y la Asignación Encadenada.
 * @author Ramon-Carrasquel
 */
public class ControladorDisco {
    
    private Bloque[] discoVirtual;
    private int cantidadBloquesLibres;

    public ControladorDisco(int tamanoDisco) {
        this.discoVirtual = new Bloque[tamanoDisco];
        for (int i = 0; i < tamanoDisco; i++) {
            this.discoVirtual[i] = new Bloque(i);
        }
        this.cantidadBloquesLibres = tamanoDisco;
    }

    /**
     * LÓGICA DE ASIGNACIÓN ENCADENADA.
     * Busca espacio disponible en el disco virtual y enlaza los bloques uno tras otro.
     * @param cantidadNecesaria El número de bloques que requiere el archivo para guardarse.
     * @param idProceso El ID (PID) del proceso que ocupará estos bloques.
     * @return El índice del bloque de arranque (el primero), o -1 si no hay espacio suficiente.
     */
    public int asignarBloques(int cantidadNecesaria, int idProceso) {
        if (cantidadNecesaria > cantidadBloquesLibres) return -1; 

        int primerBloque = -1;
        int bloqueAnterior = -1;
        int bloquesAsignados = 0;

        for (int i = 0; i < discoVirtual.length && bloquesAsignados < cantidadNecesaria; i++) {
            if (!discoVirtual[i].isOcupado()) {
                discoVirtual[i].ocupar(idProceso);
                
                if (primerBloque == -1) primerBloque = i;
                
                if (bloqueAnterior != -1) {
                    discoVirtual[bloqueAnterior].setSiguienteBloque(i);
                }
                
                bloqueAnterior = i;
                bloquesAsignados++;
            }
        }
        
        if (bloqueAnterior != -1) {
            discoVirtual[bloqueAnterior].setSiguienteBloque(Bloque.FIN_DE_ARCHIVO);
        }
        
        cantidadBloquesLibres -= cantidadNecesaria;
        return primerBloque;
    }

    /**
     * Libera una cadena de bloques guiándose por la asignación encadenada.
     * Recorre desde el bloque inicial hasta encontrar el fin del archivo (-1).
     * @param bloqueInicial El índice del primer bloque físico que ocupaba el archivo.
     */
    public void liberarBloquesEncadenados(int bloqueInicial) {
        int actual = bloqueInicial;
        while (actual != Bloque.FIN_DE_ARCHIVO && actual >= 0) {
            int siguiente = discoVirtual[actual].getSiguienteBloque();
            discoVirtual[actual].liberar();
            cantidadBloquesLibres++;
            actual = siguiente;
        }
    }

    public Bloque[] getDiscoVirtual() { 
        return discoVirtual; 
    }
    
    public int getCantidadBloquesLibres() { 
        return cantidadBloquesLibres; 
    }
}
