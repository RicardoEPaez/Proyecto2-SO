/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Procesos;

/**
 * Representa los distintos estados por los que puede pasar un Proceso PCB durante su ciclo 
 * de vida en el simulador del sistema operativo
 * @author ricar
 */
public enum Estado {
    NUEVO,
    LISTO,
    EJECUTANDO,
    BLOQUEANDO,
    TERMINADO,
}
