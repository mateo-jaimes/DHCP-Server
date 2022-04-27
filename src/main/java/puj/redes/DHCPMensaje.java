package puj.redes;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DHCPMensaje implements Serializable {

    private byte op;
    private byte htype;
    private byte hlen;
    private byte hops;
    private Integer xid;
    private short secs;
    private short flags;
    private byte[] ciaddr;
    private byte[] yiaddr;
    private byte[] siaddr;
    private byte[] giaddr;
    private byte[] chaddr;
    private byte[] sname;
    private byte[] file;
    DHCPOpciones opciones;

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
            this.chaddr = extraerBytes(datagramPacket.getData(), 28, 44);
            this.sname = extraerBytes(datagramPacket.getData(), 72, 64);
            this.file = extraerBytes(datagramPacket.getData(), 136, 128);
            // Options...
            // Validar que los datos del objeto estén correctos.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DHCPMensaje(byte op, byte htype, byte hlen, byte hops, int xid, short secs, short flags, byte[] ciaddr, byte[] yiaddr, byte[] siaddr, byte[] giaddr, byte[] chaddr, byte[] sname, byte[] file, DHCPOpciones opciones) {
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
        this.opciones = opciones;
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

    public DHCPOpciones getOpciones() {
        return opciones;
    }

    public void setOpciones(DHCPOpciones opciones) {
        this.opciones = opciones;
    }

    private static byte[] extraerBytes(byte[] buffer, int inicio, int tam) throws Exception {
        byte[] resultado = new byte[tam];
        System.arraycopy(buffer, inicio, resultado, 0, tam);
        return resultado;
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
                "\nciaddr: " + Arrays.toString(ciaddr) +
                "\nyiaddr: " + Arrays.toString(yiaddr) +
                "\nsiaddr: " + Arrays.toString(siaddr) +
                "\ngiaddr: " + Arrays.toString(giaddr) +
                "\nchaddr: " + Arrays.toString(chaddr) +
                "\nsname: " + Arrays.toString(sname) +
                "\nfile: " + Arrays.toString(file) +
                "\nopciones: " + opciones +
                "\n";
    }
}
