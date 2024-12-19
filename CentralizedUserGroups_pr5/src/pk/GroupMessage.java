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
public class GroupMessage implements Serializable{
    String nombreGrupo;
    byte[] mensaje;
    GroupMember emisor;
    
    public GroupMessage(String nombreGrupo, byte[] Mensaje, GroupMember emisor){
        this.nombreGrupo = nombreGrupo;
        this.mensaje = Mensaje;
        this.emisor = emisor;
    }
}
