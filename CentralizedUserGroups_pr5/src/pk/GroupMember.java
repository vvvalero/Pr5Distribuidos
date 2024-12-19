/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import java.io.Serializable;

/**
 *
 * @author valen
 */
public class GroupMember implements Serializable{
    String nombreMiembro;
    String hostname;
    int puertohost;
    GroupMember(String nombre,String host,int puerto){
        this.nombreMiembro = nombre;
        this.hostname = host;
        this.puertohost = puerto;
    }
    
    
}
