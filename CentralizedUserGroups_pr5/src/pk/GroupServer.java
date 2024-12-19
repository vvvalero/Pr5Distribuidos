/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author valen
 */
public class GroupServer extends UnicastRemoteObject implements GroupServerInterface{
    public class Group{
        String nombreGrupo;
        String propietario;
        List<GroupMember> listaMiembros = new LinkedList();
        private final Condition modificarMiembros = lock.newCondition();
        private boolean permitirCambiosMiembros = true; // Inicialmente se permiten cambio
        //cambios practica 5
        int numMiembros;
        int enviosEnCurso;
        
        Group(String nombre,String propietario,GroupMember miembro, int puerto){
            this.nombreGrupo = nombre;
            this.propietario = propietario;
            this.listaMiembros.add(new GroupMember(miembro.nombreMiembro,miembro.hostname,puerto));
            this.numMiembros = 1;
        }
    }
    
    private List<Group> listaGrupos = new LinkedList();
    private ReentrantLock lock = new ReentrantLock();
    String ruta = "C:/Users/valen/OneDrive/Documentos/NetBeansProjects/SecurityPolicies";
    public GroupServer() throws RemoteException{
        super();
        System.setProperty("java.security.policy",ruta);
        System.out.println("Policy: " + System.getProperty("java.security.policy"));
        if (System.getSecurityManager()==null){
            System.setSecurityManager(new SecurityManager());
        }
        System.out.println("Inicializado servidor");
    }
    @Override
    public boolean createGroup(String galias,String oalias, String ohostname, int puerto) throws RemoteException{
        lock.lock();
        try{
            if(isGroup(galias)) return false;
            
            //creamos un nuevo miembro
            GroupMember nuevoMiembro = new GroupMember(oalias,ohostname,puerto);
            //creamos el grupo 
            listaGrupos.add(new Group(galias,oalias,nuevoMiembro,puerto));
            System.out.println("grupo creado");
            return true;
        }finally{lock.unlock();}
    }
    
    @Override
    public boolean isGroup(String galias) throws RemoteException{
        lock.lock();
        try{
            boolean encontrado = false;
            int i = 0;
            while(!encontrado && i < listaGrupos.size()){
                if (galias.equals(listaGrupos.get(i).nombreGrupo))
                    encontrado = true;    
                i=i+1; 
            }
            return encontrado;
        }finally{lock.unlock();}
    }
    
    @Override
    public boolean removeGroup(String galias,String oalias){
        lock.lock();
        try{
            boolean borrado = false;
            int i = 0;
            while(!borrado && i < listaGrupos.size() ){
                if (galias.equals(listaGrupos.get(i).nombreGrupo))
                    if (listaGrupos.get(i).propietario.equals(oalias))
                    {
                        listaGrupos.remove(i);
                        borrado = true;
                    }
                i++;
            }
            return borrado;
        }finally{lock.unlock();}
    }
    
    @Override
    public boolean addMember(String galias, String alias, String hostname, int puerto) throws RemoteException {
    lock.lock(); // Bloqueo general para proteger la lista de grupos
    try {
        // Buscar el grupo en la lista
        Group group = null;
        for (Group g : listaGrupos) {
            if (g.nombreGrupo.equals(galias)) {
                group = g;
                break;
            }
        }

        if (group == null) {
            return false; // El grupo no existe
        }

        // Bloqueo específico para el grupo
        try {
            // Esperar si las altas y bajas están bloqueadas
            if(!group.permitirCambiosMiembros) {
                group.modificarMiembros.await(); // Espera hasta que se permitan los cambios
            }

            // Verificar si ya existe el miembro en el grupo
            for (GroupMember member : group.listaMiembros) {
                if (member.nombreMiembro.equals(alias)) {
                    return false; // El miembro ya existe
                }
            }

            // Agregar el nuevo miembro al grupo
            group.listaMiembros.add(new GroupMember(alias, hostname,puerto));
            return true; // Operación exitosa
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            return false; // Falló debido a una interrupción
        }
    } finally {
        lock.unlock(); // Liberar el bloqueo general
    }
}

    @Override
    public boolean removeMember(String galias, String alias) throws RemoteException{
        lock.lock();
        try{
            //posiblemente el codigo mas feo que he hecho hasta ahora, lo cual es dificil
            for(int i=0;i<listaGrupos.size();i++){
                //Si (nombreGrupo == galias)
                if (listaGrupos.get(i).nombreGrupo.equals(galias)){
                    for(int j=0;j<listaGrupos.get(i).listaMiembros.size();j++){
                        //Si (nombreGrupo.nombreMiembro == alias)
                       if (listaGrupos.get(i).listaMiembros.get(j).nombreMiembro.equals(alias)){
                                // Esperar si las altas y bajas están bloqueadas
                                while (!listaGrupos.get(i).permitirCambiosMiembros) {
                                    listaGrupos.get(i).modificarMiembros.await(); // Espera hasta que se permitan los cambios
                                }
                           listaGrupos.get(i).listaMiembros.remove(j);
                           return true;
                       }
                    }
                }
            } 
        }catch(InterruptedException ex){System.out.println(ex.getMessage());} finally {lock.unlock();}    
        return false;
    }
    
