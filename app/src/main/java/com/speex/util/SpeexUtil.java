package com.speex.util;

public class SpeexUtil {

    private static final int DEFAULT_COMPRESSION = 4;

    static {
        try {
            System.loadLibrary("speex");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static SpeexUtil speexUtil = null;

    SpeexUtil() {
        open(DEFAULT_COMPRESSION);
    }

    public static SpeexUtil getInstance() {
        if (speexUtil == null) {
            synchronized (SpeexUtil.class) {
                if (speexUtil == null) {
                    speexUtil = new SpeexUtil();
                }
            }
        }
        return speexUtil;
    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte encoded[], short lin[], int size);

    public native int encode(short lin[], int offset, byte encoded[], int size);

    public native void close();

}
