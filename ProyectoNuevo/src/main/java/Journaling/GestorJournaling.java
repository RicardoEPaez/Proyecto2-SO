/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Journaling;
/**
 * Clase para el Gestor del Journaling
 * @author ricar
 */


public class GestorJournaling {
    private static final int MAX_LOGS = 100;
    private static RegistroJournal[] bitacora = new RegistroJournal[MAX_LOGS];
    private static int tope = 0; 

    public static void registrarOperacion(RegistroJournal registro) {
        if (tope < MAX_LOGS) {
            bitacora[tope] = registro;
            tope++;
            System.out.println("[Journal] Transacción registrada como PENDIENTE: " + registro.getTipoOperacion() + " en '" + registro.getNombreArchivo() + "'");
        }
    }

    public static synchronized void confirmarOperacion() {
        if (tope > 0) {
            bitacora[tope - 1].setConfirmado(true);
            System.out.println("[Journal] Transacción CONFIRMADA (Commit): " + bitacora[tope - 1].getNombreArchivo());
        }
    }
    
    // --- AQUÍ ESTÁ EL CAMBIO ---
    // Ya no recibe parámetros () y busca automáticamente las NO confirmadas
    public static synchronized void simularFalloYRecuperar() {
        System.out.println("\n!!! SIMULANDO FALLO DE ENERGÍA / SISTEMA !!!");
        System.out.println("[Journal] Iniciando protocolo de recuperación (Undo)...");
        
        // Recorremos de atrás hacia adelante
        for (int i = tope - 1; i >= 0; i--) {
            RegistroJournal log = bitacora[i];
            
            // Requisito: Solo deshacer si NO está confirmado
            if (log != null && !log.isConfirmado()) {
                System.out.print("[Undo] Detectada op. PENDIENTE. Revirtiendo -> " + log.getTipoOperacion() + "... ");
                
                switch (log.getTipoOperacion()) {
                    case "CREAR":
                        System.out.println("Borrando archivo '" + log.getNombreArchivo() + "' y liberando bloque " + log.getBloqueInvolucrado());
                        break;
                    case "ACTUALIZAR":
                        System.out.println("Restaurando versión previa del archivo '" + log.getNombreArchivo() + "'");
                        break;
                    case "ELIMINAR":
                        System.out.println("Restaurando archivo eliminado '" + log.getNombreArchivo() + "' en bloque " + log.getBloqueInvolucrado());
                        break;
                    default:
                        System.out.println("Operación de lectura u otra: No requiere reversión.");
                }
                bitacora[i] = null; // Limpiamos la entrada corrupta
                
            } else if (log != null && log.isConfirmado()) {
                // Si ya tenía el commit, la dejamos quieta
                System.out.println("[Journal] Op. CONFIRMADA: " + log.getTipoOperacion() + " ('" + log.getNombreArchivo() + "') -> Manteniendo cambios.");
            }
        }
        System.out.println("[Journal] Recuperación completada.\n");
    }
}