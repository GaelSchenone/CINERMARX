package CINEMARX.M1;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Clase auxiliar para operaciones de base de datos del módulo M1
 * Gestiona el registro, login y actualización de datos de clientes
 */
public class DatabaseHelper {
    
    private Connection connection;
    
    // Constructor que recibe la conexión
    public DatabaseHelper(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Registra un nuevo cliente en la base de datos
     * @return true si el registro fue exitoso, false si el correo ya existe
     */
    public boolean registrarCliente(String nombre, String apellido, String correo, String contrasena, int DNI, Date FechaNacimiento) {
        // Validar que el correo no exista
        if (existeCorreo(correo)) {
            return false;
        }
        
        // Validar que el DNI no exista
        if (existeDNI(DNI)) {
            return false;
        }
        
        try {
            connection.setAutoCommit(false);
            
            // 1. Crear el usuario en la tabla Usuario
            String sqlUsuario = "INSERT INTO Usuario (DNI, FechaNac, Nombre, Apellido) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUsuario)) {
                pstmt.setInt(1, DNI);
                pstmt.setDate(2, FechaNacimiento);
                pstmt.setString(3, nombre);
                pstmt.setString(4, apellido); 
                pstmt.executeUpdate();
            }
            
            // 2. Crear el cliente vinculado al usuario CON 0 PUNTOS
            String sqlCliente = "INSERT INTO Cliente (DNI, Membresia, Mail, Contrasena, Puntos, PuntosGastados) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlCliente)) {
                pstmt.setInt(1, DNI);
                pstmt.setString(2, "NO VIP"); // Membresía por defecto
                pstmt.setString(3, correo);
                pstmt.setString(4, contrasena);
                pstmt.setInt(5, 0); // INICIAR CON 0 PUNTOS
                pstmt.setInt(6, 0); // INICIAR CON 0 PUNTOS GASTADOS
                pstmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si un correo ya está registrado
     */
    private boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Verifica si un DNI ya está registrado
     */
    private boolean existeDNI(int DNI) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE DNI = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, DNI);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Valida las credenciales de inicio de sesión
     * @return UsuarioCliente si las credenciales son correctas, null si no
     */
    public UsuarioCliente validarCredenciales(String correo, String contrasena) {
        String sql = "SELECT c.ID_Cliente, c.DNI, c.Membresia, c.Mail, c.Contrasena, u.Nombre, u.Apellido, u.FechaNac " +
                     "FROM Cliente c " +
                     "INNER JOIN Usuario u ON c.DNI = u.DNI " +
                     "WHERE c.Mail = ? AND c.Contrasena = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");
                    int dni = rs.getInt("DNI");
                    Date fechaNac = rs.getDate("FechaNac");
                    
                    // Crear objeto UsuarioCliente con los datos de la BD
                    UsuarioCliente usuario = new UsuarioCliente(
                        nombre,
                        apellido,
                        correo,
                        contrasena,
                        dni,
                        fechaNac
                    );
                    
                    return usuario;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Actualiza los datos de un cliente (versión completa y corregida)
     */
    public boolean actualizarDatosCliente(String correoActual, String nuevoNombre, String nuevoApellido,
                                         String nuevoCorreo, String nuevaContrasena, int nuevoDNI, Date nuevaFechaNac) {
        try {
            connection.setAutoCommit(false);
            
            // Obtener el DNI actual del cliente
            int dniActual = -1;
            String sqlGetDNI = "SELECT DNI FROM Cliente WHERE Mail = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlGetDNI)) {
                pstmt.setString(1, correoActual);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        dniActual = rs.getInt("DNI");
                    } else {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        return false;
                    }
                }
            }
            
