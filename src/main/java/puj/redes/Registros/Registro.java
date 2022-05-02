package puj.redes.Registros;

import puj.redes.DHCPMensaje;

import java.net.InetAddress;
import java.util.Date;

public class Registro {
    private byte[] chaddr = new byte [16];
    private InetAddress IP;
    private Date tiempoACK;
    private Date tiempoRetirar;
    private String hostname;

    public Registro () {

    }

    public Registro (byte[] chaddr, InetAddress IP, Date tiempoAcuse, Date tiempoAsignado, String hostname) {
        this.chaddr = chaddr;
        this.IP = IP;
        this.tiempoACK = tiempoAcuse;
        this.tiempoRetirar = tiempoAsignado;
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

    public Date getTiempoACK() {
        return tiempoACK;
    }

    public void setTiempoACK(Date tiempoACK) {
        this.tiempoACK = tiempoACK;
    }

    public Date getTiempoRetirar() {
        return tiempoRetirar;
    }

    public void setTiempoRetirar(Date tiempoRetirar) {
        this.tiempoRetirar = tiempoRetirar;
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
                tiempoACK + ", " +
                tiempoRetirar + ", " +
                hostname;
    }
}
