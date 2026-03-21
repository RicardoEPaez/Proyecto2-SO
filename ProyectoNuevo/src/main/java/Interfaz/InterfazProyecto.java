/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaz;

/**
 *
 * @author ricar
 */
public class InterfazProyecto extends javax.swing.JFrame {
    private javax.swing.JPanel[] bloquesDisco = new javax.swing.JPanel[100];
    private Modo modoActual = Modo.ADMINISTRADOR;
    private boolean sistemaPausado = false;
    private int cabezalAnterior = 0;
    private Disco.PlanificadorDisco discoSimulado;
    private String rutaPruebaActual = "";
    
    /**
     * Creates new form InterfazProyecto
     */
    public InterfazProyecto() {
        initComponents();
        
        inicializarDiscoVisual();
        
        actualizarCabezalVisual(50);
        
        // 1. Inicializamos el planificador de disco vacio
        estructuras.Cola<Procesos.SolicitudIO> colaIO = new estructuras.Cola<>();
        discoSimulado = new Disco.PlanificadorDisco(colaIO);
        discoSimulado.setInterfazGrafica(this); 
        discoSimulado.setCabezal(50);
        discoSimulado.setPolitica(new politicas.FIFO()); // Política por defecto
        new Thread(discoSimulado).start(); 
        
        // 2. Cargamos el árbol de carpetas (TU código)
        cargarArbolDesdeJSON("sistema_archivos.json");
        
        // 1. Agrupar los botones para que sean mutuamente excluyentes
        javax.swing.ButtonGroup grupoModos = new javax.swing.ButtonGroup();
        grupoModos.add(jRadioButton1);
        grupoModos.add(jRadioButton3);
        
        // 2. Seleccionar Administrador por defecto al abrir la ventana
        jRadioButton1.setSelected(true);
        
        // --- REDIRIGIR CONSOLA AL JTEXTAREA ---
        java.io.PrintStream printStream = new java.io.PrintStream(new CustomOutputStream(jTextArea1));
        System.setOut(printStream);
        System.setErr(printStream); // También captura los errores (System.err.println)
        
        // --- CONFIGURACIÓN DEL ÁRBOL DE ARCHIVOS (JTREE) ---
        javax.swing.tree.DefaultMutableTreeNode raiz = new javax.swing.tree.DefaultMutableTreeNode("Disco (C:)");
        javax.swing.tree.DefaultTreeModel modeloArbol = new javax.swing.tree.DefaultTreeModel(raiz);
        jTree1.setModel(modeloArbol);
        
        // --- ACTUALIZADOR DE LA PANTALLA DE PROCESOS ---
        // Usamos un Timer de Swing para actualizar la interfaz cada 500ms de forma segura
        javax.swing.Timer timer = new javax.swing.Timer(500, (java.awt.event.ActionEvent e) -> {
            actualizarPantallaProcesos();
        });
        timer.start();
        
        // --- ENCENDER LA CPU VIRTUAL ---
        Procesos.GestorProcesos.iniciarCPU();
        
        // --- CONFIGURAR PERMISOS INICIALES ---
        // Esto asegura que los botones coincidan con el modo ADMINISTRADOR al iniciar
        actualizarPermisosBotones();
    }
    
    private void inicializarDiscoVisual() {
        // Limpiamos por si acaso NetBeans dejó algo oculto
        panelDiscoSimulador.removeAll();

        for (int i = 0; i < 100; i++) {
            // 1. Crear el cubito (JPanel)
            javax.swing.JPanel bloque = new javax.swing.JPanel();
            bloque.setLayout(new java.awt.BorderLayout());
            bloque.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY)); // Borde gris
            bloque.setBackground(java.awt.Color.LIGHT_GRAY); // Fondo gris claro (bloque vacío)

            // 2. Ponerle el numerito en el centro
            javax.swing.JLabel lblNumero = new javax.swing.JLabel(String.valueOf(i), javax.swing.SwingConstants.CENTER);
            lblNumero.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
            bloque.add(lblNumero, java.awt.BorderLayout.CENTER);

