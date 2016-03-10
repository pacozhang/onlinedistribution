/**
 * SmsInterface_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package nds.weixin.ext.ssdjsms;

public interface SmsInterface_PortType extends java.rmi.Remote {
    public java.lang.String getUserInfo(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public java.lang.String setUserInfo(java.lang.String username, java.lang.String password, java.lang.String newPassword) throws java.rmi.RemoteException;
    public java.lang.String clusterSend(java.lang.String username, java.lang.String password, java.lang.String from, java.lang.String to, java.lang.String text, java.lang.String presendTime, java.lang.String isVoice) throws java.rmi.RemoteException;
    public java.lang.String getMoMsg(java.lang.String username, java.lang.String password, java.lang.String lastId) throws java.rmi.RemoteException;
}
