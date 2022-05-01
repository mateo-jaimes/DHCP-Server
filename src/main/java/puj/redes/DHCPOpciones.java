package puj.redes;

import java.io.Serializable;
import java.util.Arrays;

public class DHCPOpciones implements Serializable {

    private byte length;
    private byte type;
    private byte[] value;

    public DHCPOpciones () {

    }

    public DHCPOpciones(byte type, byte[] value) {
        this.type = type;
        this.value = value;
        this.length = (byte) value.length;
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
