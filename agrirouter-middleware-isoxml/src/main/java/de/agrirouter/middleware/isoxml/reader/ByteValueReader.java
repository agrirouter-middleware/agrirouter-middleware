package de.agrirouter.middleware.isoxml.reader;


import java.nio.ByteBuffer;

public final class ByteValueReader {

    public static byte readByte(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    public static byte readEnum(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    public static int readInteger(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }

    public static long readUnsignedLong(ByteBuffer byteBuffer) {
        return Integer.toUnsignedLong(byteBuffer.getInt());
    }

    public static int readUnsignedShort(ByteBuffer byteBuffer) {
        return Short.toUnsignedInt(byteBuffer.getShort());
    }

    public static int readDdi(ByteBuffer byteBuffer) {
        return Short.toUnsignedInt(byteBuffer.getShort());
    }

    public static double readDouble(ByteBuffer byteBuffer) {
        return byteBuffer.getInt() * 1e-7;
    }

    public static float readDecimal(ByteBuffer byteBuffer) {
        return byteBuffer.getShort() * 1e-1F;
    }

}
