
package com.biblioteca;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class Main {
    private static final String controlador = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/bibliotecabase";
    private static final String usuario = "root";
    private static final String contraseña = "Chimaru2005";

    private static Usuario usuarioActual;

    public Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName(controlador);
            conexion = DriverManager.getConnection(url, usuario, contraseña);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return conexion;
    }

    private static Usuario buscarUsuarioEnBaseDeDatos(Main app, String documento) {
        String sql = "SELECT id, nombre FROM usuarios WHERE documento = ?";
        try (Connection conexion = app.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, documento);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                return new Usuario(nombre, documento, 0, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar usuario en la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private static void registrarUsuario(Main app, String nombre, String documento) {
        String sql = "INSERT INTO usuarios (nombre, documento) VALUES (?, ?)";
        try (Connection conexion = app.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, documento);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Usuario registrado exitosamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void listarLibros(Main app, JPanel panel) {
        String sql = "SELECT id, nombre, autor, categoria, disponible, imagen FROM libros";
        try (Connection conexion = app.conectar();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            panel.removeAll();
            panel.setLayout(new GridLayout(0, 3, 10, 10));

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String autor = rs.getString("autor");
                String categoria = rs.getString("categoria");
                boolean disponible = rs.getBoolean("disponible");
                String imagenPath = rs.getString("imagen");

                JPanel libroPanel = new JPanel();
                libroPanel.setLayout(new BorderLayout());
                libroPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                libroPanel.setBackground(disponible ? new Color(232, 255, 232) : new Color(255, 232, 232));

                // Imagen del libro
                ImageIcon imageIcon = new ImageIcon(imagenPath); // Asumiendo que la ruta de la imagen está almacenada en la base de datos
                Image image = imageIcon.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                libroPanel.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);

                // Información del libro
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new GridLayout(3, 1));
                infoPanel.setBackground(libroPanel.getBackground());
                infoPanel.add(new JLabel("Nombre: " + nombre));
                infoPanel.add(new JLabel("Autor: " + autor));
                infoPanel.add(new JLabel("Categoría: " + categoria));

                libroPanel.add(infoPanel, BorderLayout.SOUTH);
                panel.add(libroPanel);
            }
            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar los libros: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void listarLibrosPrestados(Main app, JPanel panel) {
        String sql = "SELECT l.id, l.nombre, l.autor, l.categoria, p.fecha_prestamo, l.imagen FROM libros l " +
                     "JOIN prestamos p ON l.id = p.id_libro WHERE p.id_usuario = ?";
        try (Connection conexion = app.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioActual.id);
            ResultSet rs = pstmt.executeQuery();

            panel.removeAll();
            panel.setLayout(new GridLayout(0, 3, 10, 10));

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String autor = rs.getString("autor");
                String categoria = rs.getString("categoria");
                String imagenPath = rs.getString("imagen");
                Date fechaPrestamo = rs.getDate("fecha_prestamo");

                JPanel libroPanel = new JPanel();
                libroPanel.setLayout(new BorderLayout());
                libroPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                libroPanel.setBackground(new Color(255, 232, 232));

                // Imagen del libro
                ImageIcon imageIcon = new ImageIcon(imagenPath); // Asumiendo que la ruta de la imagen está almacenada en la base de datos
                Image image = imageIcon.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH);
                libroPanel.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);

                // Información del libro
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new GridLayout(3, 1));
                infoPanel.setBackground(libroPanel.getBackground());
                infoPanel.add(new JLabel("Nombre: " + nombre));
                infoPanel.add(new JLabel("Autor: " + autor));
                infoPanel.add(new JLabel("Fecha de préstamo: " + fechaPrestamo));

                libroPanel.add(infoPanel, BorderLayout.SOUTH);
                panel.add(libroPanel);
            }
            panel.revalidate();
            panel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al listar los libros prestados: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void prestarLibro(Main app, int idLibro) {
        String sqlActualizar = "UPDATE libros SET disponible = FALSE WHERE id = ? AND disponible = TRUE";
        String sqlPrestamo = "INSERT INTO prestamos (id_usuario, id_libro) VALUES (?, ?)";
        try (Connection conexion = app.conectar();
             PreparedStatement pstmtActualizar = conexion.prepareStatement(sqlActualizar);
             PreparedStatement pstmtPrestamo = conexion.prepareStatement(sqlPrestamo)) {

            pstmtActualizar.setInt(1, idLibro);
            int filasActualizadas = pstmtActualizar.executeUpdate();

            if (filasActualizadas > 0) {
                pstmtPrestamo.setInt(1, usuarioActual.id);
                pstmtPrestamo.setInt(2, idLibro);
                pstmtPrestamo.executeUpdate();
                JOptionPane.showMessageDialog(null, "El libro ha sido prestado.");
            } else {
                JOptionPane.showMessageDialog(null, "El libro no está disponible o no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al prestar el libro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void devolverLibro(Main app, int idLibro) {
        String sqlActualizar = "UPDATE libros SET disponible = TRUE WHERE id = ? AND disponible = FALSE";
        String sqlDevolucion = "DELETE FROM prestamos WHERE id_usuario = ? AND id_libro = ?";
        try (Connection conexion = app.conectar();
             PreparedStatement pstmtActualizar = conexion.prepareStatement(sqlActualizar);
             PreparedStatement pstmtDevolucion = conexion.prepareStatement(sqlDevolucion)) {

            pstmtActualizar.setInt(1, idLibro);
            int filasActualizadas = pstmtActualizar.executeUpdate();

            if (filasActualizadas > 0) {
                pstmtDevolucion.setInt(1, usuarioActual.id);
                pstmtDevolucion.setInt(2, idLibro);
                pstmtDevolucion.executeUpdate();
                JOptionPane.showMessageDialog(null, "El libro ha sido devuelto.");
            } else {
                JOptionPane.showMessageDialog(null, "El libro no estaba prestado o no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al devolver el libro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Biblioteca");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Panel de inicio de sesión
        JPanel panelLogin = new JPanel();
        panelLogin.setBackground(new Color(255, 223, 186));
        panelLogin.setLayout(new BorderLayout());

        JPanel loginInputs = new JPanel();
        loginInputs.setBackground(panelLogin.getBackground());
        loginInputs.setLayout(new GridLayout(2, 1, 10, 10));
        loginInputs.setBorder(new EmptyBorder(30, 30, 30, 30));

        JTextField documentoField = new JTextField();
        documentoField.setText("Ingrese su documento"); // Placeholder simulado

        // Añadir Listener para simular el placeholder
        documentoField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (documentoField.getText().equals("Ingrese su documento")) {
                    documentoField.setText("");
                    documentoField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (documentoField.getText().isEmpty()) {
                    documentoField.setText("Ingrese su documento");
                    documentoField.setForeground(Color.GRAY);
                }
            }
        });

        JButton iniciarSesionButton = new JButton("Iniciar sesión");
        JButton crearUsuarioButton = new JButton("Crear usuario");

        loginInputs.add(documentoField);
        loginInputs.add(iniciarSesionButton);
        loginInputs.add(crearUsuarioButton);

        panelLogin.add(loginInputs, BorderLayout.CENTER);
        frame.add(panelLogin, BorderLayout.CENTER);

        // Panel de opciones principales (después de iniciar sesión)
        JPanel panelMain = new JPanel();
        panelMain.setBackground(new Color(230, 230, 230));
        panelMain.setLayout(new BorderLayout());

        JPanel panelActions = new JPanel();
        panelActions.setBackground(new Color(204, 255, 204));
        panelActions.setLayout(new GridLayout(2, 3, 20, 20));

        JButton listarLibrosButton = new JButton("Ver libros");
        JButton prestarLibroButton = new JButton("Pedir libro");
        JButton devolverLibroButton = new JButton("Devolver libro");
        JButton listarLibrosPrestadosButton = new JButton("Mis libros prestados");
        JButton salirButton = new JButton("Salir");

        panelActions.add(listarLibrosButton);
        panelActions.add(prestarLibroButton);
        panelActions.add(devolverLibroButton);
        panelActions.add(listarLibrosPrestadosButton);
        panelActions.add(salirButton);

        // Área para mostrar los libros
        JPanel panelLibros = new JPanel();
        JScrollPane scrollLibros = new JScrollPane(panelLibros);
        panelMain.add(scrollLibros, BorderLayout.CENTER);
        panelMain.add(panelActions, BorderLayout.SOUTH);

        // Eventos
        iniciarSesionButton.addActionListener(e -> {
            String documento = documentoField.getText();
            if (documento.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Debe ingresar un documento.");
                return;
            }

            usuarioActual = buscarUsuarioEnBaseDeDatos(new Main(), documento);
            if (usuarioActual != null) {
                JOptionPane.showMessageDialog(frame, "Bienvenido, " + usuarioActual.nombre + ".");
                frame.remove(panelLogin);
                frame.add(panelMain, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Usuario no encontrado.");
            }
        });

        crearUsuarioButton.addActionListener(e -> {
            String nombre = JOptionPane.showInputDialog(frame, "Ingrese su nombre:");
            String documento = documentoField.getText();

            if (nombre != null && !nombre.isEmpty() && !documento.isEmpty()) {
                registrarUsuario(new Main(), nombre, documento);
            } else {
                JOptionPane.showMessageDialog(frame, "Debe ingresar nombre y documento.");
            }
        });

        listarLibrosButton.addActionListener(e -> listarLibros(new Main(), panelLibros));
        listarLibrosPrestadosButton.addActionListener(e -> listarLibrosPrestados(new Main(), panelLibros));

        prestarLibroButton.addActionListener(e -> {
            String idLibroStr = JOptionPane.showInputDialog(frame, "Ingrese el ID del libro a pedir prestado:");
            if (idLibroStr != null && !idLibroStr.isEmpty()) {
                try {
                    int idLibro = Integer.parseInt(idLibroStr);
                    prestarLibro(new Main(), idLibro);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "ID de libro no válido.");
                }
            }
        });

        devolverLibroButton.addActionListener(e -> {
            String idLibroStr = JOptionPane.showInputDialog(frame, "Ingrese el ID del libro a devolver:");
            if (idLibroStr != null && !idLibroStr.isEmpty()) {
                try {
                    int idLibro = Integer.parseInt(idLibroStr);
                    devolverLibro(new Main(), idLibro);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "ID de libro no válido.");
                }
            }
        });

        salirButton.addActionListener(e -> {
            usuarioActual = null;
            JOptionPane.showMessageDialog(frame, "Sesión cerrada.");
            frame.remove(panelMain);
            frame.add(panelLogin, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        });

        frame.setVisible(true);
    }
}
