/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package politicas;

/**
 * Enumera las posibles direcciones de movimiento del cabezal del disco.
 * Este enumerador es fundamental para las políticas de planificación basadas en escaneo 
 * como SCAN, C-SCAN, LOOK y C-LOOK), ya que el algoritmo necesita saber hacia dónde 
 * se está desplazando físicamente el cabezal para decidir qué solicitud atender primero.
 * @author ricar
 */
public enum direccionScan {
    ARRIBA,
    ABAJO
}
