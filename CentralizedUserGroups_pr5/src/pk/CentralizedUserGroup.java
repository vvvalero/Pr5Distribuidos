/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import static java.rmi.registry.LocateRegistry.createRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author albert0
 */
public class CentralizedUserGroup{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Registry reg = null;
            // Intentar iniciar el registro RMI en el puerto 1099
            try {
                reg = LocateRegistry.createRegistry(1099);
                System.out.println("Registro RMI creado en el puerto 1099.");

            
            // Crear una instancia del servidor
            GroupServer server = new GroupServer();
            //System.setProperty("java.rmi.server.hostname","192.168.1.111");
            // Obtener el registro RMI
            //Registry registry = LocateRegistry.getRegistry();

            // Registrar el servidor en el registro RMI con el nombre "GroupServer"
            //registry.rebind("GroupServer", server);
            //Naming.rebind("GroupServer", server);
            reg.rebind("GroupServer", server);
            System.out.println(reg.toString());

            System.out.println("Servidor registrado y listo para recibir solicitudes.");
        } catch (RemoteException e) {
                System.out.println("El registro RMI ya estaba en ejecución.");
            }
        } catch (Exception e) {
            System.err.println("Excepción general: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}

