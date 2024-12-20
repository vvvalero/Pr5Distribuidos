/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import static java.lang.System.in;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Proxy;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author valen,alber y el karlorzas
 */
public class Client extends UnicastRemoteObject implements IClient{
    private static Object logger;
    private static LinkedList<GroupMessage> laCola = new LinkedList<>();
    private static GroupServerInterface gp;
    private static String alias=null;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition nuevoMensaje = lock.newCondition();
    
    public Client()throws RemoteException{
        
    }
    
    @Override
    public void DepositMessage(GroupMessage m)throws RemoteException{
        lock.lock();
        laCola.push(m);
        nuevoMensaje.signalAll();
        
        lock.unlock();
    }
    
    private static byte[] receiveGroupMessage(String galias) throws RemoteException{
        lock.lock();
        try {
            byte[] mensaje = null;
            if (gp.isMember(galias, alias)){
                // Devolvemos y eliminamos el primer mensaje del grupo pedido.
                int i=0;
                for(i=0;i<laCola.size() && mensaje==null;i++){
                    System.out.println("Recibiendo mensaje");
                    if(laCola.get(i).nombreGrupo.equals(galias)){
                        mensaje = laCola.get(i).mensaje;        // Seria mejor una cola por grupo
                        laCola.remove(i);
                    }
                }
                while(mensaje==null){
                    try {
                        System.out.println("Esperando al mensaje");
                        nuevoMensaje.await();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    for(i=0;i<laCola.size() && mensaje==null;i++){
                        if(laCola.get(i).nombreGrupo.equals(galias)){
                            mensaje = laCola.get(i).mensaje;
                            laCola.remove(i);
                        }
                    }
                }
            }
            return mensaje;
        } finally {
            lock.unlock();
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException, NotBoundException {
        Client cliente = new Client();
        String hostname_servidor = "localhost"; 
        String nombreRegistroServidor = "GroupServer"; 
        String ruta = "C:/Users/valen/OneDrive/Documentos/NetBeansProjects/SecurityPolicies"; 
        int puertecitoCliente ;
        
        Scanner sc = new Scanner(System.in);
        int menu = 1;
        String hostname_cliente = null;
        try {
            hostname_cliente = InetAddress.getLocalHost().getHostName();
            
        } catch (UnknownHostException ex) {
            System.out.println("OH NOOOOOOOOO!");
        }

        System.setProperty("java.security.policy", ruta);
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.out.println("Inicializado cliente");
        System.out.println("Escribe un alias:");
        alias = sc.nextLine();
        
        System.out.println("Introduce el puerto para el cliente:");
        puertecitoCliente = sc.nextInt();  //Intrducimos nosotros el puerto
        
        Registry reg = LocateRegistry.getRegistry(hostname_servidor);
        try {
            gp = (GroupServerInterface) reg.lookup(nombreRegistroServidor);

            // Exportar el cliente como un objeto remoto
            //IClient stub = (IClient) UnicastRemoteObject.exportObject(cliente, puertecitoCliente); 
            Registry registry = LocateRegistry.createRegistry(puertecitoCliente); // Puerto para el cliente
            registry.rebind("CLIENTASO", cliente); 

            while (menu > 0 && menu < 14) {
                System.out.println("Menu");
                System.out.println("    1.  Crear grupo");
                System.out.println("    2.  Eliminar grupo");
                System.out.println("    3.  Añadir cliente a grupo");
                System.out.println("    4.  Eliminar cliente de grupo");
                System.out.println("    5.  Bloquear altas al grupo");
                System.out.println("    6.  Desbloquear altas al grupo");
                System.out.println("    7.  Mostrar miembros del grupo");
                System.out.println("    8.  Mostrar grupos");
                System.out.println("    9.  Comprobar si existe un grupo");
                System.out.println("    10. Ver propietario de un grupo");
                System.out.println("    11. Comprobar si se es miembro de un grupo");
                System.out.println("    12. Enviar mensaje a todos los miembros del grupo");
                System.out.println("    13. Recibir mensaje");
                System.out.println("    0.  Terminar");

                System.out.println("Introduce el numero de una de las opciones. Introduce otro para salir");
                menu = sc.nextInt();

                switch (menu) {
                    case 1: // Crear grupo
                        System.out.println("Introduce el nombre del grupo:");
                        String nombreGrupo = sc.next();
                        boolean result = gp.createGroup(nombreGrupo, alias, hostname_cliente, puertecitoCliente);
                        if (result) {
                            System.out.println("Grupo " + nombreGrupo + " creado con éxito.");
                        } else {
                            System.out.println("El grupo no pudo ser creado.");
                        }
                        break;

                    case 2: // Eliminar grupo
                        System.out.println("Introduce el nombre del grupo a eliminar:");
                        nombreGrupo = sc.next();
                        result = gp.removeGroup(nombreGrupo,alias);
                        if (result) {
                            System.out.println("Grupo " + nombreGrupo + " eliminado con éxito.");
                        } else {
                            System.out.println("No se pudo eliminar el grupo.");
                        }
                        break;

                    case 3: // Añadir cliente a grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        result = gp.addMember(nombreGrupo, alias, hostname_cliente, puertecitoCliente);
                        if (result) {
                            System.out.println("Miembro añadido al grupo " + nombreGrupo + " con éxito.");
                        } else {
                            System.out.println("No se pudo añadir el miembro.");
                        }
                        break;

                    case 4: // Eliminar cliente de grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        result = gp.removeMember(nombreGrupo, alias);
                        if (result) {
                            System.out.println("Miembro eliminado del grupo " + nombreGrupo + " con éxito.");
                        } else {
                            System.out.println("No se pudo eliminar el miembro.");
                        }
                        break;

                    case 5: // Bloquear altas al grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        result = gp.StopMembers(nombreGrupo);
                        if (result) {
                            System.out.println("Altas bloqueadas para el grupo " + nombreGrupo + " con éxito.");
                        } else {
                            System.out.println("No se pudo bloquear las altas.");
                        }
                        break;

                    case 6: // Desbloquear altas al grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        result = gp.AllowMembers(nombreGrupo);
                        if (result) {
                            System.out.println("Altas desbloqueadas para el grupo " + nombreGrupo + " con éxito.");
                        } else {
                            System.out.println("No se pudo desbloquear las altas.");
                        }
                        break;

                    case 7: // Mostrar miembros del grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        LinkedList<String> members = gp.ListMembers(nombreGrupo);
                        System.out.println("Miembros del grupo " + nombreGrupo + ": ");
                        for (String member : members) {
                            System.out.println(member);
                        }
                        break;

                    case 8: 
                        LinkedList<String> groups = gp.ListGroups();
                        System.out.println("Grupos actuales: ");
                        for (String group : groups) {
                            System.out.println(group);
                        }
                        break;

                    case 9: // Comprobar si existe un grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        result = gp.isGroup(nombreGrupo);
                        System.out.println(result ? "El grupo existe." : "El grupo no existe.");
                        break;

                    case 10: // Ver propietario de un grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        String owner = gp.Owner(nombreGrupo);
                        System.out.println("Propietario del grupo " + nombreGrupo + ": " + owner);
                        break;

                    case 11: // Comprobar si es miembro de un grupo
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        try {
                            boolean isMember = gp.isMember(nombreGrupo, alias);
                            System.out.println(isMember ? "Eres miembro del grupo." : "No eres miembro del grupo.");
                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    
                    case 12: // Enviar mensaje
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        sc.nextLine(); // Limpiar el buffer después de nextInt o next()
                        try {
                            if (gp.isGroup(nombreGrupo)) {
                                System.out.println("Escribe un mensaje para enviar al grupo:");
                                String inputMessage = sc.nextLine(); // Leer el mensaje completo del usuario
                                byte[] msg = inputMessage.getBytes(); // Para que sea serializable
                                if (gp.sendGroupMessage(nombreGrupo, alias, msg)) {
                                    System.out.println("Mensaje enviado con éxito");
                                } else {
                                    System.out.println("El mensaje no se ha podido enviar");
                                }
                            } else {
                                System.out.println("No se ha encontrado el grupo");
                            }
                        } catch (NullPointerException e) {
                            System.out.println("No es parte del grupo");
                        }
                        break;
                    
                    case 13: // Recibir mensaje 
                        System.out.println("Introduce el nombre del grupo:");
                        nombreGrupo = sc.next();
                        try{
                            if (gp.isGroup(nombreGrupo)) {
                                System.out.println(receiveGroupMessage(nombreGrupo).toString());
                             } else {
                                System.out.println("No se ha encontrado el grupo");
                            }
                        }catch(RemoteException ex){
                            System.out.println("OH NO!!\n" + ex.getMessage());
                        }    
                    default: // Terminar
                        System.out.println("Terminando el cliente...");
                        UnicastRemoteObject.unexportObject(cliente, true); 
                        break;
                }
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        }
    }
    

