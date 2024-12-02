package com.biblioteca;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
public class App {
	private static final String controlador = "com.mysql.cj.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost:3306/bibliotecabase";
	private static final String usuario = "root";
	private static final String contraseña = "Julian1025";
	
	public Connection conectar() {
		Connection conexion = null;
    	try {
			Class.forName(controlador);
			conexion = DriverManager.getConnection(url, usuario, contraseña);
		} catch (ClassNotFoundException e) {
			System.out.println("Error: al cargar el controlador");
			e.printStackTrace();
		}catch(SQLException e) {
			System.out.println("Ocurrio un error con la base de datos");
			e.printStackTrace();
			
		}
    	return conexion;
	}
    private static Usuario usuarioActual;
    
    private static Usuario buscarUsuarioEnBaseDeDatos(App app, String documento) {
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
            System.out.println("Error al buscar usuario en la base de datos: " + e.getMessage());
        }
        return null;
    }

    private int generarNuevoIdUsuario(String nombre, String documento) {
        String verificarSql = "SELECT id FROM usuarios WHERE documento = ?";
        try (Connection conexion = this.conectar();
             PreparedStatement pstmtVerificar = conexion.prepareStatement(verificarSql)) {
            
            pstmtVerificar.setString(1, documento);
            ResultSet rs = pstmtVerificar.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            
            String sql = "INSERT INTO usuarios (nombre, documento) VALUES (?, ?)";
            try (PreparedStatement pstmtInsertar = conexion.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsertar.setString(1, nombre);
                pstmtInsertar.setString(2, documento);
                pstmtInsertar.executeUpdate();

                ResultSet rsInsertar = pstmtInsertar.getGeneratedKeys();
                if (rsInsertar.next()) {
                    return rsInsertar.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al generar un nuevo ID: " + e.getMessage());
        }
        return -1;
    }

    
    private static HashMap<String, Usuario> cargarUsuariosDesdeBaseDeDatos(App app) {
        HashMap<String, Usuario> usuarios = new HashMap<>();
        String sql = "SELECT id, nombre, documento FROM usuarios";

        try (Connection conexion = app.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String documento = rs.getString("documento");

                Usuario usuario = new Usuario(nombre, documento, 0, id);
                usuarios.put(documento, usuario);
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar usuarios desde la base de datos: " + e.getMessage());
        }

        return usuarios;
    }



    public void registrarUsuario(String nombre, String documento) {
        String verificarSql = "SELECT COUNT(*) FROM usuarios WHERE documento = ?";
        String insertarSql = "INSERT INTO usuarios (nombre, documento) VALUES (?, ?)";
        
        try (Connection conexion = this.conectar();
             PreparedStatement pstmtVerificar = conexion.prepareStatement(verificarSql);
             PreparedStatement pstmtInsertar = conexion.prepareStatement(insertarSql)) {
            
            pstmtVerificar.setString(1, documento);
            ResultSet rs = pstmtVerificar.executeQuery();
            rs.next();
            
            if (rs.getInt(1) > 0) {
                System.out.println("El usuario con el documento " + documento + " ya existe.");
                return;
            }
           
            pstmtInsertar.setString(1, nombre);
            pstmtInsertar.setString(2, documento);
            pstmtInsertar.executeUpdate();
            System.out.println("Usuario registrado exitosamente.");
            
        } catch (SQLException e) {
            System.out.println("Error al registrar el usuario: " + e.getMessage());
        }
    }



    public void listarLibros(){
        String sql = "SELECT nombre, autor, categoria, disponible FROM libros";
        try(Connection conexion = this.conectar(); 
        java.sql.Statement stmt = conexion.createStatement(); 
        ResultSet rs = stmt.executeQuery(sql) ) {
            while (rs.next()) {
                System.out.println("Nombre: " + rs.getString("nombre"));
                System.out.println("Autor: " + rs.getString("autor"));
                System.out.println("Categoría: " + rs.getString("categoria"));
                System.out.println("Disponible: " + (rs.getBoolean("disponible") ? "Sí" : "No"));
                System.out.println("----------------------------");
                
            }
            
        } catch (SQLException e) {
            System.out.println("Error al listar libros: " +e.getMessage());
        }

    }

    public void prestarLibro(int idLibro) {
        String sqlActualizar = "UPDATE libros SET disponible = FALSE WHERE id = ? AND disponible = TRUE";
        String sqlRegistrarPrestamo = "INSERT INTO prestamos (id_usuario, id_libro) VALUES (?, ?)";
        
        try (Connection conexion = this.conectar();
             PreparedStatement pstmtActualizar = conexion.prepareStatement(sqlActualizar);
             PreparedStatement pstmtPrestamo = conexion.prepareStatement(sqlRegistrarPrestamo)) {
            
            
            pstmtActualizar.setInt(1, idLibro);
            int filasActualizadas = pstmtActualizar.executeUpdate();
            
            if (filasActualizadas > 0) {
                System.out.println("El libro con ID " + idLibro + " ha sido prestado.");
                
               
                pstmtPrestamo.setInt(1, usuarioActual.id); 
                pstmtPrestamo.setInt(2, idLibro); 
                pstmtPrestamo.executeUpdate();
                
                System.out.println("El préstamo ha sido registrado.");
            } else {
                System.out.println("El libro no está disponible o no existe.");
            }
        } catch (SQLException e) {
            System.out.println("Error al prestar el libro: " + e.getMessage());
        }
    }
    public void listarLibrosPrestadosPorUsuario(int idUsuario) {
        String sql = "SELECT DISTINCT l.id, l.nombre, l.autor " +
                     "FROM libros l " +
                     "INNER JOIN prestamos p ON l.id = p.id_libro " +
                     "WHERE p.id_usuario = ? AND l.disponible = FALSE";

        try (Connection conexion = this.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Libros actualmente prestados por ti:");
            boolean hayPrestamos = false;

            while (rs.next()) {
                int idLibro = rs.getInt("id");
                String nombreLibro = rs.getString("nombre");
                String autorLibro = rs.getString("autor");

                System.out.println("ID: " + idLibro);
                System.out.println("Nombre: " + nombreLibro);
                System.out.println("Autor: " + autorLibro);
                System.out.println("----------------------------");

                hayPrestamos = true;
            }

            if (!hayPrestamos) {
                System.out.println("No tienes libros prestados actualmente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al listar los libros prestados: " + e.getMessage());
        }
    }



    
    

    public void devolverLibro(int idLibro) {
        String sqlActualizar = "UPDATE libros SET disponible = TRUE WHERE id = ? AND disponible = FALSE";
        String sqlRegistrarDevolucion = "INSERT INTO devoluciones (id_usuario, id_libro) VALUES (?, ?)";
        
        try (Connection conexion = this.conectar();
             PreparedStatement pstmtActualizar = conexion.prepareStatement(sqlActualizar);
             PreparedStatement pstmtDevolucion = conexion.prepareStatement(sqlRegistrarDevolucion)) {
            
            
            pstmtActualizar.setInt(1, idLibro);
            int filasActualizadas = pstmtActualizar.executeUpdate();
            
            if (filasActualizadas > 0) {
                System.out.println("El libro con ID " + idLibro + " ha sido devuelto.");
                
                
                pstmtDevolucion.setInt(1, usuarioActual.id);
                pstmtDevolucion.setInt(2, idLibro); 
                pstmtDevolucion.executeUpdate();
                
                System.out.println("La devolución ha sido registrada.");
            } else {
                System.out.println("El libro no estaba prestado o no existe.");
            }
        } catch (SQLException e) {
            System.out.println("Error al devolver el libro: " + e.getMessage());
        }
    }


    public void registrarPrestamo(int idUsuario, String nombreLibro) {
        String sqlPrestamo = "INSERT INTO prestamos (id_usuario, nombre_libro, fecha_prestamo) VALUES (?, ?, NOW())";
        try (Connection conexion = this.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sqlPrestamo)) {
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, nombreLibro);
            pstmt.executeUpdate();
            System.out.println("El préstamo ha sido registrado.");
        } catch (SQLException e) {
            System.out.println("Error al registrar el préstamo: " + e.getMessage());
        }
    }
    public void listarPrestamos(int idUsuario) {
        String sql = "SELECT nombre_libro, fecha_prestamo FROM prestamos WHERE id_usuario = ?";
        try (Connection conexion = this.conectar();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("Libro: " + rs.getString("nombre_libro"));
                System.out.println("Fecha de préstamo: " + rs.getDate("fecha_prestamo"));
                System.out.println("----------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error al listar los préstamos: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
    	App app = new App();
    	Connection conexion = app.conectar();
    	if (conexion != null) {
    	    System.out.println("La conexión al servidor SQL fue exitosa.");
    	} else {
    	    System.out.println("No se pudo establecer conexión con el servidor SQL.");
    	}
        Scanner scanner = new Scanner(System.in);
        ArrayList <Libro> libros = new ArrayList<>();
        libros.add(new Libro(1,"100 años de soledad", "Gabriel García Márquez", "Novela", true));
        libros.add(new Libro(2,"Don Quijote de la Mancha", "Miguel de Cervantes", "Novela", true));
        libros.add(new Libro(3,"Crimen y castigo", "Fiódor Dostoyevski", "Novela psicológica", true));
        libros.add(new Libro(4,"Orgullo y prejuicio", "Jane Austen", "Romance", true));
        libros.add(new Libro(5,"1984", "George Orwell", "Distopía", true));
        libros.add(new Libro(6,"El señor de los anillos", "J.R.R. Tolkien", "Fantasía", true));
        libros.add(new Libro(7,"Matar a un ruiseñor", "Harper Lee", "Drama", true));
        libros.add(new Libro(8,"Cumbres borrascosas", "Emily Brontë", "Gótico", true));
        libros.add(new Libro(9,"El gran Gatsby", "F. Scott Fitzgerald", "Novela", true));
        libros.add(new Libro(10,"La metamorfosis", "Franz Kafka", "Ficción", true));
        libros.add(new Libro(11,"Harry Potter y la piedra filosofal", "J.K. Rowling", "Fantasía", true));
        boolean usuarioAccedido = false;

        int opciones = 0;

        HashMap<String, Usuario> usuarios = cargarUsuariosDesdeBaseDeDatos(app);
        usuarios.put("192428", new Usuario("Admin", "192428", 0,1));
        while (opciones != 4 && !usuarioAccedido ) {
            System.out.println("Bienvenido");
            System.out.println("Opciones:");
            System.out.println("(1) Listado de libros");
            System.out.println("(2) Crear un nuevo usuario");
            System.out.println("(3) Iniciar sesion");
            System.out.println("(4) Salir");
    
    
            opciones = scanner.nextInt();
            scanner.nextLine();
    
            switch (opciones) {
                case 1:
                System.out.println("Libros disponibles: ");
                        for (Libro libro : libros) {
                System.out.println("╔════════════════════════════════════════════════════╗");
                System.out.printf("║ %-50s ║\n", "Nombre: " + libro.nombre);
                System.out.printf("║ %-50s ║\n", "Autor: " + libro.autor);
                System.out.printf("║ %-50s ║\n", "Categoría: " + libro.categoria);
                System.out.printf("║ %-50s ║\n", "Disponible: " + (libro.disponible ? "Sí" : "No"));
                System.out.println("╚════════════════════════════════════════════════════╝");
                System.out.println("<-------------------------------------------------------->");
            }
            break;
    
                case 2:
                    System.out.println("Ingrese su documento para registrar un nuevo usuario:");
                    String nuevoDocumentoRegistro = scanner.nextLine();

                    if (usuarios.containsKey(nuevoDocumentoRegistro)) {
                        System.out.println("El usuario ya existe. Por favor, inicie sesión.");
                    } else {
                        // Buscar en la base de datos
                        Usuario usuarioDesdeDB = buscarUsuarioEnBaseDeDatos(app, nuevoDocumentoRegistro);
                        if (usuarioDesdeDB != null) {
                            System.out.println("El usuario ya existe en la base de datos. Por favor, inicie sesión.");
                        } else {
                            // Registrar un nuevo usuario
                            System.out.println("Ingrese su nombre:");
                            String nuevoNombre = scanner.nextLine();

                            app.registrarUsuario(nuevoNombre, nuevoDocumentoRegistro);

                            // Actualizar la lista de usuarios en memoria
                            Usuario nuevoUsuario = new Usuario(nuevoNombre, nuevoDocumentoRegistro, 0, app.generarNuevoIdUsuario(nuevoNombre, nuevoDocumentoRegistro));
                            usuarios.put(nuevoDocumentoRegistro, nuevoUsuario);

                            System.out.println("¡Usuario registrado exitosamente! Ahora puede iniciar sesión.");
                        }
                    }
                    break;

    
            case 3:
            System.out.println("Ingrese su documento: ");
            String nuevoDocumento = scanner.nextLine(); // Renombrar variable para evitar conflicto
        
            if (usuarios.containsKey(nuevoDocumento)) {
                usuarioActual = usuarios.get(nuevoDocumento);
                System.out.println("¡Bienvenido, " + usuarioActual.nombre + "!");
                usuarioAccedido = true;
            } else {
                // Buscar en la base de datos
                Usuario usuarioDesdeDB = buscarUsuarioEnBaseDeDatos(app, nuevoDocumento);
                if (usuarioDesdeDB != null) {
                    usuarios.put(nuevoDocumento, usuarioDesdeDB);
                    usuarioActual = usuarioDesdeDB;
                    System.out.println("¡Bienvenido, " + usuarioActual.nombre + "!");
                    usuarioAccedido = true;
                } else {
                    System.out.println("Error: usuario no encontrado.");
                }
            }
    
            break;
    
            case 4:
            System.out.println("Saliendo del sistema...");
            
            break;
    
                default:
                try {
                    throw new IllegalArgumentException("Opcion no valida, por favor elija una opcion valida.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
                    break;
            }
            
        }
        int opcionAcceso = 0;

        while (opcionAcceso != 4 && usuarioAccedido == true) {
            System.out.println("Opciones:");
            System.out.println("(1) Listado de libros.");
            System.out.println("(2) Sacar libro.");
            System.out.println("(3) Devolver libro.");
            System.out.println("(4) Salir");

            int opcionUsuario = scanner.nextInt();

            switch (opcionUsuario) {
                case 1:
                System.out.println("Libros disponibles: ");
                for (Libro libro : libros) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ %-50s ║\n", "Nombre: " + libro.nombre);
        System.out.printf("║ %-50s ║\n", "Autor: " + libro.autor);
        System.out.printf("║ %-50s ║\n", "Categoría: " + libro.categoria);
        System.out.printf("║ %-50s ║\n", "Disponible: " + (libro.disponible ? "Sí" : "No"));
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        System.out.println("<--------------------------------------------------------------------------->");
                }
                    
                    break;

                case 2:
                    System.out.println("Libros disponibles para préstamo: ");
                    int index = 1;
                    ArrayList<Libro> librosDisponibles = new ArrayList<>();
                    for (Libro libro : libros) {
                        if (libro.disponible) {
                            librosDisponibles.add(libro);
                            System.out.println(index + ". " + libro.nombre + " - " + libro.autor);
                            index++;
                        }
                    }
                    
                    if (librosDisponibles.isEmpty()) {
                        System.out.println("No hay libros disponibles en este momento.");
                        break;
                    }

                    System.out.println("Ingrese el número del libro que desea sacar");
                    int seleccion = scanner.nextInt();
                    scanner.nextLine();

                    if (seleccion < 1 || seleccion > librosDisponibles.size()) {
                        System.out.println("Selección inválida, intente de nuevo.");
                        break;
                    }

                    Libro libroSeleccionado = librosDisponibles.get(seleccion - 1);
                    app.prestarLibro(libroSeleccionado.idlibro);
                    libroSeleccionado.cambiarDisponibilidad(false);
                    break;

                case 3:
                	System.out.println("Libros en tu posesión:");

                	app.listarLibrosPrestadosPorUsuario(usuarioActual.id);

                	System.out.println("Ingresa el ID del libro que deseas devolver:");
                	int idLibroDevolver = scanner.nextInt();
                	scanner.nextLine();

               
                	app.devolverLibro(idLibroDevolver);
                    break;
                case 4:
                System.out.println("Saliendo del sistema....");
                opcionAcceso = 4;
                break;
            
                default:
                    break;
            }
            
        }


scanner.close();

    }
}