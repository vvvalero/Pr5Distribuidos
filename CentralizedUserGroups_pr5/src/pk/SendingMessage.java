/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import pk.GroupServer.Group;

/**
 *
 * @author valen
 */
public class SendingMessage implements Runnable{
    GroupMember miembroRecibe;
    GroupMessage mensaje;
    Group grupo;
    
    public SendingMessage(GroupMember Destino, GroupMessage mensaje,Group grupo){
        this.miembroRecibe=Destino;
        this.mensaje=mensaje;
        this.grupo=grupo;

    }  
    
    @Override
    public void run(){
        Registry registro;
        try{
            grupo.enviosEnCurso++;
            Random tiempoAleatorio = new Random();
            registro = LocateRegistry.getRegistry(miembroRecibe.hostname,miembroRecibe.puertohost);
            Thread.sleep(tiempoAleatorio.nextInt(3100) + 3000);
            //System.setProperty("java.rmi.server.hostname", "localhost");
            IClient InterfazCliente = (IClient) registro.lookup("CLIENTASO"); // falta por crear la interfaz
            InterfazCliente.DepositMessage(mensaje);

        }catch(RemoteException | InterruptedException | NotBoundException ex){
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
