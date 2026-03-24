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
    private javax.swing.JPanel[] bloquesDisco = new javax.swing.JPanel[250];
    private Modo modoActual = Modo.ADMINISTRADOR;
    private boolean sistemaPausado = false;
    private int cabezalAnterior = 0;
    private Disco.PlanificadorDisco discoSimulado;
    private String rutaPruebaActual = "";
    private Archivo.Directorio raizLogicaGlobal = new Archivo.Directorio("Disco (C:)", null);
    private java.awt.Color[] coloresBloques = new java.awt.Color[100];
    private java.util.HashMap<String, java.awt.Color> mapaColoresArchivos = new java.util.HashMap<>();
    private int cicloActual = 0;
    private boolean simulacionIniciada = false;
    private final Object lockBloques = new Object();
    public final Object lockPausa = new Object();
    private java.util.HashMap<String, javax.swing.tree.DefaultMutableTreeNode> nodosDestinoPendientes = new java.util.HashMap<>();
    private java.util.HashMap<String, java.awt.Color> coloresPendientes = new java.util.HashMap<>();
    
    private final int MAX_CACHE = 10;
    private int cacheHits = 0;
    private int cacheMisses = 0;
    
    // Nuestro objeto para guardar la info del bloque
    class RegistroCache {
        int bloque;
        String nombreArchivo;
        
        public RegistroCache(int b, String n) { 
            this.bloque = b; 
            this.nombreArchivo = n; 
        }
        
        // ¡Súper importante! Le decimos a Java cómo comparar dos registros.
        // Esto permite que el método eliminar() de tu ListaEnlazada encuentre el objeto correcto.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            RegistroCache otro = (RegistroCache) obj;
            return this.bloque == otro.bloque && this.nombreArchivo.equals(otro.nombreArchivo);
        }
    }
    
    // Instanciamos tu lista enlazada
    private estructuras.ListaEnlazada<RegistroCache> bufferCache = new estructuras.ListaEnlazada<>();
    
    
    /**
     * Creates new form InterfazProyecto
     */
    public InterfazProyecto() {
        initComponents();
        
        inicializarDiscoVisual();
        
        actualizarCabezalVisual(50);
        
        // Renderizador para la columna 0 ("Nombre Archivo")
        tablaAsignacion.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component celda = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    // Buscamos el color en nuestra memoria usando el nombre del archivo
                    java.awt.Color color = mapaColoresArchivos.get(value.toString());
                    if (color != null) {
                        celda.setBackground(color);
                        celda.setForeground(java.awt.Color.BLACK); // Texto negro para que contraste con el pastel
                    } else {
                        celda.setBackground(java.awt.Color.WHITE);
                    }
                }
                return celda;
            }
        });
        
        

        
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
        //Procesos.GestorProcesos.iniciarCPU();
        
       //iniciarRelojSistema();
        // --- CONFIGURAR PERMISOS INICIALES ---
        // Esto asegura que los botones coincidan con el modo ADMINISTRADOR al iniciar
        //actualizarPermisosBotones();
    }
    
    private void inicializarDiscoVisual() {
        // Limpiamos por si acaso NetBeans dejó algo oculto
        panelDiscoSimulador.removeAll();
        coloresBloques = new java.awt.Color[bloquesDisco.length];
        // CAMBIA EL 100 o 200 POR bloquesDisco.length
        for (int i = 0; i < bloquesDisco.length; i++) { 
            javax.swing.JPanel bloque = new javax.swing.JPanel();
            bloque.setLayout(new java.awt.BorderLayout());
            bloque.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
            bloque.setBackground(java.awt.Color.LIGHT_GRAY);
            coloresBloques[i] = java.awt.Color.LIGHT_GRAY;

            javax.swing.JLabel lblNumero = new javax.swing.JLabel(String.valueOf(i), javax.swing.SwingConstants.CENTER);
            lblNumero.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
            bloque.add(lblNumero, java.awt.BorderLayout.CENTER);

            bloquesDisco[i] = bloque;
            panelDiscoSimulador.add(bloque);
        }

        panelDiscoSimulador.revalidate();
        panelDiscoSimulador.repaint();
    }
    
    public final void actualizarCabezalVisual(int nuevaPosicion) {
        // Protección: Si el bloque es mayor a 99, lo mapeamos para que entre en la cuadrícula
        final int posSegura = (nuevaPosicion >= bloquesDisco.length) ? (nuevaPosicion % bloquesDisco.length) : nuevaPosicion;

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // 1. Despintar el bloque viejo (volverlo gris)
                bloquesDisco[cabezalAnterior].setBackground(coloresBloques[cabezalAnterior]);
                
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
                String colorHex = fileData.optString("color", "#FFFFFF");
                // Pintamos los archivos de prueba en el disco visual
                registrarArchivoEnGUI(nombreArchivo, cantidadBloques, posicionInicial, colorHex);
                
                // --- NUEVO: AGREGAR AL ÁRBOL LÓGICO Y VISUAL ---
                
                // 1. Lo agregamos a la raíz lógica para que exista en el sistema
                // (Usamos ID=1 y color blanco por defecto para la simulación)
                Archivo.Archivo nuevoArch = new Archivo.Archivo(nombreArchivo, this.raizLogicaGlobal, cantidadBloques, posicionInicial, 1, "#FFFFFF");
                this.raizLogicaGlobal.getContenido().agregar(nuevoArch);
                
                // 2. Lo agregamos visualmente al JTree para que aparezca en pantalla
                javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
                javax.swing.tree.DefaultMutableTreeNode raizVisual = (javax.swing.tree.DefaultMutableTreeNode) modeloArbol.getRoot();
                javax.swing.tree.DefaultMutableTreeNode nuevoNodoArch = new javax.swing.tree.DefaultMutableTreeNode("📄 " + nombreArchivo);
                
                // Lo insertamos en la interfaz gráfica
                modeloArbol.insertNodeInto(nuevoNodoArch, raizVisual, raizVisual.getChildCount());
                
                // Expandir el árbol para que se vean los archivos al cargar
                jTree1.expandPath(new javax.swing.tree.TreePath(raizVisual.getPath()));
                // ------------------------------------------------
            }

            org.json.JSONArray requests = jsonPrueba.getJSONArray("requests");
            for (int i = 0; i < requests.length(); i++) {
                org.json.JSONObject req = requests.getJSONObject(i);
                int posicion = req.getInt("pos");
                String operacionStr = req.getString("op").toUpperCase(); 
                
                // --- NUEVO: BUSCAR EL NOMBRE REAL Y CANTIDAD DE BLOQUES ---
                String nombreReal = "Desconocido";
                int cantidadBloques = 1; // 1 por defecto por si no lo encuentra
                String keyPos = String.valueOf(posicion);
                
                // Buscamos en el diccionario de archivos usando la posición
                if (systemFiles.has(keyPos)) {
                    org.json.JSONObject fileData = systemFiles.getJSONObject(keyPos);
                    nombreReal = fileData.getString("name");
                    cantidadBloques = fileData.getInt("blocks"); // Sacamos cuántos bloques ocupa
                }
                
                
                // --- TRADUCTOR DE INGLÉS A ESPAÑOL ---
                Procesos.TipoOperacionIO tipoOp;
                switch (operacionStr) {
                    case "READ":
                        tipoOp = Procesos.TipoOperacionIO.LEER;
                        break;
                    case "UPDATE":
                        tipoOp = Procesos.TipoOperacionIO.ACTUALIZAR;
                        break;
                    case "DELETE":
                        tipoOp = Procesos.TipoOperacionIO.ELIMINAR;
                        break;
                    case "CREATE": 
                        tipoOp = Procesos.TipoOperacionIO.CREAR;
                        break;
                    default:
                        tipoOp = Procesos.TipoOperacionIO.LEER; // Por defecto
                        break;
                }
                // --------------------------------------------
                
                // Usamos valores ficticios (1, "Simulacion", 1) para las variables que no importan en la prueba
                Procesos.SolicitudIO nuevaSolicitud = new Procesos.SolicitudIO(1, tipoOp, nombreReal, 1, posicion);
                
                // Lo metemos a la cola
                discoSimulado.getColaCompartida().encolar(nuevaSolicitud); 
                discoSimulado.registrarNuevaPeticion(); // "Despierta" al disco
            }
            System.out.println("Prueba cargada exitosamente.");
            if (!simulacionIniciada) {
                Procesos.GestorProcesos.iniciarCPU();
                iniciarRelojSistema();
                simulacionIniciada = true;
                System.out.println("[Sistema] Simulación, Reloj y CPU iniciados.");
            }
            
        } catch (org.json.JSONException e) {
            System.err.println("Error procesando el JSON: " + e.getMessage());
        }
    }
    
    private java.awt.Color generarColorPastel() {
        java.util.Random random = new java.util.Random();
        // Usamos valores de 128 a 255 para asegurar que el color sea claro (pastel)
        int r = random.nextInt(128) + 128;
        int g = random.nextInt(128) + 128;
        int b = random.nextInt(128) + 128;
        return new java.awt.Color(r, g, b);
    }

    private java.awt.Color hexAColor(String hex) {
        try {
            return java.awt.Color.decode(hex);
        } catch (Exception e) {
            return generarColorPastel(); // Si el hex es inválido, generamos uno pastel
        }
    }
    
    private void registrarArchivoEnGUI(String nombre, int bloques, int posInicial, String colorHex) {
        // 1. Convertimos el texto del JSON a un Color de Java
        java.awt.Color colorArchivo = hexAColor(colorHex);
        
        // Si el JSON no trae color o es blanco/negro, le damos un tono pastel aleatorio
        if (colorHex == null || colorHex.equals("#FFFFFF") || colorHex.equals("#000000")) {
            colorArchivo = generarColorPastel();
        }
        
        // 2. Guardamos el color en nuestra "memoria" para que la tabla sepa cómo pintarse
        mapaColoresArchivos.put(nombre, colorArchivo);

        // 3. Agregamos la fila a la tabla (Ojo: Ajusté el orden a Nombre, Posición, Bloques 
        // para que coincida con las columnas de tu diseño)
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
        modeloTabla.addRow(new Object[]{nombre, posInicial, bloques});
        
        // 4. Pintamos el disco
        for (int i = 0; i < bloques; i++) {
            int posicionActual = posInicial + i;
            
            if (posicionActual < bloquesDisco.length) {
                // GUARDAMOS el color en la memoria de los bloques
                coloresBloques[posicionActual] = colorArchivo;
                
                // Pintamos el bloque, a menos que el cabezal rojo esté parado exactamente ahí
                if (posicionActual != cabezalAnterior) {
                    bloquesDisco[posicionActual].setBackground(colorArchivo);
                }
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
    
    public void procesarPeticionCache(int bloqueBuscado, String nombreArchivo) {
        boolean encontrado = false;
        
        // 1. Buscamos en tu lista recorriéndola por índice
        for (int i = 0; i < bufferCache.getTamano(); i++) {
            RegistroCache reg = bufferCache.get(i); 
            if (reg.bloque == bloqueBuscado) {
                encontrado = true;
                break;
            }
        }
        
        // 2. Evaluamos Hit o Miss
        if (encontrado) {
            cacheHits++; // Hit: el bloque ya estaba en memoria
        } else {
            cacheMisses++; // Miss: el bloque no estaba
            
            // FIFO: Si la lista está llena (10), borramos el más viejo (el del índice 0)
            if (bufferCache.getTamano() >= MAX_CACHE) {
                RegistroCache masViejo = bufferCache.get(0);
                bufferCache.eliminar(masViejo); // Tu método eliminar() lo sacará del frente
            }
            
            // Metemos el bloque nuevo al final de la cola
            bufferCache.agregar(new RegistroCache(bloqueBuscado, nombreArchivo));
        }
        
        // 3. Refrescamos la UI
        actualizarPantallaCache();
    }

    private void actualizarPantallaCache() {
        StringBuilder sb = new StringBuilder();
        
        // Recorremos tu lista para imprimir el texto como en la imagen del profesor
        for (int i = 0; i < bufferCache.getTamano(); i++) {
            RegistroCache reg = bufferCache.get(i);
            sb.append("Bloque ").append(reg.bloque).append(" (").append(reg.nombreArchivo).append(")\n");
        }
        
        // Asumiendo que tu JTextArea se llama txtAreaCache y tu JLabel lblEstadisticasCache
        txtAreaCache.setText(sb.toString()); 
        
        int total = cacheHits + cacheMisses;
        double tasa = (total == 0) ? 0.0 : ((double) cacheHits / total) * 100.0;
        
        String textoStats = String.format("Hits: %d | Misses: %d | Total: %d | Tasa: %.1f%%", 
                                          cacheHits, cacheMisses, total, tasa);
        
        lblEstadisticasCache.setText(textoStats); 
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
        jScrollPane5 = new javax.swing.JScrollPane();
        txtAreaCache = new javax.swing.JTextArea();
        lblEstadisticasCache = new javax.swing.JLabel();
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
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

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
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 29, Short.MAX_VALUE))
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

        txtAreaCache.setColumns(20);
        txtAreaCache.setRows(5);
        jScrollPane5.setViewportView(txtAreaCache);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEstadisticasCache, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblEstadisticasCache, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
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
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Cargar Estado del Sistema (.json)");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem3.setText("Salir");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Reportes");

        jMenuItem4.setText("Reportar Resumen del Sistema (.txt)");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
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
        // TODO add your handling code here:
        String planSeleccionado = jComboBox1.getSelectedItem().toString();
        System.out.println(">>> CAMBIANDO POLÍTICA A: " + planSeleccionado);
        
        // 1. Cambiamos la política dinámicamente
        switch (planSeleccionado) {
            case "FIFO": discoSimulado.setPolitica(new politicas.FIFO()); break;
            case "SSTF": discoSimulado.setPolitica(new politicas.SSTF()); break;
            case "SCAN": discoSimulado.setPolitica(new politicas.SCAN()); break;
            case "CSCAN": discoSimulado.setPolitica(new politicas.CSCAN(discoSimulado)); break;
        }
        
        // 2. Limpiamos toda la cuadrícula visualmente (solo los espacios grises que recorre el cabezal)
        for (int i = 0; i < bloquesDisco.length; i++) {
            // Asegúrate de no borrar los colores de los archivos ya cargados
            if (bloquesDisco[i] != null && coloresBloques[i] == java.awt.Color.LIGHT_GRAY) {
                bloquesDisco[i].setBackground(java.awt.Color.LIGHT_GRAY);
            }
        }
        
        // 3. Reiniciamos la posición del cabezal al punto de partida
        discoSimulado.setCabezal(50);
        actualizarCabezalVisual(50);
        
        // 4. Volvemos a mandar "trabajo" al disco para que arranque
        reiniciarPeticionesSimulacion();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        // Limpiar el log de eventos
        jTextArea1.setText("");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        // 1. Validaciones Iniciales
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona una carpeta donde guardar el archivo.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombreDirectorio = nodoSeleccionado.getUserObject().toString();
        if (!nodoSeleccionado.isRoot() && !nombreDirectorio.startsWith("📁")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Solo puedes crear archivos dentro de carpetas o en el Disco (C:).", "Acción denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 2. Solicitar Nombre del Archivo
        String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre del nuevo archivo (ej. documento.txt):");
        if (nombre == null || nombre.trim().isEmpty()) return;
        String nombreLimpio = nombre.trim();
        
        // 3. Solicitar Tamaño del Archivo en Bloques
        String tamañoStr = javax.swing.JOptionPane.showInputDialog(this, "Tamaño del archivo en bloques (ej. 3):");
        if (tamañoStr == null || tamañoStr.trim().isEmpty()) return;
        
        int tamanoBloques;
        try {
            tamanoBloques = Integer.parseInt(tamañoStr.trim());
            if (tamanoBloques <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "El tamaño debe ser un número entero mayor a 0.", "Error de Formato", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 4. Buscar Espacio Contiguo en el Disco (Validación previa)
        int bloqueInicial = buscarEspacioLibreContiguo(tamanoBloques);
        if (bloqueInicial == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay espacio suficiente o contiguo en el disco.", "Disco Lleno", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 5. Guardar temporalmente los datos visuales
        java.awt.Color colorNuevo = generarColorPastel();
        nodosDestinoPendientes.put(nombreLimpio, nodoSeleccionado);
        coloresPendientes.put(nombreLimpio, colorNuevo);
        
        // 6. Crear la Solicitud de Entrada/Salida (I/O)
        int pIdSimulado = Procesos.GestorProcesos.getCantidadProcesos() + 1;
        Procesos.SolicitudIO nuevaSolicitud = new Procesos.SolicitudIO(
                pIdSimulado, 
                Procesos.TipoOperacionIO.CREAR, // Asumiendo que "CREAR" está en tu enum
                nombreLimpio, 
                tamanoBloques, 
                bloqueInicial
        );
        
        // 7. Enviar la Solicitud al PCB y al Gestor
        Procesos.PCB nuevoProceso = new Procesos.PCB("Crear_" + nombreLimpio, nuevaSolicitud);
        Procesos.GestorProcesos.agregarProceso(nuevoProceso);
        
        // 8. Arrancar el HiloProceso (Esto pasará el proceso por la CPU y lo encolará al Disco)
        // OJO: Usamos el constructor de 4 parámetros incluyendo el "this"
        Procesos.HiloProceso hilo = new Procesos.HiloProceso(nuevoProceso, discoSimulado.getColaCompartida(), discoSimulado, this);
        new Thread(hilo).start();
        
        // 9. Actualizar la pantalla
        this.actualizarPantallaProcesos();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, selecciona una carpeta en el arbol primero.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombrePadreConEmoji = nodoSeleccionado.getUserObject().toString();
        if (!nodoSeleccionado.isRoot() && !nombrePadreConEmoji.startsWith("📁")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Solo puedes crear directorios dentro de otras carpetas o en la raíz.", "Acción denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombre = javax.swing.JOptionPane.showInputDialog(this, "Nombre del nuevo directorio:");
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            String nombreLimpio = nombre.trim();
            
            // --- PASO 1: Guardamos en memoria temporal el nodo donde queremos que aparezca visualmente ---
            // Reutilizamos el mapa que creamos para "Crear Archivo"
            nodosDestinoPendientes.put("DIR_" + nombreLimpio, nodoSeleccionado);
            
            // --- PASO 2: CREAR LA SOLICITUD ---
            int pIdSimulado = Procesos.GestorProcesos.getCantidadProcesos() + 1;
            // Para un directorio, el tamaño es 0 y no necesita bloque inicial. Usamos bloque 0 o -1 por convención.
            Procesos.SolicitudIO peticion = new Procesos.SolicitudIO(pIdSimulado, Procesos.TipoOperacionIO.CREAR, "DIR_" + nombreLimpio, 0, 0); 
            
            // --- PASO 3: ENVIAR AL PCB Y ARRANCAR HILO ---
            Procesos.PCB nuevoProceso = new Procesos.PCB("Crear_Dir_" + nombreLimpio, peticion);
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            
            // Lanzamos el hilo y le pasamos el "this" de la interfaz
            Procesos.HiloProceso hilo = new Procesos.HiloProceso(nuevoProceso, discoSimulado.getColaCompartida(), discoSimulado, this);
            new Thread(hilo).start();
            
            this.actualizarPantallaProcesos();
            System.out.println("[Sistema] Solicitud de creación de directorio enviada.");
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
        
        String nombreLimpio = nombre.substring(2).trim();
        
        // --- PASO 1: Buscar el bloque inicial en la Tabla de Asignación ---
        int bloqueInicial = -1;
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
        
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (modeloTabla.getValueAt(i, 0) != null && modeloTabla.getValueAt(i, 0).toString().equals(nombreLimpio)) {
                bloqueInicial = Integer.parseInt(modeloTabla.getValueAt(i, 1).toString());
                break;
            }
        }
        
        if (bloqueInicial == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: No se encontró el bloque físico del archivo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- PASO 2: CREAR LA SOLICITUD Y EL PROCESO ---
        int idProceso = Procesos.GestorProcesos.getCantidadProcesos() + 1;
        Procesos.SolicitudIO peticionLectura = new Procesos.SolicitudIO(idProceso, Procesos.TipoOperacionIO.LEER, nombreLimpio, 1, bloqueInicial);
        
        Procesos.PCB nuevoProceso = new Procesos.PCB("Leer_" + nombreLimpio, peticionLectura);
        Procesos.GestorProcesos.agregarProceso(nuevoProceso);
        
        // --- PASO 3: ARRANCAR EL HILO PROCESO (Para que pase por la CPU) ---
        // ¡Este hilo se encargará internamente de encolar y despertar al disco!
        Procesos.HiloProceso hilo = new Procesos.HiloProceso(nuevoProceso, discoSimulado.getColaCompartida(), discoSimulado, this);
        new Thread(hilo).start();

        this.actualizarPantallaProcesos();

        // Simulación visual inicial
        javax.swing.JOptionPane.showMessageDialog(this, "Solicitud de lectura enviada a CPU.\nLuego pasará a la cola y el disco se moverá hacia el bloque " + bloqueInicial + ".", "Leyendo Archivo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        javax.swing.tree.DefaultMutableTreeNode nodoSeleccionado = (javax.swing.tree.DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
    
        if (nodoSeleccionado == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un archivo o carpeta para renombrar.");
            return;
        }
        if (nodoSeleccionado.isRoot()) {
            javax.swing.JOptionPane.showMessageDialog(this, "¡No puedes renombrar el Disco Principal!", "Acción denegada", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String nombreAntiguoConEmoji = nodoSeleccionado.getUserObject().toString();
        boolean esCarpeta = nombreAntiguoConEmoji.startsWith("📁");
        String nombreAntiguoLimpio = nombreAntiguoConEmoji.substring(2).trim();
    
        String nombreNuevo = javax.swing.JOptionPane.showInputDialog(this, "Ingresa el nuevo nombre para '" + nombreAntiguoLimpio + "':", nombreAntiguoLimpio);
        if (nombreNuevo == null || nombreNuevo.trim().isEmpty() || nombreNuevo.trim().equals(nombreAntiguoLimpio)) {
            return; 
        }
        String nombreNuevoLimpio = nombreNuevo.trim();

        // Buscamos el bloque para que el Disco sepa a dónde mover el cabezal
        int bloqueInicial = 0; // Para carpetas simulamos el bloque 0
        if (!esCarpeta) {
            javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                if (modeloTabla.getValueAt(i, 0) != null && modeloTabla.getValueAt(i, 0).toString().equals(nombreAntiguoLimpio)) {
                    bloqueInicial = Integer.parseInt(modeloTabla.getValueAt(i, 1).toString());
                    break;
                }
            }
        }

        // --- PASO 1: Guardar en memoria temporal ---
        nodosDestinoPendientes.put("REN_" + nombreAntiguoLimpio, nodoSeleccionado);

        // --- PASO 2: CREAR LA SOLICITUD Y EL PCB ---
        int idProceso = Procesos.GestorProcesos.getCantidadProcesos() + 1;
        // Usamos el constructor de 6 parámetros que incluye el nuevoNombre
        Procesos.SolicitudIO peticion = new Procesos.SolicitudIO(idProceso, Procesos.TipoOperacionIO.ACTUALIZAR, nombreAntiguoLimpio, 0, bloqueInicial, nombreNuevoLimpio);
        
        Procesos.PCB nuevoProceso = new Procesos.PCB("Renombrar_" + nombreAntiguoLimpio, peticion);
        Procesos.GestorProcesos.agregarProceso(nuevoProceso);

        // --- PASO 3: ARRANCAR HILO ---
        Procesos.HiloProceso hilo = new Procesos.HiloProceso(nuevoProceso, discoSimulado.getColaCompartida(), discoSimulado, this);
        new Thread(hilo).start();

        this.actualizarPantallaProcesos();
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
            String nombreLimpio = nombreNodo.substring(2).trim();
            boolean esCarpeta = nombreNodo.startsWith("📁");
            
            int bloqueInicial = 0; 
            int tamanoBloques = 0;
            
            if (!esCarpeta) {
                javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if (modeloTabla.getValueAt(i, 0) != null && modeloTabla.getValueAt(i, 0).toString().equals(nombreLimpio)) {
                        bloqueInicial = Integer.parseInt(modeloTabla.getValueAt(i, 1).toString());
                        tamanoBloques = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
                        break; 
                    }
                }
            }
            
            // --- PASO 1: Guardar en memoria temporal ---
            nodosDestinoPendientes.put("DEL_" + nombreLimpio, nodoSeleccionado);
            
            // --- PASO 2: CREAR SOLICITUD Y PCB ---
            int idProceso = Procesos.GestorProcesos.getCantidadProcesos() + 1;
            Procesos.SolicitudIO peticion = new Procesos.SolicitudIO(idProceso, Procesos.TipoOperacionIO.ELIMINAR, nombreLimpio, tamanoBloques, bloqueInicial);
            
            Procesos.PCB nuevoProceso = new Procesos.PCB("Eliminar_" + nombreLimpio, peticion); 
            Procesos.GestorProcesos.agregarProceso(nuevoProceso);
            
            // --- PASO 3: ARRANCAR HILO ---
            Procesos.HiloProceso hilo = new Procesos.HiloProceso(nuevoProceso, discoSimulado.getColaCompartida(), discoSimulado, this);
            new Thread(hilo).start();
            
            this.actualizarPantallaProcesos();
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        // 1. Conteo Lógico (Tu código original)
        javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
        javax.swing.tree.DefaultMutableTreeNode raiz = (javax.swing.tree.DefaultMutableTreeNode) modelo.getRoot();
        
        int totalCarpetas = 0;
        int totalArchivos = 0;
        
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
        
        // 2. Conteo Físico del Disco (NUEVO)
        int bloquesOcupados = 0;
        int bloquesLibres = 0;
        
        synchronized(lockBloques) { // Bloqueamos un microsegundo para que la lectura sea exacta
            for (int i = 0; i < coloresBloques.length; i++) {
                if (coloresBloques[i] == null || coloresBloques[i].equals(java.awt.Color.LIGHT_GRAY)) {
                    bloquesLibres++;
                } else {
                    bloquesOcupados++;
                }
            }
        }
        
        // Calculamos porcentaje de uso
        double porcentajeUso = ((double) bloquesOcupados / coloresBloques.length) * 100.0;
        
        // 3. Reporte Mejorado
        String reporte = """
                         === ESTADÍSTICAS DEL SISTEMA ===
                         
                         [ Estructura Lógica ]
                         Carpetas creadas: %d
                         Archivos creados: %d
                         Total de elementos: %d
                         
                         [ Almacenamiento Físico ]
                         Bloques Ocupados: %d
                         Bloques Libres: %d
                         Uso del Disco: %.1f%%""".formatted(
                             totalCarpetas, 
                             totalArchivos, 
                             (totalCarpetas + totalArchivos),
                             bloquesOcupados,
                             bloquesLibres,
                             porcentajeUso
                         );
                         
        System.out.println(reporte);
        javax.swing.JOptionPane.showMessageDialog(this, reporte, "Estadísticas del Disco", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        // Invertimos el estado actual
        sistemaPausado = !sistemaPausado;
        
        if (sistemaPausado) {
            jButton8.setText("Reanudar");
            System.out.println("[Sistema] ⏸ SIMULACION PAUSADA.");
            
            // Avisamos al Gestor de Procesos que congele la CPU
            Procesos.GestorProcesos.pausarSimulacion();
            
        } else {
            jButton8.setText("Pausa");
            System.out.println("[Sistema] ▶ SIMULACION REANUDADA.");
            
            // 1. Avisamos al Gestor de Procesos que arranque la CPU
            Procesos.GestorProcesos.reanudarSimulacion();
            
            // 2. Despertamos al Disco (si es que estaba dormido en el wait)
            synchronized(lockPausa) {
                lockPausa.notifyAll();
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar estado del sistema como...");
        
        int seleccion = fileChooser.showSaveDialog(this);
        
        if (seleccion == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoSeleccionado = fileChooser.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();
            
            if (!ruta.toLowerCase().endsWith(".json")) {
                ruta += ".json";
            }
            
            // Actualizamos la lógica leyendo lo que hay en la pantalla antes de guardar
            this.raizLogicaGlobal = reconstruirLogicaDesdeVisual(); 
            
            boolean exito = Utilidades.GestorJSON.guardarSistema(this.raizLogicaGlobal, ruta);
            
            if (exito) {
                javax.swing.JOptionPane.showMessageDialog(this, "Sistema guardado exitosamente en:\n" + ruta, "Guardado Exitoso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                System.out.println("[Sistema] Arbol guardado en: " + ruta);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Hubo un error al intentar guardar el archivo.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Cargar Archivo JSON");
        
        if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoSeleccionado = fileChooser.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();
            
            try {
                // Leemos todo el contenido del archivo como texto
                String contenidoJSON = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(ruta)));
                
                // Verificamos qué tipo de JSON es
                if (contenidoJSON.contains("\"test_id\"") || contenidoJSON.contains("\"requests\"")) {
                    // 1. Es el caso de prueba oficial de la profesora
                    System.out.println("[Sistema] Detectado archivo de simulacion oficial. Iniciando...");
                    
                    // Limpiamos el disco visual por si acaso
                    for (int i = 0; i < bloquesDisco.length; i++) {
                        if (bloquesDisco[i] != null) {
                            bloquesDisco[i].setBackground(java.awt.Color.LIGHT_GRAY);
                        }
                    }
                    
                    // Limpiamos la tabla
                    javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
                    modeloTabla.setRowCount(0);
                    
                    // Llamamos a tu método de prueba
                    ejecutarPruebaJSON(ruta);
                    
                } else {
                    // 2. Es un archivo de guardado normal de ustedes
                    System.out.println("[Sistema] Detectado archivo de estado del sistema (Arbol).");
                    cargarArbolDesdeJSON(ruta);
                }
                
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        System.out.println("[Sistema] Cerrando el simulador...");
        System.exit(0); // Cierra la aplicación de forma segura
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Resumen del Sistema (.txt)");
        
        if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fileChooser.getSelectedFile();
            String ruta = archivo.getAbsolutePath();
            
            // Asegurar que termine en .txt
            if (!ruta.toLowerCase().endsWith(".txt")) {
                ruta += ".txt";
            }

            try (java.io.FileWriter writer = new java.io.FileWriter(ruta)) {
                writer.write("=========================================\n");
                writer.write("       RESUMEN DEL SISTEMA DE ARCHIVOS   \n");
                writer.write("=========================================\n\n");
                
                javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
                javax.swing.tree.DefaultMutableTreeNode raiz = (javax.swing.tree.DefaultMutableTreeNode) modelo.getRoot();
                
                int totalCarpetas = 0;
                int totalArchivos = 0;
                
                // Recorremos el árbol visual para dibujarlo en el TXT
                var enumeracion = raiz.preorderEnumeration(); // Usamos pre-orden para que salga de arriba hacia abajo
                
                while (enumeracion.hasMoreElements()) {
                    javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) enumeracion.nextElement();
                    String nombre = nodo.getUserObject().toString();
                    
                    // Calculamos la sangría (espacios) basada en qué tan profunda está la carpeta
                    StringBuilder sangria = new StringBuilder();
                    for (int i = 0; i < nodo.getLevel(); i++) {
                        sangria.append("    "); // 4 espacios por cada nivel
                    }
                    
                    writer.write(sangria.toString() + nombre + "\n");
                    
                    if (nombre.startsWith("📁")) totalCarpetas++;
                    else if (nombre.startsWith("📄")) totalArchivos++;
                }
                
                writer.write("\n=========================================\n");
                writer.write("ESTADISTICAS GLOBALES:\n");
                writer.write("Total Carpetas: " + totalCarpetas + "\n");
                writer.write("Total Archivos: " + totalArchivos + "\n");
                writer.write("=========================================\n");

                javax.swing.JOptionPane.showMessageDialog(this, "Resumen exportado exitosamente a:\n" + ruta, "Éxito", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                System.out.println("[Reporte] Resumen guardado en: " + ruta);
                
            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
       // Obtenemos el valor directamente en milisegundos (ej. 500)
        int velocidadMs = jSlider1.getValue();
        
        // Actualizamos la etiqueta de la interfaz
        jLabel3.setText(velocidadMs + " ms");
        
        // Le pasamos la nueva velocidad al Gestor de Procesos
        Procesos.GestorProcesos.velocidadSimulacion = velocidadMs;
    }//GEN-LAST:event_jSlider1StateChanged

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
        
        // 1. Obtenemos todos los procesos desde el Gestor
        Procesos.PCB[] procesos = Procesos.GestorProcesos.getTodosLosProcesos();
        int cantidad = Procesos.GestorProcesos.getCantidadProcesos();
        
        sb.append("=== LISTOS ===\n");
        for (int i = 0; i < cantidad; i++) {
            if (procesos[i] != null && procesos[i].getEstado().toString().equals("LISTO")) { 
                sb.append(procesos[i].toString()).append("\n");
            }
        }
        sb.append("\n");
        
        sb.append("=== EN CPU ===\n");
        for (int i = 0; i < cantidad; i++) {
            if (procesos[i] != null && procesos[i].getEstado().toString().equals("EJECUTANDO")) {
                sb.append(procesos[i].toString()).append("\n");
            }
        }
        sb.append("\n");
        
        sb.append("=== BLOQUEADOS ===\n");
        for (int i = 0; i < cantidad; i++) {
            if (procesos[i] != null && procesos[i].getEstado().toString().equals("BLOQUEADO")) {
                sb.append(procesos[i].toString()).append("\n");
            }
        }
        sb.append("\n");
        
        sb.append("=== COLA I/O ===\n");
        if (discoSimulado != null && discoSimulado.getColaCompartida() != null) {
            // Utilizamos el toString() de tu clase Cola (Asegúrate de que tu clase Cola imprima sus elementos)
            sb.append(discoSimulado.getColaCompartida().toString()); 
        }
        sb.append("\n");
        
        sb.append("=== I/O EN EJECUCIÓN ===\n");
        if (discoSimulado != null && discoSimulado.getPeticionActual() != null) {
            // Suponiendo que creas este getter en PlanificadorDisco
            sb.append(discoSimulado.getPeticionActual().toString()).append("\n");
        } else {
            sb.append("Ninguna\n");
        }
        sb.append("\n");
        
        // 2. Actualizamos el JTextArea de forma segura para Swing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jTextArea2.setText(sb.toString());
            }
        });
    }
    
    private Archivo.Directorio reconstruirLogicaDesdeVisual() {
        javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
        javax.swing.tree.DefaultMutableTreeNode raizVisual = (javax.swing.tree.DefaultMutableTreeNode) modelo.getRoot();
        
        // Creamos la raíz lógica limpia
        Archivo.Directorio raizLogica = new Archivo.Directorio("Disco (C:)", null);
        
        // Empezamos la magia de la recursividad
        recorrerYConstruir(raizVisual, raizLogica);
        return raizLogica;
    }

    private void recorrerYConstruir(javax.swing.tree.DefaultMutableTreeNode nodoVisual, Archivo.Directorio dirLogicoPadre) {
        for (int i = 0; i < nodoVisual.getChildCount(); i++) {
            javax.swing.tree.DefaultMutableTreeNode hijoVisual = (javax.swing.tree.DefaultMutableTreeNode) nodoVisual.getChildAt(i);
            String nombreNodo = hijoVisual.getUserObject().toString();
            
            if (nombreNodo.startsWith("📁")) {
                // Si es carpeta, le quitamos el emoji y creamos el objeto Directorio
                String nombreLimpio = nombreNodo.substring(2).trim();
                Archivo.Directorio nuevoDir = new Archivo.Directorio(nombreLimpio, dirLogicoPadre);
                dirLogicoPadre.getContenido().agregar(nuevoDir);
                
                // Llamada recursiva por si hay carpetas dentro de esta carpeta
                recorrerYConstruir(hijoVisual, nuevoDir);
                
            } else if (nombreNodo.startsWith("📄")) {
                // Si es archivo, creamos el objeto Archivo con valores simulados por ahora
                String nombreLimpio = nombreNodo.substring(2).trim();
                // Constructor: Archivo(nombre, padre, tamaño, bloque, idProceso, color)
                Archivo.Archivo nuevoArch = new Archivo.Archivo(nombreLimpio, dirLogicoPadre, 1, 0, 1, "#FFFFFF");
                dirLogicoPadre.getContenido().agregar(nuevoArch);
            }
        }
    }
    
    private void iniciarRelojSistema() {
        new Thread(() -> {
            while (!sistemaPausado) { // Si tienes pausa más adelante, esto servirá
                try {
                    // El reloj respeta la velocidad de la variable global
                    Thread.sleep(Procesos.GestorProcesos.velocidadSimulacion);
                    
                    cicloActual++;
                    
                    // Actualizamos jLabel4 en la pantalla de forma segura
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        jLabel4.setText("Ciclo: " + cicloActual);
                    });
                    
                } catch (InterruptedException e) {
                    System.out.println("Reloj detenido.");
                    break;
                }
            }
        }).start();
    }
    
    public void reiniciarPeticionesSimulacion() {
        new Thread(() -> {
            try {
                System.out.println("=== RE-ENCOLANDO PETICIONES ===");
                // Cargamos el JSON de la ruta actual
                org.json.JSONObject jsonPrueba = Utilidades.GestorJSON.cargarPruebaSimulacion(this.rutaPruebaActual);
                
                if (jsonPrueba == null) return;
                
                if (!jsonPrueba.has("requests")) {
                    String aviso = "Aviso: El JSON actual no contiene pruebas de simulación ('requests'). El disco no se moverá.";
                    System.out.println(aviso);
                    // jTextAreaLog.append(aviso + "\n");
                    return; // Salimos del método sin romper el programa
                }
                
                org.json.JSONArray requests = jsonPrueba.getJSONArray("requests");
                
                // Obtenemos la cola existente de tu disco simulado
                estructuras.Cola<Procesos.SolicitudIO> colaIO = discoSimulado.getColaCompartida();
                
                // Encolar las peticiones nuevamente
                for (int i = 0; i < requests.length(); i++) {
                    org.json.JSONObject req = requests.getJSONObject(i);
                    int posicion = req.getInt("pos");
                    String operacionStr = req.getString("op").toUpperCase();
                    
                    Procesos.TipoOperacionIO tipoOp;
                    switch (operacionStr) {
                        case "READ": tipoOp = Procesos.TipoOperacionIO.LEER; break;
                        case "UPDATE": tipoOp = Procesos.TipoOperacionIO.ACTUALIZAR; break;
                        default: tipoOp = Procesos.TipoOperacionIO.ELIMINAR; break;
                    }
                    
                    // Mapeo seguro visual
                    int posVisual = posicion >= bloquesDisco.length ? (posicion % bloquesDisco.length) : posicion;
                    Procesos.SolicitudIO nuevaSolicitud = new Procesos.SolicitudIO(i, tipoOp, "archivo", 1, posVisual);
                    
                    // Metemos la solicitud a la cola compartida
                    colaIO.encolar(nuevaSolicitud);
                }
                
                // ¡Despertamos al disco enviando los semáforos correspondientes!
                for (int i = 0; i < requests.length(); i++) {
                    discoSimulado.registrarNuevaPeticion();
                }
            }catch (org.json.JSONException e) {
                // Atrapamos errores específicos de formato JSON
                String errorMsg = "Error al leer el formato JSON: " + e.getMessage();
                System.err.println(errorMsg);
                // Ejemplo: jTextAreaLog.append(errorMsg + "\n");
            } catch (Exception e) {
                // Un catch genérico por si falla otra cosa inesperada
                String errorMsg = "Error inesperado en simulación: " + e.getMessage();
                // Ejemplo: jTextAreaLog.append(errorMsg + "\n");
            }
        }).start();
    }
    
    /**
     * Busca un archivo o directorio por su nombre dentro del árbol lógico y lo elimina.
     */
    private boolean eliminarArchivoLogico(Archivo.Directorio directorioPadre, String nombreBuscado) {
        if (directorioPadre == null || directorioPadre.getContenido() == null) return false;
        
        // Recorremos el contenido del directorio
        for (int i = 0; i < directorioPadre.getContenido().getTamano(); i++) {
            Archivo.EntradaSistemaArchivos entrada = directorioPadre.getContenido().get(i);
            // Si encontramos el archivo/carpeta, lo removemos de la lista
            if (entrada.getNombre().equals(nombreBuscado)) {
                directorioPadre.getContenido().eliminar(entrada);
                return true;
            }
            
            // Si es una subcarpeta, entramos a buscar recursivamente
            if (entrada instanceof Archivo.Directorio) {
                boolean eliminado = eliminarArchivoLogico((Archivo.Directorio) entrada, nombreBuscado);
                if (eliminado) return true; // Si lo encontró y borró adentro, terminamos
            }
        }
        return false;
    }
    
    /**
    * Busca N bloques vacíos de forma consecutiva en el disco.
    * @param tamaño Requerimiento de bloques del nuevo archivo.
    * @return El índice de inicio si encuentra espacio, o -1 si no hay espacio.
    */
    private int buscarEspacioLibreContiguo(int tamaño) {
        int contadorLibres = 0;
    
        for (int i = 0; i < coloresBloques.length; i++) {
            // Un bloque está libre si su color es nulo o gris claro
            if (coloresBloques[i] == null || coloresBloques[i].equals(java.awt.Color.LIGHT_GRAY)) {
                contadorLibres++;
            
                // Si ya encontramos la cantidad de bloques seguidos que necesitamos
                if (contadorLibres == tamaño) {
                    return (i - tamaño) + 1; // Devolvemos el índice donde empieza el hueco
                }
            } else {
                // Si chocamos con un bloque ocupado, reiniciamos el contador a 0
                contadorLibres = 0;
            }
        }
        return -1; // Retorna -1 si recorrió todo el disco y no encontró un hueco suficientemente grande
    }

    /**
    * Busca recursivamente el directorio padre y añade el nuevo objeto Archivo.Archivo en él.
    */
    private boolean agregarArchivoLogico(Archivo.Directorio directorioActual, String nombrePadreBuscado, Archivo.EntradaSistemaArchivos nuevoArchivo) {
        if (directorioActual == null) return false;

        // Si encontramos el directorio destino
        if (directorioActual.getNombre().equals(nombrePadreBuscado)) {
            // Usamos el método de tu ListaEnlazada para agregar
            directorioActual.getContenido().agregar(nuevoArchivo); 
            return true;
        }
    
        // Si no es el directorio, buscamos profundamente en las subcarpetas
        for (int i = 0; i < directorioActual.getContenido().getTamano(); i++) {
            Archivo.EntradaSistemaArchivos entrada = directorioActual.getContenido().get(i);
        
            if (entrada instanceof Archivo.Directorio) {
                boolean agregado = agregarArchivoLogico((Archivo.Directorio) entrada, nombrePadreBuscado, nuevoArchivo);
                if (agregado) return true;
            }
        }
    
        return false;
    }
    
    /**
     * Método que el Planificador llama para intentar procesar un bloque.
     * Maneja su propia sincronización de forma transparente usando el lock global.
     */
    public boolean ejecutarPeticionEnDiscoSegura(Procesos.SolicitudIO peticion) {
        String nombreArchivo = peticion.getRuta();
        String tipoOperacion = peticion.getTipo().toString();
        
        // --- ACTUALIZACIÓN DEL CABEZAL ---
        int bloqueObj = peticion.getBloqueObjetivo();
        // Validamos que sea >= 0 (algunas operaciones como crear directorios puros podrían enviar -1)
        if (bloqueObj >= 0) { 
            int posSegura = bloqueObj >= bloquesDisco.length ? (bloqueObj % bloquesDisco.length) : bloqueObj;
            actualizarCabezalVisual(posSegura);
        }
        
        // ===============================================
        // 1. LÓGICA PARA CREAR (ARCHIVOS Y DIRECTORIOS)
        // ===============================================
        if (tipoOperacion.equals("CREAR")) {
            
            // --- Caso A: Es un Directorio ---
            if (nombreArchivo.startsWith("DIR_")) {
                String nombreLimpio = nombreArchivo.substring(4); // Quitamos el "DIR_"
                javax.swing.tree.DefaultMutableTreeNode nodoPadre = nodosDestinoPendientes.remove(nombreArchivo);
                
                if (nodoPadre == null) return false;
                
                String nombrePadreConEmoji = nodoPadre.getUserObject().toString();
                String nombrePadreLimpio = nodoPadre.isRoot() ? "Disco (C:)" : nombrePadreConEmoji.substring(2).trim();
                
                // Memoria lógica en RAM
                Archivo.Directorio nuevoDirLogico = new Archivo.Directorio(nombreLimpio, null);
                agregarArchivoLogico(raizLogicaGlobal, nombrePadreLimpio, nuevoDirLogico);
                
                // Árbol Visual
                javax.swing.tree.DefaultMutableTreeNode nuevoNodo = new javax.swing.tree.DefaultMutableTreeNode("📁 " + nombreLimpio);
                javax.swing.tree.DefaultTreeModel modelo = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
                modelo.insertNodeInto(nuevoNodo, nodoPadre, nodoPadre.getChildCount());
                jTree1.scrollPathToVisible(new javax.swing.tree.TreePath(nuevoNodo.getPath()));
                
                return true;
                
            } else {
            // --- Caso B: Es un Archivo ---
                int tamano = peticion.getTamanoEnBloques();
                int bloqueArranque = peticion.getBloqueObjetivo();
                
                // Recuperamos el nodo y el color que guardamos en la memoria temporal
                javax.swing.tree.DefaultMutableTreeNode nodoPadre = nodosDestinoPendientes.get(nombreArchivo);
                java.awt.Color color = coloresPendientes.get(nombreArchivo);
                
                if (nodoPadre == null || color == null) {
                    return false; // Error de seguridad: se perdieron los datos visuales
                }
                
                String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                
                // BLOQUEO: Modificar el Disco Físico (Visual y Arreglos)
                synchronized(lockBloques) {
                    mapaColoresArchivos.put(nombreArchivo, color);
                    for (int i = 0; i < tamano; i++) {
                        int posActual = bloqueArranque + i;
                        coloresBloques[posActual] = color;
                        if (posActual != cabezalAnterior && bloquesDisco[posActual] != null) {
                            bloquesDisco[posActual].setBackground(color);
                        }
                    }
                }
                
                // Actualizar la Tabla de Asignación Visual
                javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
                modeloTabla.addRow(new Object[]{nombreArchivo, bloqueArranque, tamano});
                
                // Crear la Estructura Lógica
                String nombreDirectorio = nodoPadre.getUserObject().toString();
                String nombrePadreLimpio = nodoPadre.isRoot() ? "Disco (C:)" : nombreDirectorio.substring(2).trim();
                Archivo.Archivo archivoLogico = new Archivo.Archivo(nombreArchivo, null, tamano, bloqueArranque, peticion.getIdProceso(), hexColor);
                agregarArchivoLogico(raizLogicaGlobal, nombrePadreLimpio, archivoLogico);
                
                // Actualizar el Árbol Visual
                javax.swing.tree.DefaultMutableTreeNode nuevoNodo = new javax.swing.tree.DefaultMutableTreeNode("📄 " + nombreArchivo);
                javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
                modeloArbol.insertNodeInto(nuevoNodo, nodoPadre, nodoPadre.getChildCount());
                jTree1.scrollPathToVisible(new javax.swing.tree.TreePath(nuevoNodo.getPath()));
                
                // Limpiamos memoria
                nodosDestinoPendientes.remove(nombreArchivo);
                coloresPendientes.remove(nombreArchivo);
                
                return true;
            }
        }
        
        // ===============================================
        // 2. LÓGICA PARA LEER
        // ===============================================
        if (tipoOperacion.equals("LEER")) {
            System.out.println("[UI] Actualizando interfaz: Lectura de " + nombreArchivo + " completada.");
            return true;
        }

        // ===============================================
        // 3. LÓGICA PARA ACTUALIZAR (RENOMBRAR)
        // ===============================================
        if (tipoOperacion.equals("ACTUALIZAR")) {
            String nombreAntiguoLimpio = peticion.getRuta();
            String nombreNuevoLimpio = peticion.getNuevoNombre();
            
            javax.swing.tree.DefaultMutableTreeNode nodo = nodosDestinoPendientes.remove("REN_" + nombreAntiguoLimpio);
            if (nodo == null) return false;
            
            boolean esCarpeta = nodo.getUserObject().toString().startsWith("📁");
            String emoji = esCarpeta ? "📁 " : "📄 ";
            
            if (!esCarpeta) {
                // Actualizar Mapa Colores
                synchronized(lockBloques) {
                    if (mapaColoresArchivos.containsKey(nombreAntiguoLimpio)) {
                        java.awt.Color colorDelArchivo = mapaColoresArchivos.remove(nombreAntiguoLimpio);
                        mapaColoresArchivos.put(nombreNuevoLimpio, colorDelArchivo);
                    }
                }
                // Actualizar Tabla Asignación
                javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if (modeloTabla.getValueAt(i, 0) != null && modeloTabla.getValueAt(i, 0).toString().equals(nombreAntiguoLimpio)) {
                        modeloTabla.setValueAt(nombreNuevoLimpio, i, 0);
                        break;
                    }
                }
            }
            
            // Actualizar Lógica y Árbol Visual
            renombrarArchivoLogico(raizLogicaGlobal, nombreAntiguoLimpio, nombreNuevoLimpio);
            nodo.setUserObject(emoji + nombreNuevoLimpio);
            javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
            modeloArbol.nodeChanged(nodo);
            
            return true;
        }
        
        // ===============================================
        // 4. LÓGICA PARA ELIMINAR
        // ===============================================
        if (tipoOperacion.equals("ELIMINAR")) {
            String nombreLimpio = peticion.getRuta();
            int bloqueInicial = peticion.getBloqueObjetivo();
            int tamanoBloques = peticion.getTamanoEnBloques();
            
            javax.swing.tree.DefaultMutableTreeNode nodo = nodosDestinoPendientes.remove("DEL_" + nombreLimpio);
            if (nodo == null) return false;
            
            boolean esCarpeta = nodo.getUserObject().toString().startsWith("📁");
            
            if (!esCarpeta) {
                // Borrar fila de la tabla
                javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tablaAsignacion.getModel();
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if (modeloTabla.getValueAt(i, 0) != null && modeloTabla.getValueAt(i, 0).toString().equals(nombreLimpio)) {
                        modeloTabla.removeRow(i);
                        break;
                    }
                }
                
                // Liberar Disco Físico Visual
                if (bloqueInicial != -1 && tamanoBloques > 0) {
                    synchronized(lockBloques) {
                        mapaColoresArchivos.remove(nombreLimpio);
                        for (int i = 0; i < tamanoBloques; i++) {
                            int posActual = bloqueInicial + i;
                            if (posActual < coloresBloques.length) {
                                coloresBloques[posActual] = java.awt.Color.LIGHT_GRAY;
                                if (posActual != cabezalAnterior && bloquesDisco[posActual] != null) {
                                    bloquesDisco[posActual].setBackground(java.awt.Color.LIGHT_GRAY);
                                }
                            }
                        }
                    }
                }
            }
            
            // Eliminar de RAM lógica y del Árbol visual
            eliminarArchivoLogico(raizLogicaGlobal, nombreLimpio);
            javax.swing.tree.DefaultTreeModel modeloArbol = (javax.swing.tree.DefaultTreeModel) jTree1.getModel();
            modeloArbol.removeNodeFromParent(nodo);
            
            return true;
        }
        
        // Retorno por defecto para que el disco sepa que todo terminó bien aunque no pintara nada.
        return true;
    }
    
    /**
    * Busca un archivo o directorio por su nombre antiguo en el árbol lógico y lo actualiza al nuevo.
    */
    private boolean renombrarArchivoLogico(Archivo.Directorio directorioPadre, String nombreAntiguo, String nombreNuevo) {
        if (directorioPadre == null || directorioPadre.getContenido() == null) return false;
    
        // Recorremos el contenido de este directorio
        for (int i = 0; i < directorioPadre.getContenido().getTamano(); i++) {
            Archivo.EntradaSistemaArchivos entrada = directorioPadre.getContenido().get(i);
        
            // Si encontramos el que buscamos, le cambiamos el nombre
            if (entrada.getNombre().equals(nombreAntiguo)) {
                entrada.setNombre(nombreNuevo);
                return true; // Terminamos la búsqueda con éxito
            }
        
            // Si es una subcarpeta, buscamos dentro de ella recursivamente
            if (entrada instanceof Archivo.Directorio) {
                boolean renombrado = renombrarArchivoLogico((Archivo.Directorio) entrada, nombreAntiguo, nombreNuevo);
                if (renombrado) return true;
            }
        }
        return false;
    }
    
    /**
    * Pone a dormir al hilo que lo llame si el sistema está pausado.
    */
    public void verificarPausa() {
        synchronized (lockPausa) {
            while (sistemaPausado) {
                try {
                    // Aquí el hilo se duerme sin consumir nada de CPU (0%)
                    lockPausa.wait(); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTree jTree1;
    private javax.swing.JLabel lblEstadisticasCache;
    private javax.swing.JPanel panelDiscoSimulador;
    private javax.swing.JTable tablaAsignacion;
    private javax.swing.JTextArea txtAreaCache;
    // End of variables declaration//GEN-END:variables
}
