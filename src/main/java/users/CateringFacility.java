package users;

import interfaceRMI.IRegistar;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CateringFacility {
    private String buisnessId;
    private String name;
    private String address;
    private String phoneNumber;

    private String[] dayTokens;

    private final String hostName;
    private final int port;

    private IRegistar registar;

    public CateringFacility(String buisnessId, String name, String address, String phoneNumber, String hostName, int port) {
        this.buisnessId = buisnessId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.hostName = hostName;
        this.port = port;

        start();
    }

    public synchronized void start(){
        try {
            Registry myRegistry = LocateRegistry.getRegistry(hostName, port);
            registar = (IRegistar) myRegistry.lookup("Registar");

            registar.EnrolCF(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getBuisnessId() {
        return buisnessId;
    }

    public void setBuisnessId(String buisnessId) {
        this.buisnessId = buisnessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
