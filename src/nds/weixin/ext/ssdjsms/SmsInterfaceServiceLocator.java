/**
 * SmsInterfaceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package nds.weixin.ext.ssdjsms;

public class SmsInterfaceServiceLocator extends org.apache.axis.client.Service implements nds.weixin.ext.ssdjsms.SmsInterfaceService {

    public SmsInterfaceServiceLocator() {
    }


    public SmsInterfaceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SmsInterfaceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SmsInterface
    private java.lang.String SmsInterface_address = "http://219.133.59.101/ws/ws/SmsInterface";

    public java.lang.String getSmsInterfaceAddress() {
        return SmsInterface_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SmsInterfaceWSDDServiceName = "SmsInterface";

    public java.lang.String getSmsInterfaceWSDDServiceName() {
        return SmsInterfaceWSDDServiceName;
    }

    public void setSmsInterfaceWSDDServiceName(java.lang.String name) {
        SmsInterfaceWSDDServiceName = name;
    }

    public nds.weixin.ext.ssdjsms.SmsInterface_PortType getSmsInterface() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SmsInterface_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSmsInterface(endpoint);
    }

    public nds.weixin.ext.ssdjsms.SmsInterface_PortType getSmsInterface(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
        	nds.weixin.ext.ssdjsms.SmsInterfaceSoapBindingStub _stub = new nds.weixin.ext.ssdjsms.SmsInterfaceSoapBindingStub(portAddress, this);
            _stub.setPortName(getSmsInterfaceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSmsInterfaceEndpointAddress(java.lang.String address) {
        SmsInterface_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (nds.weixin.ext.ssdjsms.SmsInterface_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
            	nds.weixin.ext.ssdjsms.SmsInterfaceSoapBindingStub _stub = new nds.weixin.ext.ssdjsms.SmsInterfaceSoapBindingStub(new java.net.URL(SmsInterface_address), this);
                _stub.setPortName(getSmsInterfaceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SmsInterface".equals(inputPortName)) {
            return getSmsInterface();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://219.133.59.101/ws/ws/SmsInterface", "SmsInterfaceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://219.133.59.101/ws/ws/SmsInterface", "SmsInterface"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SmsInterface".equals(portName)) {
            setSmsInterfaceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
