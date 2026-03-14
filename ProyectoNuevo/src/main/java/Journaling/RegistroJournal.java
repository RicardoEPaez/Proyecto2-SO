/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Journaling;

/**
 * Representa una transacción individual en el sistema de archivos.
 * Guarda la información necesaria para poder DESHACER la operación.
 * @author ricar
 */
public class RegistroJournal {
    private String tipoOperacion;
    private String nombreArchivo;
    private int bloqueInvolucrado;
    private boolean confirmado;
    
    public RegistroJournal(String tipoOperacion, String nombreArchivo, int bloqueInvolucrado) {
        this.tipoOperacion = tipoOperacion;
        this.nombreArchivo = nombreArchivo;
        this.bloqueInvolucrado = bloqueInvolucrado;
        this.confirmado =false;
    }
   public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean estado) { this.confirmado = estado; }
    public String getTipoOperacion() { return tipoOperacion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getBloqueInvolucrado() { return bloqueInvolucrado; }

    @Override
    public String toString() {
        return "[" + tipoOperacion + "] Archivo: " + nombreArchivo + " (Bloque: " + bloqueInvolucrado + ")";
    }
}
