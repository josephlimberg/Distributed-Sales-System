import java.sql.*;
import java.util.Scanner;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;

public class ClienteVenta {
    // En producción, esto se lee de un archivo application.properties
    private static final String SERVER_URL = System.getenv().getOrDefault("SERVER_URL", "http://DIRECCION_NUBE:5000");
    private static final String DB_LOCAL_URL = System.getenv().getOrDefault("DB_LOCAL_URL", "jdbc:mysql://localhost:3306/grupoJoseph_local");
    private static final String DB_LOCAL_USER = System.getenv().getOrDefault("DB_LOCAL_USER", "usuario_local");
    private static final String DB_LOCAL_PASS = System.getenv().getOrDefault("DB_LOCAL_PASS", "password_local");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- NUEVA VENTA ---");
        System.out.print("Ingrese ID Tienda: ");
        int idTienda = scanner.nextInt();
        System.out.print("Ingrese Total: ");
        double total = scanner.nextDouble();
        System.out.print("Ingrese ID Cliente (DNI): ");
        String idCliente = scanner.next();
        System.out.print("Ingrese Teléfono Celular: ");
        String telefono = scanner.next();

        // 1. Solicitar voucher a la Nube
        JSONObject ventaData = new JSONObject();
        ventaData.put("id_tienda", idTienda);
        ventaData.put("total", total);
        ventaData.put("id_cliente", idCliente);
        ventaData.put("telefono_cliente", telefono);

        System.out.println("\nConectando con el servidor central...");
        JSONObject respuesta = postRequest(SERVER_URL + "/generar_voucher", ventaData);
        
        // Manejo de escenario de fallo: Pérdida de conexión con la nube
        if (respuesta.has("conexion_error")) {
            System.err.println("ERROR CRÍTICO: No se pudo conectar con la base de datos en la nube.");
            System.err.println("Por favor, verifique su conexión a internet e intente más tarde.");
            return;
        }
        
        if (respuesta.has("error")) {
            System.err.println("Error en el servidor: " + respuesta.getString("error"));
            return;
        }

        int idVenta = respuesta.getInt("id_venta");
        System.out.println("Voucher generado. SMS enviado al cliente.");

        // 2. Bucle de Validación de Token (Máximo 3 intentos)
        boolean tokenValido = false;
        int intentos = 0;
        
        while (intentos < 3 && !tokenValido) {
            System.out.print("\nIngrese token de 6 dígitos recibido por SMS (Intento " + (intentos + 1) + "/3): ");
            String token = scanner.next();
            
            JSONObject validacionData = new JSONObject();
            validacionData.put("id_venta", idVenta);
            validacionData.put("token", token);

            JSONObject validacion = postRequest(SERVER_URL + "/validar_token", validacionData);
            
            if (validacion.has("valido") && validacion.getBoolean("valido")) {
                tokenValido = true;
                System.out.println("Token validado exitosamente.");
            } else {
                System.err.println("Error: " + validacion.optString("error", "Error desconocido"));
                intentos++;
            }
        }

        if (!tokenValido) {
            System.err.println("\nOperación cancelada: Se superó el límite de intentos o el token expiró.");
            return;
        }

        // 3. Registrar detalles en MySQL local de la tienda
        registrarDetalleVenta(idVenta, total);
    }

    private static JSONObject postRequest(String url, JSONObject data) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(data.toString()));
            
            HttpResponse response = client.execute(post);
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        } catch (HttpHostConnectException e) {
            // Captura específica si el servidor Python está caído
            return new JSONObject().put("conexion_error", true);
        } catch (Exception e) {
            return new JSONObject().put("error", e.getMessage());
        }
    }

    private static void registrarDetalleVenta(int idVenta, double total) {
        // En una app real, los productos vendrían de un carrito de compras. Aquí simulamos uno.
        System.out.println("\nGuardando detalles en base de datos local...");
        try (Connection conn = DriverManager.getConnection(DB_LOCAL_URL, DB_LOCAL_USER, DB_LOCAL_PASS)) {
            
            String sql = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Simulación de guardado de un producto
            stmt.setInt(1, idVenta);
            stmt.setInt(2, 999);      // ID Producto simulado
            stmt.setInt(3, 1);        // Cantidad
            stmt.setDouble(4, total); // Precio
            stmt.executeUpdate();
            
            System.out.println("VENTA COMPLETADA Y REGISTRADA EXITOSAMENTE");
        } catch (SQLException e) {
            System.err.println("Error guardando en la BD local: " + e.getMessage());
        }
    }
}