    @Override
    public boolean isMember(String galias, String alias) throws RemoteException{
            for(int i=0;i<listaGrupos.size();i++){
                //Si (nombreGrupo == galias)
                if (listaGrupos.get(i).nombreGrupo.equals(galias)){
                    for(int j=0;j<listaGrupos.get(i).listaMiembros.size();j++){
                        //Si (nombreGrupo.nombreMiembro == alias)
                       if (listaGrupos.get(i).listaMiembros.get(j).nombreMiembro.equals(alias)){
                           return true;
                       }
                    }
                }
            }
                
        return false;
    }
    
    @Override
    public String Owner(String galias) throws RemoteException{
        for(int i=0;i<listaGrupos.size();i++){
                //Si (nombreGrupo == galias)
                if (listaGrupos.get(i).nombreGrupo.equals(galias)){
                    return listaGrupos.get(i).propietario;
                }
        }
        return null;
    }
    
    @Override
    public boolean StopMembers(String galias) {
        lock.lock(); // Protege la lista de grupos durante la búsqueda
        try {
            for (Group grupo : listaGrupos) {
                if (grupo.nombreGrupo.equals(galias)) {
                    grupo.permitirCambiosMiembros = false; 
                    
                    return true; // Grupo encontrado y bloqueado
                }
            }
        } finally {
            lock.unlock(); // Libera el cerrojo del servidor
        }
        return false; // Grupo no encontrado
    }
    
    @Override
    public boolean AllowMembers(String galias) {
        lock.lock(); // Protege la lista de grupos durante la búsqueda
        try {
            for (Group grupo : listaGrupos) {
                if (grupo.nombreGrupo.equals(galias)) {
                    grupo.permitirCambiosMiembros = true; // Permite modificaciones
                    grupo.modificarMiembros.signalAll(); // Notifica a los hilos en espera
                    return true; // Grupo encontrado y desbloqueado
                }
            }
        } finally {
            lock.unlock(); // Libera el cerrojo del servidor
        }
        return false; // Grupo no encontrado
    }     
    
    @Override
    public LinkedList<String> ListMembers(String galias) throws RemoteException{
        lock.lock();
        LinkedList<String> resultado = new LinkedList();
        try{
            for (int i = 0; i<listaGrupos.size();i++){
                if (galias.equals(listaGrupos.get(i).nombreGrupo)){
                    for (int j = 0; j<listaGrupos.get(i).listaMiembros.size();j++){
                        resultado.add(listaGrupos.get(i).listaMiembros.get(j).nombreMiembro);
                    }
                }
            }
            return resultado;
        }finally{lock.unlock();}
    }
    
    @Override
    public LinkedList<String> ListGroups() throws RemoteException{
        lock.lock();
        LinkedList<String> resultado = new LinkedList();
        try{
            for (int i = 0; i<listaGrupos.size();i++){
                resultado.add(listaGrupos.get(i).nombreGrupo);
            }
            return resultado;
        }finally{lock.unlock();}
    }
    
    @Override
    public boolean sendGroupMessage(String galias, String alias, byte[] msg) throws RemoteException{
        lock.lock();
        GroupMember miembroEnvia = null;
        GroupMember miembroRecibe;
        Group grupo=null;
        try{
            if(this.isGroup(galias) && this.isMember(galias, alias)){
                this.StopMembers(galias);
                LinkedList miembros = this.ListMembers(galias);
                GroupMember envia;
                LinkedList grupos = this.ListGroups();
                
                // Guardamos el nombre del grupo en una variable
                for(int i=0; i<listaGrupos.size();i++){
                    if(listaGrupos.get(i).nombreGrupo.equals(galias)){
                        grupo = listaGrupos.get(i);
                        i=listaGrupos.size(); // para parar el bucle sin usar break
                    }      
                }
                
                // Guardamos el emisor en una variable
                for(int i=0;i<grupo.listaMiembros.size();i++){
                    if(grupo.listaMiembros.get(i).nombreMiembro.equals(alias)){
                        miembroEnvia = grupo.listaMiembros.get(i);
                        i = grupo.listaMiembros.size();
                    }
                }
                
                GroupMessage mensaje = new GroupMessage(galias,msg,miembroEnvia);
                
                // Enviamos un mensaje a todos los miembros del grupo, menos al que los envia
                for(int i=0;i<grupo.listaMiembros.size();i++){
                    miembroRecibe = grupo.listaMiembros.get(i);
                    
                    if(!miembroRecibe.nombreMiembro.equals(alias)){ // mas eficiente que comprar groupMembers
                        SendingMessage enviarMensaje = new SendingMessage(miembroRecibe,mensaje,grupo); //falta por implementar la clase SendingMessage
                        Thread hilo = new Thread(enviarMensaje);
                        hilo.start();
                    }
                }
                
                while(grupo.enviosEnCurso>0){} // no me gusta nada pero tampoco se me ocurre otra cosa. 
                                                // re:Quizas se puede hacer con un semaforo
                this.AllowMembers(grupo.nombreGrupo);
            }
        }finally{lock.unlock();}
        return true;
    }
}


