package puj.redes.Registros;

import puj.redes.DHCPMensaje;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Registro {
    private byte[] chaddr = new byte [16];
    private InetAddress IP;
    private Date tiempoAcuse;
    private Date tiempoAsignado;
    private String hostname;

    public Registro () {

    }

    public Registro(byte[] chaddr, InetAddress IP, Date tiempoAcuse, Date tiempoAsignado, String hostname) {
        this.chaddr = chaddr;
        this.IP = IP;
        this.tiempoAcuse = tiempoAcuse;
        this.tiempoAsignado = tiempoAsignado;
        this.hostname = hostname;
    }

    public byte[] getChaddr() {
        return chaddr;
    }

    public void setChaddr(byte[] chaddr) {
        this.chaddr = chaddr;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress IP) {
        this.IP = IP;
    }

    public Date getTiempoAcuse() {
        return tiempoAcuse;
    }

    public void setTiempoAcuse(Date tiempoAcuse) {
        this.tiempoAcuse = tiempoAcuse;
    }

    public Date getTiempoAsignado() {
        return tiempoAsignado;
    }

    public void setTiempoAsignado(Date tiempoAsignado) {
        this.tiempoAsignado = tiempoAsignado;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return DHCPMensaje.printByteArray(chaddr, 2) + ", " +
                DHCPMensaje.printByteArray(IP.getAddress(), 1) + ", " +
                tiempoAcuse + ", " +
                tiempoAsignado + ", " +
                hostname + "\n";
    }
}
