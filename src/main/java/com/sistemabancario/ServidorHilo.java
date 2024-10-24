package com.sistemabancario;

import Controlador.Conexion;
import Modelo.Cliente;
import Modelo.Cuenta;
import MySQL.MySQLCliente;
import MySQL.MySQLCuenta;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServidorHilo extends Thread {
    
    private DataInputStream in;
    private DataOutputStream out;
    private String[] nombreCliente;
    private Socket socket;
    
    public ServidorHilo(Socket socket, DataInputStream in, DataOutputStream out, String[] nombreCliente) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.nombreCliente = nombreCliente;
    }
    
 
    public void Transferencia() {
//                         out.writeUTF("Transferencia,"+idCuenta+","+montoTrnasferencia+","+idDestino);
        try {
            String cuentasString = in.readUTF();
            
            String[] nombreCliente = cuentasString.split(",");            
            System.out.println(nombreCliente[0] + nombreCliente[3]);
            float montoTransferencia = Float.parseFloat(nombreCliente[2]);
            float fondosCuenta = consultarCuenta(Integer.parseInt(nombreCliente[1]));
            Cliente cliente = traerCuenta(Integer.parseInt(nombreCliente[1]));
            if (fondosCuenta >= montoTransferencia) {
                // Procesar la transferencia

                // Actualizar saldo del destinatario
                Cliente transfiereA = traerCuenta(Integer.parseInt(nombreCliente[3]));
                
                enviarTransferencia(transfiereA, montoTransferencia);
                fondosCuenta -= montoTransferencia; // Actualizar saldo
                actualizarSaldo(cliente, fondosCuenta);
                out.writeUTF("Descontado," + montoTransferencia + "," + transfiereA.getNombre_Cliente() + "," + fondosCuenta);

                // Confirmar transferencia
            } else {
                // out.writeUTF("Saldo insuficiente para la transferencia. Saldo actual: " + fondosCuenta);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public String VerificarCuenta(int idCuenta) {
        Cliente cliente = traerCuenta(idCuenta);
        if (cliente != null) {
            return cliente.getNombre_Cliente();
        }
        return cliente.getNombre_Cliente();
    }
    
    public void RetiroSaldo() {
        try {
            String cuentasString = in.readUTF();
            
            String[] nombreCliente = cuentasString.split(",");
            Cliente cliente = traerCuenta(Integer.parseInt(nombreCliente[1]));
            // Obtener el saldo inicial de la cuenta
            float fondosCuenta = consultarCuenta(cliente.getId_Cliente());
            
            float montoRetiro = Float.parseFloat(nombreCliente[2]);
            
            if (fondosCuenta >= montoRetiro) {
                fondosCuenta -= montoRetiro; // Actualiza el saldo 
                actualizarSaldo(cliente, fondosCuenta);
                out.writeUTF("Descontado," + fondosCuenta + "," + cliente.getNombre_Cliente());
                
            }
        } catch (IOException ex) {
            System.out.println(ex);
            
        }
    }

    public void Cajero() {
        try {
            String cuentasString = in.readUTF();
            
            String[] nombreCliente = cuentasString.split(",");
            if (nombreCliente[0].equals("Deposito")) {                
                Cliente cliente = traerCuenta(Integer.parseInt(nombreCliente[1]));
                float fondosCuenta = consultarCuenta(cliente.getId_Cliente());
                float montoDeposito = Float.parseFloat(nombreCliente[2]);
                fondosCuenta += montoDeposito; // Actualiza el saldo 
                actualizarSaldo(cliente, fondosCuenta);
                out.writeUTF(Float.toString(fondosCuenta));
            } else if (nombreCliente[0].equals("Retiro")) {
                RetiroSaldo();
            } else {
                Transferencia();
            }
            
        } catch (IOException ex) {
            System.out.println(ex);            
        }
    }
    
    @Override
    public void run() {
        try {
            
            String cuentasString = nombreCliente[1];
            String idcuentaExistente = VerificarCuenta(Integer.parseInt(nombreCliente[1]));
            out.writeUTF(idcuentaExistente);
            switch (nombreCliente[0]) {
                case "HomeBanking":
                    Transferencia();
                    break;
                case "PostNet":
                    RetiroSaldo();
                    break;
                case "CajeroAutomatico":
                    Cajero();
                    break;
                default:
                    System.out.println("");
                    break;
            }
            
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public float consultarCuenta(int id) {
        Conexion c = new Conexion();
        MySQLCuenta msq = new MySQLCuenta(c.conectar());
        Cuenta cuenta = msq.obtener(id);
        System.out.println("Saldo actual de la cuenta: " + cuenta.getSaldo());
        return cuenta.getSaldo();
    }
    
    public void actualizarSaldo(Cliente cliente, float retiro) {
        Conexion c = new Conexion();
        MySQLCuenta msq = new MySQLCuenta(c.conectar());
        Cuenta cuenta = msq.obtener(cliente.getId_Cliente());
        cuenta.setId_Cuenta(cuenta.getId_Cuenta());
        cuenta.setId_Cliente(cuenta.getId_Cliente());
        cuenta.setTipo(cuenta.getTipo());
        cuenta.setFecha_Creacion("123");
        cuenta.setSaldo(retiro);
        System.out.println(retiro);
        System.out.println(cuenta.getId_Cliente() + cuenta.getSaldo() + cuenta.getId_Cliente());
        msq.modificar(cuenta);
    }
    
    public void enviarTransferencia(Cliente cliente, float ingreso) {
        Conexion c = new Conexion();
        MySQLCuenta msq = new MySQLCuenta(c.conectar());
        Cuenta cuenta = msq.obtener(cliente.getId_Cliente());
        cuenta.setId_Cuenta(cuenta.getId_Cuenta());
        cuenta.setId_Cliente(cuenta.getId_Cliente());
        cuenta.setTipo(cuenta.getTipo());
        cuenta.setFecha_Creacion("123");
        cuenta.setSaldo(ingreso + cuenta.getSaldo());
        System.out.println(ingreso);
        System.out.println(cuenta.getId_Cliente() + cuenta.getSaldo() + cuenta.getId_Cliente());
        msq.modificar(cuenta);
    }
    
    public Cliente traerCuenta(int id) {
        Conexion c = new Conexion();
        MySQLCliente msq = new MySQLCliente(c.conectar());
        Cliente cliente = msq.obtener(id);
        if (cliente == null) {
            return null;
        }
        return cliente;
    }
}
