package puj.redes;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class DHCPMensaje implements Serializable {

    private byte op;
    private byte htype;
    private byte hlen;
    private byte hops;
    private Integer xid;
    private short secs;
    private short flags;
    private byte[] ciaddr = new byte [4];
    private byte[] yiaddr = new byte [4];
    private byte[] siaddr = new byte [4];
    private byte[] giaddr = new byte [4];
    private byte[] chaddr = new byte [16];
    private byte[] sname  = new byte [64];
    private byte[] file  = new byte [128];
    private byte[] opciones;
    ArrayList <DHCPOpciones> opcionesDHCP = new ArrayList<>();

    public DHCPMensaje (DatagramPacket datagramPacket) {

        try {
            this.op = extraerBytes(datagramPacket.getData(), 0, 1)[0];
            this.htype = extraerBytes(datagramPacket.getData(), 1, 1)[0];
            this.hlen = extraerBytes(datagramPacket.getData(), 2, 1)[0];
            this.hops = extraerBytes(datagramPacket.getData(), 3, 1)[0];
            this.xid = ByteBuffer.wrap(extraerBytes(datagramPacket.getData(), 4, 4)).getInt();
            this.secs = ByteBuffer.wrap(extraerBytes(datagramPacket.getData(), 8, 2)).getShort();
            this.flags = ByteBuffer.wrap(extraerBytes(datagramPacket.getData(), 10, 2)).getShort();
            this.ciaddr = extraerBytes(datagramPacket.getData(), 12, 4);
            this.yiaddr = extraerBytes(datagramPacket.getData(), 16, 4);
            this.siaddr = extraerBytes(datagramPacket.getData(), 20, 4);
            this.giaddr = extraerBytes(datagramPacket.getData(), 24, 4);
            this.chaddr = extraerBytes(datagramPacket.getData(), 28, 16);
            this.sname = extraerBytes(datagramPacket.getData(), 44, 64);
            this.file = extraerBytes(datagramPacket.getData(), 108, 128);
            byte[] temp = extraerBytes(datagramPacket.getData(), 240, datagramPacket.getLength() - 240);

            for (int i = 0; temp[i] != -1; i += temp[i+1] + 2) { // temp[i] != -1 indicando que el ultimo campo es -1 (byte 255) indicando el final del campo opciones.
                DHCPOpciones opcionDHCP = new DHCPOpciones();
                opcionDHCP.setType(temp[i]);
                opcionDHCP.setLength(temp[i+1]);
                opcionDHCP.setValue(extraerBytes(temp, i+2, opcionDHCP.getLength()));
                this.opcionesDHCP.add(opcionDHCP);
            }

            // Validar que los datos del objeto estén correctos.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DHCPMensaje(byte op, byte htype, byte hlen, byte hops, int xid, short secs, short flags, byte[] ciaddr, byte[] yiaddr, byte[] siaddr, byte[] giaddr, byte[] chaddr, byte[] sname, byte[] file, ArrayList <DHCPOpciones> opcionesDHCP) {
        this.op = op;
        this.htype = htype;
        this.hlen = hlen;
        this.hops = hops;
        this.xid = xid;
        this.secs = secs;
        this.flags = flags;
        this.ciaddr = ciaddr;
        this.yiaddr = yiaddr;
        this.siaddr = siaddr;
        this.giaddr = giaddr;
        this.chaddr = chaddr;
        this.sname = sname;
        this.file = file;
        this.opcionesDHCP = opcionesDHCP;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public byte getHtype() {
        return htype;
    }

    public void setHtype(byte htype) {
        this.htype = htype;
    }

    public byte getHlen() {
        return hlen;
    }

    public void setHlen(byte hlen) {
        this.hlen = hlen;
    }

    public byte getHops() {
        return hops;
    }

    public void setHops(byte hops) {
        this.hops = hops;
    }

    public int getXid() {
        return xid;
    }

    public void setXid(int xid) {
        this.xid = xid;
    }

    public short getSecs() {
        return secs;
    }

    public void setSecs(short secs) {
        this.secs = secs;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(short flags) {
        this.flags = flags;
    }

    public byte[] getCiaddr() {
        return ciaddr;
    }

    public void setCiaddr(byte[] ciaddr) {
        this.ciaddr = ciaddr;
    }

    public byte[] getYiaddr() {
        return yiaddr;
    }

    public void setYiaddr(byte[] yiaddr) {
        this.yiaddr = yiaddr;
    }

    public byte[] getSiaddr() {
        return siaddr;
    }

    public void setSiaddr(byte[] siaddr) {
        this.siaddr = siaddr;
    }

    public byte[] getGiaddr() {
        return giaddr;
    }

    public void setGiaddr(byte[] giaddr) {
        this.giaddr = giaddr;
    }

    public byte[] getChaddr() {
        return chaddr;
    }

    public void setChaddr(byte[] chaddr) {
        this.chaddr = chaddr;
    }

    public byte[] getSname() {
        return sname;
    }

    public void setSname(byte[] sname) {
        this.sname = sname;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public byte[] getOpciones() {
        return opciones;
    }

    public void setOpciones(byte[] opciones) {
        this.opciones = opciones;
    }

    public static byte[] extraerBytes(byte[] buffer, int inicio, int tam) throws Exception {
        byte[] resultado = new byte[tam];
        System.arraycopy(buffer, inicio, resultado, 0, tam);
        return resultado;
    }

    public void setXid(Integer xid) {
        this.xid = xid;
    }

    public ArrayList<DHCPOpciones> getOpcionesDHCP() {
        return opcionesDHCP;
    }

    public void setOpcionesDHCP(ArrayList<DHCPOpciones> opcionesDHCP) {
        this.opcionesDHCP = opcionesDHCP;
    }

    @Override
    public String toString() {
        return "Mensaje DHCP:\n" +
                "op: " + op +
                "\nhtype: " + htype +
                "\nhlen: " + hlen +
                "\nhops: " + hops +
                "\nxid: " + xid +
                "\nsecs: " + secs +
                "\nflags: " + flags +
                "\nciaddr: " + printByteArray(ciaddr, 1) +
                "\nyiaddr: " + printByteArray(yiaddr, 1) +
                "\nsiaddr: " + printByteArray(siaddr, 1) +
                "\ngiaddr: " + printByteArray(giaddr, 1) +
                "\nchaddr: " + printByteArray(chaddr, 2) +
                "\nsname: " + printByteArray(sname, 0) +
                "\nfile: " + printByteArray(file, 0) +
                "\nopciones: " + opcionesDHCP.toString() +
                "\n";
    }

    public static String printByteArray(byte[] bytes, int opcion) {
        // opcion 1 = IP
        // opcion 2 = MAC
        // else print normal
        StringBuilder str = new StringBuilder();
        int pos = 0;
        for (byte b : bytes) {
            if (opcion == 1) {
                str.append(b & 0xFF);
                str.append(".");
            }
            else if (opcion == 2) {
                str.append(String.format("%02X:", b));
                pos++;
                if (pos > 5) break;
            }
            else {
                str.append(b & 0xFF);
                str.append(" ");
            }
        }
        return str.toString().substring(0, str.length() - 1);
    }
}
