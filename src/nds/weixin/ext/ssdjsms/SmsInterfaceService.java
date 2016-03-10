/**
 * SmsInterfaceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package nds.weixin.ext.ssdjsms;

public interface SmsInterfaceService extends javax.xml.rpc.Service {
    public java.lang.String getSmsInterfaceAddress();

    public nds.weixin.ext.ssdjsms.SmsInterface_PortType getSmsInterface() throws javax.xml.rpc.ServiceException;

    public nds.weixin.ext.ssdjsms.SmsInterface_PortType getSmsInterface(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
