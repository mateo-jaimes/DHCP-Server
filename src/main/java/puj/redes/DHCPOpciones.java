package puj.redes;

import java.io.Serializable;
import java.util.Arrays;

public class DHCPOpciones implements Serializable {

    private byte length;
    private byte type;
    private byte[] value;

    public DHCPOpciones () {

    }

    public DHCPOpciones(byte length, byte type, byte[] value) {
        this.length = length;
        this.type = type;
        this.value = value;
    }

    public byte getLength() {
        return length;
    }

    public void setLength(byte length) {
        this.length = length;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" +
                "" + length +
                ", " + type +
                ", " + Arrays.toString(value) +
                "]\n";
    }
}
