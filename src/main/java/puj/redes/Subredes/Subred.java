package puj.redes.Subredes;

public class Subred {
    private byte[] rangoInicial;
    private byte[] rangoFinal;
    private byte[] mascaraSubnet;
    private byte[] puertaEnlace;
    private byte[] servidorDNS;
    private Integer tiempo;

    public Integer getTiempo() {
        return tiempo;
    }

    public void setTiempo(Integer tiempo) {
        this.tiempo = tiempo;
    }

    public byte[] getRangoInicial() {
        return rangoInicial;
    }

    public void setRangoInicial(byte[] rangoInicial) {
        this.rangoInicial = rangoInicial;
    }

    public byte[] getRangoFinal() {
        return rangoFinal;
    }

    public void setRangoFinal(byte[] rangoFinal) {
        this.rangoFinal = rangoFinal;
    }

    public byte[] getMascaraSubnet() {
        return mascaraSubnet;
    }

    public void setMascaraSubnet(byte[] mascaraSubnet) {
        this.mascaraSubnet = mascaraSubnet;
    }

    public byte[] getPuertaEnlace() {
        return puertaEnlace;
    }

    public void setPuertaEnlace(byte[] puertaEnlace) {
        this.puertaEnlace = puertaEnlace;
    }

    public byte[] getServidorDNS() {
        return servidorDNS;
    }

    public void setServidorDNS(byte[] servidorDNS) {
        this.servidorDNS = servidorDNS;
    }

}