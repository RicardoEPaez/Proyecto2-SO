/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilidades;

import Archivo.Archivo;
import Archivo.Directorio;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Clase utilitaria encargada de manejar la persistencia del sistema de archivos.
 * Permite guardar el estado actual del árbol de directorios en un archivo JSON y cargarlo nuevamente en futuras ejecuciones.
 * @author Ramon-Carrasquel
 */
public class GestorJSON {

    /**
     * Guarda el árbol de directorios en un archivo JSON físico en la computadora.
     * @param raiz El directorio raíz del sistema de archivos.
     * @param rutaFisica La ruta donde se guardará el archivo (ej. "sistema_archivos.json").
     * @return true si se guardó con éxito, false en caso contrario.
     */
    public static boolean guardarSistema(Directorio raiz, String rutaFisica) {
        try (FileWriter file = new FileWriter(rutaFisica)) {
            // Obtenemos el JSON del árbol usando el método que hicieron tus compañeros
            JSONObject jsonRaiz = raiz.toJson();
            
            // Escribimos el JSON en el archivo con una indentación de 4 espacios (para que sea legible)
            file.write(jsonRaiz.toString(4));
            file.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lee un archivo JSON físico y reconstruye el árbol de directorios y archivos.
     * @param rutaFisica La ruta del archivo a leer (ej. "sistema_archivos.json").
     * @return El objeto Directorio que funciona como raíz, o null si hubo un error.
     */
    public static Directorio cargarSistema(String rutaFisica) {
        try {
            // 1. Leer todo el contenido del archivo de texto
            String contenido = new String(Files.readAllBytes(Paths.get(rutaFisica)));
            
            // 2. Convertir el texto a un objeto JSON
            JSONObject jsonRaiz = new JSONObject(contenido);
            
            // 3. Empezar a reconstruir recursivamente desde la raíz (cuyo padre es null)
            return parsearDirectorio(jsonRaiz, null);
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON físico: " + e.getMessage());
            return null;
        } catch (JSONException e) {
            System.err.println("Error de formato al parsear el JSON: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método auxiliar recursivo para reconstruir un directorio y su contenido.
     * @param jsonDir El objeto JSON que representa el directorio actual.
     * @param padre El directorio padre al que pertenece este directorio (null si es la raíz).
     * @return El objeto Directorio reconstruido con todo su contenido.
     */
    private static Directorio parsearDirectorio(JSONObject jsonDir, Directorio padre) {
        // Extraemos el nombre del directorio actual
        String nombre = jsonDir.getString("nombre");
        
        // Creamos la instancia del directorio
        Directorio nuevoDir = new Directorio(nombre, padre);
        
        // Obtenemos el arreglo "contenido" que guarda sus archivos y subdirectorios
        JSONArray contenidoArray = jsonDir.getJSONArray("contenido");
        
        // Iteramos sobre lo que hay adentro de esta carpeta
        for (int i = 0; i < contenidoArray.length(); i++) {
            JSONObject item = contenidoArray.getJSONObject(i);
            String tipo = item.getString("tipo");
            
            if (tipo.equals("DIRECTORIO")) {
                // Si es un subdirectorio, hacemos recursividad
                Directorio subDir = parsearDirectorio(item, nuevoDir);
                
                nuevoDir.getContenido().agregar(subDir); 
                
            } else if (tipo.equals("ARCHIVO")) {
                // Si es un archivo, extraemos todos sus atributos
                String nombreArch = item.getString("nombre");
                int tamano = item.getInt("tamanoEnBloques");
                int primerBloque = item.getInt("primerBloque");
                int idCreador = item.getInt("idProcesoCreador");
                String color = item.optString("color", "#FFFFFF"); // optString por si acaso falta el color
                
                // Instanciamos el archivo reconstruido
                Archivo nuevoArch = new Archivo(nombreArch, nuevoDir, tamano, primerBloque, idCreador, color);
                
                // Lo agregamos a la lista enlazada del directorio padre
                nuevoDir.getContenido().agregar(nuevoArch);
            }
        }
        
        return nuevoDir;
    }
}
