package users;

import io.jsondb.JsonDBTemplate;
import mixingServer.MatchingServerGUI;
import mixingServer.MixingServerGUI;
import registar.RegistarGUI;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.bouncycastle.util.encoders.Hex.toHexString;

public class Main {
    public static void main(String[] args){
        try {
            JFrame registarFrame = new UserGUI("User");
            registarFrame.setVisible(true);
            JFrame matchingServerFrame = new CateringFacilityGUI("CateringFacility Gui");
            matchingServerFrame.setVisible(true);
            JFrame mixingServerFrame = new DoctorGUI("Doctor");
            mixingServerFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