            // Si el DNI cambió, verificar que el nuevo no exista (solo si es diferente al actual)
            if (nuevoDNI != dniActual && existeDNI(nuevoDNI)) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Si el correo cambió, verificar que el nuevo no exista (solo si es diferente al actual)
            if (!nuevoCorreo.equals(correoActual) && existeCorreo(nuevoCorreo)) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Actualizar Cliente primero (mail, contraseña - SIN cambiar DNI aún)
            String sqlCliente = "UPDATE Cliente SET Mail = ?, Contrasena = ? WHERE DNI = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlCliente)) {
                pstmt.setString(1, nuevoCorreo);
                pstmt.setString(2, nuevaContrasena);
                pstmt.setInt(3, dniActual);
                pstmt.executeUpdate();
            }
            
            // Actualizar Usuario (nombre, apellido y fecha de nacimiento - SIN cambiar DNI aún)
            String sqlUsuario = "UPDATE Usuario SET Nombre = ?, Apellido = ?, FechaNac = ? WHERE DNI = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUsuario)) {
                pstmt.setString(1, nuevoNombre);
                pstmt.setString(2, nuevoApellido);
                pstmt.setDate(3, nuevaFechaNac);
                pstmt.setInt(4, dniActual);
                pstmt.executeUpdate();
            }
            
            // Si el DNI cambió, actualizarlo AL FINAL en ambas tablas
            if (nuevoDNI != dniActual) {
                // Actualizar DNI en Usuario
                String sqlUpdateDNIUsuario = "UPDATE Usuario SET DNI = ? WHERE DNI = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdateDNIUsuario)) {
                    pstmt.setInt(1, nuevoDNI);
                    pstmt.setInt(2, dniActual);
                    pstmt.executeUpdate();
                }
                
                // Actualizar DNI en Cliente
                String sqlUpdateDNICliente = "UPDATE Cliente SET DNI = ? WHERE DNI = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdateDNICliente)) {
                    pstmt.setInt(1, nuevoDNI);
                    pstmt.setInt(2, dniActual);
                    pstmt.executeUpdate();
                }
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene los puntos acumulados de un cliente desde la base de datos
     */
    public int obtenerPuntosCliente(String correo) {
        String sql = "SELECT Puntos FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Puntos");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Obtiene el ID del cliente a partir de su correo
     */
    public int obtenerIdCliente(String correo) {
        String sql = "SELECT ID_Cliente FROM Cliente WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_Cliente");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Suma 200 puntos al cliente por una compra realizada
     */
    public boolean sumarPuntosPorCompra(String correo) {
        String sql = "UPDATE Cliente SET Puntos = Puntos + 200 WHERE Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Registra un canje de puntos y actualiza los puntos del cliente
     */
    public boolean registrarCanje(String correo, String producto, int puntos) {
        try {
            connection.setAutoCommit(false);
            
            // Obtener ID del cliente
            int idCliente = obtenerIdCliente(correo);
            if (idCliente == -1) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Verificar que tenga suficientes puntos
            int puntosActuales = obtenerPuntosCliente(correo);
            if (puntosActuales < puntos) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Restar puntos y sumar a PuntosGastados
            String sqlUpdate = "UPDATE Cliente SET Puntos = Puntos - ?, PuntosGastados = PuntosGastados + ? WHERE Mail = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {
                pstmt.setInt(1, puntos);
                pstmt.setInt(2, puntos);
                pstmt.setString(3, correo);
                pstmt.executeUpdate();
            }
            
            // Registrar el canje en la tabla Canje
            String sqlInsert = "INSERT INTO Canje (ID_Cliente, Producto, Puntos) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
                pstmt.setInt(1, idCliente);
                pstmt.setString(2, producto);
                pstmt.setInt(3, puntos);
                pstmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene el historial de canjes de un cliente desde la base de datos
     */
    public ResultSet obtenerHistorialCanjes(String correo) {
        String sql = "SELECT c.FechaCanje, c.Producto, c.Puntos " +
                     "FROM Canje c " +
                     "INNER JOIN Cliente cl ON c.ID_Cliente = cl.ID_Cliente " +
                     "WHERE cl.Mail = ? " +
                     "ORDER BY c.FechaCanje DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, correo);
            return pstmt.executeQuery();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene el historial de compras de un cliente (BOLETOS + PRODUCTOS)
     * Retorna un ResultSet con: Fecha, Descripcion, Precio, Tipo
     */
    public ResultSet obtenerHistorialCompras(String correo) {
        String sql = 
            "SELECT " +
            "    comp.FechaCompra as Fecha, " +
            "    CONCAT(cb.Cantidad, 'x Entrada ', p.Titulo, ' - ', s.TipoDeSala) as Descripcion, " +
            "    (f.Precio * cb.Cantidad) as Precio, " +
            "    'BOLETO' as Tipo " +
            "FROM Comprobante comp " +
            "INNER JOIN Cliente c ON comp.ID_Cliente = c.ID_Cliente " +
            "INNER JOIN Comprobante_Boleto cb ON comp.ID_Comprobante = cb.ID_Comprobante " +
            "INNER JOIN Boleto b ON cb.ID_Boleto = b.ID_Boleto " +
            "INNER JOIN Funcion f ON b.ID_Funcion = f.ID_Funcion " +
            "INNER JOIN Pelicula p ON f.ID_Pelicula = p.ID_Pelicula " +
            "INNER JOIN Sala s ON f.ID_Sala = s.ID_Sala " +
            "WHERE c.Mail = ? " +
            "UNION ALL " +
            "SELECT " +
            "    comp.FechaCompra as Fecha, " +
            "    CONCAT(cp.Cantidad, 'x ', prod.Nombre) as Descripcion, " +
            "    (prod.Precio * cp.Cantidad) as Precio, " +
            "    'PRODUCTO' as Tipo " +
            "FROM Comprobante comp " +
            "INNER JOIN Cliente c ON comp.ID_Cliente = c.ID_Cliente " +
            "INNER JOIN Comprobante_Producto cp ON comp.ID_Comprobante = cp.ID_Comprobante " +
            "INNER JOIN Producto prod ON cp.ID_Prod = prod.ID_Prod " +
            "WHERE c.Mail = ? " +
            "ORDER BY Fecha DESC";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, correo);
            pstmt.setString(2, correo);
            return pstmt.executeQuery();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene los datos personales del usuario (nombre y apellido de la tabla Usuario)
     */
    public String[] obtenerDatosPersonales(String correo) {
        String sql = "SELECT u.Nombre, u.Apellido FROM Usuario u " +
                     "INNER JOIN Cliente c ON u.DNI = c.DNI " +
                     "WHERE c.Mail = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("Nombre");
                    String apellido = rs.getString("Apellido");
                    return new String[]{nombre, apellido};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new String[]{"", ""};
    }
}