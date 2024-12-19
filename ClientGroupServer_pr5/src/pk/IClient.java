/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pk;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author valen
 */
public interface IClient extends Remote{
        void DepositMessage(GroupMessage m) throws RemoteException;
}
