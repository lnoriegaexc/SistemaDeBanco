
package Controlador;
import java.sql.*;
public class Conexion {
    private Connection conn=null;
    private final String driver="com.mysql.cj.jdbc.Driver";
    private final String database="banco";
    private final String hostname="localhost";
    private final String port="3308";
    private final String username="root";
    private final String password="root";
    private final String url="jdbc:mysql://"+hostname+":"+port+"/"+database+"?useSSL=false";
    
    public Connection conectar()
    {
        try{
            Class.forName(driver);
            conn=DriverManager.getConnection(url,username,password);
            System.out.println("Conexion establecida");
        }
        catch(ClassNotFoundException|SQLException e){
            e.printStackTrace();
            System.out.println("No se pudo establecer la conexión.");
        }
        return conn;
    }
    
    public void cerrar(){
        try{
            conn.close();
        }catch(SQLException e){
               e.printStackTrace();
        }
    }
    
}