            // 3. Guardarlo en nuestro arreglo para manipularlo después
            bloquesDisco[i] = bloque;

            // 4. Agregarlo a tu panelCuadriculaDisco de la interfaz
            panelDiscoSimulador.add(bloque);
        }

        // Le decimos a la ventana que se actualice para mostrar los cambios
        panelDiscoSimulador.revalidate();
        panelDiscoSimulador.repaint();
    }
    
    public final void actualizarCabezalVisual(int nuevaPosicion) {
        // Protección: Si el bloque es mayor a 99, lo mapeamos para que entre en la cuadrícula
        final int posSegura = (nuevaPosicion >= 100) ? (nuevaPosicion % 100) : nuevaPosicion;

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // 1. Despintar el bloque viejo (volverlo gris)
                bloquesDisco[cabezalAnterior].setBackground(java.awt.Color.LIGHT_GRAY);
                
                // 2. Pintar el bloque nuevo de rojo
                bloquesDisco[posSegura].setBackground(java.awt.Color.RED);
                
                // 3. Actualizar la etiqueta de texto
                jLabel5.setText("Cabeza: " + posSegura);
                
                // Forzar a la ventana a redibujarse
                panelDiscoSimulador.repaint();
                
                // 4. Guardar la nueva posición
                cabezalAnterior = posSegura;
                
            } catch (Exception e) {
                System.out.println("Error al pintar la UI: " + e.getMessage());
            }
        });
    }
    
    public void ejecutarPruebaJSON(String rutaArchivoJSON) {
        this.rutaPruebaActual = rutaArchivoJSON;
        System.out.println("=== CARGANDO CASO DE PRUEBA ===");
        org.json.JSONObject jsonPrueba = Utilidades.GestorJSON.cargarPruebaSimulacion(rutaArchivoJSON);
        
        if (jsonPrueba == null) {
            System.err.println("Error: No se pudo cargar el archivo JSON de prueba.");
            return;
        }

        try {
            int cabezalInicial = jsonPrueba.getInt("initial_head");
            actualizarCabezalVisual(cabezalInicial); 
            if (discoSimulado != null) {
                discoSimulado.setCabezal(cabezalInicial);
            }

            org.json.JSONObject systemFiles = jsonPrueba.getJSONObject("system_files");
            for (String key : systemFiles.keySet()) {
                org.json.JSONObject fileData = systemFiles.getJSONObject(key);
                String nombreArchivo = fileData.getString("name");
                int cantidadBloques = fileData.getInt("blocks");
                int posicionInicial = Integer.parseInt(key);
                
                // Pintamos los archivos de prueba en el disco visual
                registrarArchivoEnGUI(nombreArchivo, cantidadBloques, posicionInicial);
            }

            org.json.JSONArray requests = jsonPrueba.getJSONArray("requests");
            for (int i = 0; i < requests.length(); i++) {
                org.json.JSONObject req = requests.getJSONObject(i);
                int posicion = req.getInt("pos");
                String operacionStr = req.getString("op"); 
                
                // 1. Convertimos el String "READ", "UPDATE" al enum TipoOperacionIO de tu compañero
                Procesos.TipoOperacionIO tipoOp = Procesos.TipoOperacionIO.valueOf(operacionStr.toUpperCase());
                
                // 2. Usamos el constructor correcto: (idProceso, Tipo, Ruta, Tamaño, BloqueObjetivo)
                // Usamos valores ficticios (1, "Simulacion", 1) para las variables que no importan en la prueba
                Procesos.SolicitudIO nuevaSolicitud = new Procesos.SolicitudIO(1, tipoOp, "Simulacion", 1, posicion);
                
                // 3. Lo metemos a la cola
                discoSimulado.getColaCompartida().encolar(nuevaSolicitud); //
                discoSimulado.registrarNuevaPeticion(); // IMPORTANTE: Esto "despierta" al hilo del disco
            }
            System.out.println("Prueba cargada exitosamente.");
            
        } catch (org.json.JSONException e) {
            System.err.println("Error procesando el JSON: " + e.getMessage());
        }
    }
    
    private void registrarArchivoEnGUI(String nombre, int bloques, int posInicial) {
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
        modeloTabla.addRow(new Object[]{nombre, bloques, posInicial});
        
        for (int i = 0; i < bloques; i++) {
            int posicionActual = posInicial + i;
            if (posicionActual < bloquesDisco.length) {
                bloquesDisco[posicionActual].setBackground(java.awt.Color.RED);
            }
        }
    }
    
    private void actualizarPermisosBotones() {
        // Determinamos si es administrador (true o false)
        boolean esAdmin = (modoActual == Modo.ADMINISTRADOR);
        
        // Bloqueamos o desbloqueamos según el modo
        jButton2.setEnabled(esAdmin); // Crear Directorio
        jButton3.setEnabled(esAdmin); // Crear Archivo
        jButton4.setEnabled(esAdmin); // Renombrar
        jButton6.setEnabled(esAdmin); // Eliminar
        jButton7.setEnabled(esAdmin); // Estadísticas
        
        // El botón Leer (jButton1) y Pausa (jButton8) siempre quedan activos
        jButton1.setEnabled(true); 
        jButton8.setEnabled(true);
    }
    
    private void cargarArbolDesdeJSON(String rutaArchivo) {
        Archivo.Directorio raizLogica = Utilidades.GestorJSON.cargarSistema(rutaArchivo);
        javax.swing.tree.DefaultMutableTreeNode raizVisual;

        if (raizLogica != null) {
            System.out.println("[Sistema] Estado del disco cargado desde: " + rutaArchivo);
            raizVisual = new javax.swing.tree.DefaultMutableTreeNode("Disco (C:)");
            construirArbolVisual(raizLogica, raizVisual);
        } else {
            System.out.println("[Sistema] No se encontró estado guardado. Iniciando disco vacío.");
            raizVisual = new javax.swing.tree.DefaultMutableTreeNode("Disco (C:)");
        }
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(raizVisual));
    }
    
    private void construirArbolVisual(Archivo.Directorio dirLogico, javax.swing.tree.DefaultMutableTreeNode nodoVisual) {
        // Aprovechamos el método toArray() de la ListaEnlazada que ustedes crearon
        Object[] elementos = dirLogico.getContenido().toArray();
        
        for (Object elemento : elementos) {
            if (elemento == null) continue;
            
            if (elemento instanceof Archivo.Directorio) {
                // Si el elemento es una carpeta
                Archivo.Directorio subDir = (Archivo.Directorio) elemento;
                javax.swing.tree.DefaultMutableTreeNode nuevoNodoDir = new javax.swing.tree.DefaultMutableTreeNode("📁 " + subDir.getNombre());
                nodoVisual.add(nuevoNodoDir);
                
                // Hacemos recursividad para ver si esta subcarpeta tiene más archivos adentro
                construirArbolVisual(subDir, nuevoNodoDir); 
                
            } else if (elemento instanceof Archivo.Archivo) {
                // Si el elemento es un archivo normal
                Archivo.Archivo arch = (Archivo.Archivo) elemento;
                javax.swing.tree.DefaultMutableTreeNode nuevoNodoArch = new javax.swing.tree.DefaultMutableTreeNode("📄 " + arch.getNombre());
                nodoVisual.add(nuevoNodoArch);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        panelDiscoSimulador = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaAsignacion = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Controles"));

        jLabel1.setText("Modo");

        jRadioButton1.setText("Admin");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton3.setText("User");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jLabel2.setText("Planificacion");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "CSCAN" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton2.setText("Crear Directorio");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Crear Archivo");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton1.setText("Leer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("Renombrar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setText("Eliminar");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("Estadistica");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Pausa");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Velocidad"));

        jSlider1.setMajorTickSpacing(250);
        jSlider1.setMaximum(1000);
        jSlider1.setMinimum(50);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setValue(500);

        jLabel3.setText("500 ms");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(169, 169, 169)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel4.setText("Ciclo: 0");

        jLabel5.setText("Cabezal: 0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(58, 58, 58)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton8)))
                .addContainerGap(426, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton3)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton1)
                    .addComponent(jButton4)
                    .addComponent(jButton6)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Sistema de archivos"));

        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Log de eventos"));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jButton5.setText("Limpiar");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jButton5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Cola de Procesos"));

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText("=== LISTOS ===\n\n=== EN CPU ===\n\n=== BLOQUEADOS ===\n\n=== I/O EN EJECUCIÓN ===\n\n=== COLA I/O ===");
        jScrollPane3.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 728, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 1, Short.MAX_VALUE))
        );

        panelDiscoSimulador.setLayout(new java.awt.GridLayout(10, 10, 2, 2));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDiscoSimulador, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDiscoSimulador, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Simulador ", jPanel8);

        tablaAsignacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre Archivo", "Bloque Inicial", "Tamaño"
            }
        ));
        jScrollPane4.setViewportView(tablaAsignacion);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Tabla de Asignacion", jPanel9);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 672, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 355, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Cache", jPanel10);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(180, 180, 180))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("Archivos");

        jMenuItem1.setText("Guardar Estado del Sistema (.json)");
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Cargar Estado del Sistema (.json)");
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Salir");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Reportes");

        jMenuItem4.setText("Reportar Resumen del Sistema (.txt)");
        jMenu2.add(jMenuItem4);

        jMenuItem5.setText("Exportar Estadisticas Procesos (.csv)");
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
        modoActual = Modo.ADMINISTRADOR;
        System.out.println("[Sistema] Modo cambiado a: ADMINISTRADOR");
        
        // --- LLAMADA AL NUEVO MÉTODO ---
        actualizarPermisosBotones();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        // TODO add your handling code here:
        modoActual = Modo.USUARIO;
        System.out.println("[Sistema] Modo cambiado a: USUARIO");
        
        // --- LLAMADA AL NUEVO MÉTODO ---
        actualizarPermisosBotones();
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        System.out.println(">>> REINICIANDO SIMULACION CON: " + jComboBox1.getSelectedItem().toString());
        
        // (El código de limpiar los bloques se mantiene igual...)
        
        // Si ya habíamos cargado una prueba antes, la volvemos a lanzar
        if (!rutaPruebaActual.isEmpty()) {
            ejecutarPruebaJSON(rutaPruebaActual);
        } else {
            System.out.println("Aun no se ha cargado ninguna prueba de simulacion.");
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        // Limpiar el log de eventos
        jTextArea1.setText("");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona una carpeta donde guardar el archivo.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre del nuevo archivo (ej. documento.txt):");
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            
            // --- NUEVO: CREAR EL PROCESO CON TU CONSTRUCTOR ---
            // Nota: Pasamos 'null' a SolicitudIO temporalmente hasta que hagamos el Disco
            Procesos.PCB nuevoProceso = new Procesos.PCB("Crear_Archivo_" + nombre, null);
            nuevoProceso.setEstado(Procesos.Estado.LISTO);
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            System.out.println("[Sistema] Solicitud enviada al Gestor de Procesos.");
            
            // --- ACTUALIZAR EL ÁRBOL (Visual) ---
            javax.swing.tree.DefaultMutableTreeNode nuevoNodo = new javax.swing.tree.DefaultMutableTreeNode("📄 " + nombre);
            javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
            
            modelo.insertNodeInto(nuevoNodo, nodoSeleccionado, nodoSeleccionado.getChildCount());
            jTree1.scrollPathToVisible(new javax.swing.tree.TreePath(nuevoNodo.getPath()));
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona una carpeta en el arbol primero.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre del nuevo directorio:");
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            
            // --- NUEVO: CREAR EL PROCESO ---
            // Le ponemos "Crear_Directorio_" al nombre y pasamos null a la solicitud por ahora
            Procesos.PCB nuevoProceso = new Procesos.PCB("Crear_Directorio_" + nombre, null);
            nuevoProceso.setEstado(Procesos.Estado.LISTO);
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            System.out.println("[Sistema] Solicitud de creacion de directorio enviada.");
            
            // --- ACTUALIZAR EL ÁRBOL (Visual/Temporal) ---
            javax.swing.tree.DefaultMutableTreeNode nuevoNodo = new javax.swing.tree.DefaultMutableTreeNode("📁 " + nombre);
            javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
            
            modelo.insertNodeInto(nuevoNodo, nodoSeleccionado, nodoSeleccionado.getChildCount());
            jTree1.scrollPathToVisible(new javax.swing.tree.TreePath(nuevoNodo.getPath()));
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona que deseas leer.", "Atencion", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombre = nodoSeleccionado.getUserObject().toString();
        
        // Verificamos que sea un archivo y no una carpeta
        if (nombre.startsWith("📁") || nodoSeleccionado.isRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Solo puedes leer archivos, no carpetas o discos.", "Accion denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // --- NUEVO: CREAR EL PROCESO ---
        String nombreLimpio = nombre.substring(2).trim();
        Procesos.PCB nuevoProceso = new Procesos.PCB("Leer_" + nombreLimpio, null);
        nuevoProceso.setEstado(Procesos.Estado.LISTO);
        Procesos.GestorProcesos.agregarProceso(nuevoProceso);
        System.out.println("[Sistema] Solicitud de lectura enviada.");
        
        // Simulación visual
        javax.swing.JOptionPane.showMessageDialog(this, "Leyendo el contenido de:\n" + nombre + "\n\n(Simulación de lectura exitosa)", "Visor de Archivos", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta para renombrar.", "Atencion", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (nodoSeleccionado.isRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No puedes renombrar la raíz del disco.", "Accion denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombreAntiguo = nodoSeleccionado.getUserObject().toString();
        // Le quitamos el emoji para mostrar solo el texto limpio en el cuadro de diálogo
        String textoLimpio = nombreAntiguo.substring(2).trim(); 
        
        String nuevoNombre = javax.swing.JOptionPane.showInputDialog(this, "Nuevo nombre:", textoLimpio);
        
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            
            // --- NUEVO: CREAR EL PROCESO ---
            Procesos.PCB nuevoProceso = new Procesos.PCB("Renombrar_" + textoLimpio, null);
            nuevoProceso.setEstado(Procesos.Estado.LISTO);
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            System.out.println("[Sistema] Solicitud de renombrado enviada.");

            // --- ACTUALIZAR EL ÁRBOL (Visual/Temporal) ---
            String emoji = nombreAntiguo.substring(0, 2);
            nodoSeleccionado.setUserObject(emoji + " " + nuevoNombre);
            
            // Avisamos al modelo que el nodo cambió para que se actualice visualmente
            ((javax.swing.tree.DefaultTreeModel) jTree1.getModel()).nodeChanged(nodoSeleccionado);
            
            System.out.println("Se renombró '" + textoLimpio + "' a '" + nuevoNombre + "'");
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona el archivo o carpeta que deseas eliminar.");
            return;
        }
        
        if (nodoSeleccionado.isRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, "¡No puedes eliminar el Disco Principal!", "Accion denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombreNodo = nodoSeleccionado.getUserObject().toString();
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar '" + nombreNodo + "'?", "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            
            // --- NUEVO: CREAR EL PROCESO ---
            // Limpiamos el nombre un poco para que no salga el emoji en el nombre del proceso
            String nombreLimpio = nombreNodo.substring(2).trim();
            Procesos.PCB nuevoProceso = new Procesos.PCB("Eliminar_" + nombreLimpio, null);
            nuevoProceso.setEstado(Procesos.Estado.LISTO);
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            System.out.println("[Sistema] Solicitud de eliminacion enviada.");

            // --- ACTUALIZAR EL ARBOL (Visual/Temporal) ---
            javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
            modelo.removeNodeFromParent(nodoSeleccionado);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
        javax.swing.tree.DefaultMutableTreeNode raiz = (javax.swing.tree.DefaultMutableTreeNode) modelo.getRoot();
        
        int totalCarpetas = 0;
        int totalArchivos = 0;
        
        // ¡Usamos 'var' como sugirió tu IDE!
        var enumeracion = raiz.breadthFirstEnumeration();
        
        while (enumeracion.hasMoreElements()) {
            javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) enumeracion.nextElement();
            String nombre = nodo.getUserObject().toString();
            
            if (nombre.startsWith("📁")) {
                totalCarpetas++;
            } else if (nombre.startsWith("📄")) {
                totalArchivos++;
            }
        }
        
        // ¡Usamos un 'Text Block' como sugirió tu IDE!
        String reporte = """
                         === ESTADISTICAS DEL DISCO ===
                         Carpetas creadas: %d
                         Archivos creados: %d
                         Total de elementos: %d""".formatted(totalCarpetas, totalArchivos, (totalCarpetas + totalArchivos));
                         
        System.out.println(reporte);
        javax.swing.JOptionPane.showMessageDialog(this, reporte, "Estadísticas", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        // Invertimos el estado actual
        sistemaPausado = !sistemaPausado;
        
        // Como sabemos que el botón se llama jButton8, lo usamos directamente:
        if (sistemaPausado) {
            jButton8.setText("Reanudar");
            System.out.println("[Sistema] ⏸ SIMULACION PAUSADA.");
            // TODO: Aquí luego llamaremos a un método para detener el reloj del procesador
        } else {
            jButton8.setText("Pausa");
            System.out.println("[Sistema] ▶ SIMULACION REANUDADA.");
            // TODO: Aquí luego llamaremos a un método para reanudar el reloj del procesador
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InterfazProyecto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InterfazProyecto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InterfazProyecto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InterfazProyecto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InterfazProyecto().setVisible(true);
            }
        });
    }
    
    // --- CLASE INTERNA PARA REDIRIGIR LA CONSOLA ---
    class CustomOutputStream extends java.io.OutputStream {
        private javax.swing.JTextArea textArea;

        public CustomOutputStream(javax.swing.JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws java.io.IOException {
            // Redirige el texto al JTextArea de forma segura para la interfaz gráfica
            javax.swing.SwingUtilities.invokeLater(() -> {
                textArea.append(String.valueOf((char) b));
                // Mueve el scroll siempre hacia abajo automáticamente
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }
    
    /**
     * Este método actualiza la vista de los procesos en la interfaz.
     * Deberá ser llamado cada vez que un proceso cambie de estado, se encole, o termine.
     */
    public void actualizarPantallaProcesos() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== LISTOS ===\n");
        // TODO: Iterar sobre la cola de listos y hacer sb.append(proceso.toString()).append("\n");
        sb.append("\n");
        
        sb.append("=== EN CPU ===\n");
        // TODO: Mostrar el proceso actual en ejecución
        sb.append("\n");
        
        sb.append("=== BLOQUEADOS ===\n");
        // TODO: Mostrar los procesos esperando disco
        sb.append("\n");
        
        sb.append("=== I/O EN EJECUCIÓN ===\n");
        // TODO: Mostrar qué proceso está usando el disco en este instante
        sb.append("\n");
        
        sb.append("=== COLA I/O ===\n");
        // TODO: Mostrar la Cola de peticiones del disco
        sb.append("\n");
        
        // Finalmente, enviamos todo ese texto construido al JTextArea
        jTextArea2.setText(sb.toString());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel panelDiscoSimulador;
    private javax.swing.JTable tablaAsignacion;
    // End of variables declaration//GEN-END:variables
}
