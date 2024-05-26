package org.example;

import java.nio.ByteBuffer;
import java.util.Arrays;
import static org.example.CRC16.CRC16Code;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.io.InvalidObjectException;

public class Packet {
    private final byte bMagic;
    private final byte bSrc;
    private final long bPktId;
    private final int wLen;
    private final short wCrc16Header;
    private final byte[] bMsq;
    private final short wCrc16Msg;

    // Getters
    public byte getbMagic() {
        return bMagic;
    }

    public byte getbSrc() {
        return bSrc;
    }

    public long getbPktId() {
        return bPktId;
    }

    public int getwLen() {
        return wLen;
    }

    public short getwCrc16Header() {
        return wCrc16Header;
    }

    public byte[] getbMsq() {
        return bMsq;
    }

    public short getwCrc16Msg() {
        return wCrc16Msg;
    }

    // Constructor
    public Packet(Message message, byte[] encryption_key) throws InvalidObjectException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.bMagic = 0x13;
        this.bSrc = message.getClient_number();
        this.bPktId = message.getMessage_number();

        // Serialize the message
        ByteBuffer msgBuffer = ByteBuffer.allocate(8 + message.getData().toString().getBytes().length);
        msgBuffer.putInt(message.getCommand_number());
        msgBuffer.putInt(message.getSender_number());
        msgBuffer.put(message.getData().toString().getBytes());
        byte[] message_data = msgBuffer.array();

        // Encrypt the message
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryption_key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        this.bMsq = cipher.doFinal(message_data);

        this.wLen = bMsq.length;

        // Construct header
        ByteBuffer headerBuffer = ByteBuffer.allocate(14);
        headerBuffer.put(bMagic);
        headerBuffer.put(bSrc);
        headerBuffer.putLong(bPktId);
        headerBuffer.putInt(wLen);

        byte[] headerBytes = headerBuffer.array();
        this.wCrc16Header = CRC16Code(headerBytes);

        ByteBuffer packetBuffer = ByteBuffer.allocate(16 + bMsq.length + 2);
        packetBuffer.put(headerBytes);
        packetBuffer.putShort(wCrc16Header);
        packetBuffer.put(bMsq);
        this.wCrc16Msg = CRC16Code(bMsq);
        packetBuffer.putShort(wCrc16Msg);

        byte[] packetBytes = packetBuffer.array();

        // Validation
        if (CRC16Code(Arrays.copyOfRange(packetBytes, 0, 14)) != wCrc16Header ||
                CRC16Code(Arrays.copyOfRange(packetBytes, 16, 16 + wLen)) != wCrc16Msg) {
            throw new InvalidObjectException("Invalid CRC16 checksum");
        }
    }

    // Method to convert Packet to byte array
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(16 + bMsq.length + 2);
        buffer.put(bMagic);
        buffer.put(bSrc);
        buffer.putLong(bPktId);
        buffer.putInt(wLen);
        buffer.putShort(wCrc16Header);
        buffer.put(bMsq);
        buffer.putShort(wCrc16Msg);
        return buffer.array();
    }
}
