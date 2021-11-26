package su.plo.voice.utils;

import java.nio.ByteBuffer;

public class AudioUtils {
    public static short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).asShortBuffer().get(shorts);
        return shorts;
    }

    public static byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes).asShortBuffer().put(shorts);
        return bytes;
    }

    public static float[] bytesToFloats(byte[] bytes) {
        float[] floats = new float[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            floats[i / 2] = ((float) AudioUtils.bytesToShort(bytes[i], bytes[i + 1])) / 0x8000;
        }

        return floats;
    }

    public static float[] shortsToFloats(short[] shorts) {
        float[] floats = new float[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            floats[i] = ((Short) shorts[i]).floatValue();
        }
        return floats;
    }

    public static float[] shortsToFloatsObs(short[] shorts) {
        float[] floats = new float[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            floats[i] = ((float) shorts[i]) / 0x8000;
        }
        return floats;
    }

    public static byte[] floatsToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short audioSample = (short) (floats[i / 2] * 0x8000);

            bytes[i] = (byte) audioSample;
            bytes[i + 1] = (byte) (audioSample >> 8);
        }

        return bytes;
    }

    public static short[] floatsToShorts(float[] floats) {
        short[] shorts = new short[floats.length];
        for (int i = 0; i < floats.length; i++) {
            shorts[i] = ((Float) floats[i]).shortValue();
        }
        return shorts;
    }

    public static short[] floatsToShortsObs(float[] floats) {
        short[] shorts = new short[floats.length];
        for (int i = 0; i < floats.length; i++) {
            shorts[i] = (short) (floats[i] * 0x8000);
        }
        return shorts;
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    public static float mulToDB(float mul) {
        return (mul == 0.0f) ? -Float.MAX_VALUE : (float) (20.0F * Math.log10(mul));
    }

    public static float dbToMul(float db) {
        return Float.isFinite(db) ? (float) Math.pow(10.0F, db / 20.0F) : 0.0F;
    }

    public static float gainCoefficient(int sampleRate, float time) {
        return (float)Math.exp(-1.0f / (sampleRate * time));
    }

    public static boolean hasHigherLevel(short[] samples, double targetLevel) {
        for (int i = 0; i < samples.length; i += 50) {
            double chunkLevel = calculateAudioLevel(samples, i, Math.min(i + 50, samples.length));
            if (chunkLevel >= targetLevel) {
                return true;
            }
        }
        return false;
    }

    public static double getHighestAudioLevel(short[] samples) {
        double highest = -127D;
        for (int i = 0; i < samples.length; i += 50) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 50, samples.length));
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    /**
     * Calculates the audio level of a signal with specific <tt>samples</tt>.
     * Source: https://github.com/jitsi/libjitsi/blob/master/src/main/java/org/jitsi/impl/neomedia/audiolevel/AudioLevelCalculator.java
     *
     * @param samples the samples of the signal to calculate the audio level of
     * @param offset the offset in <tt>samples</tt> in which the samples start
     * @param length the length in bytes of the signal in <tt>samples<tt>
     * starting at <tt>offset</tt>
     * @return the audio level of the specified signal
     */
    public static double calculateAudioLevel(short[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (; offset < length; offset++) {
            double sample = (double) samples[offset] / Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = 20 * Math.log10(rms);
            // XXX The audio level is expressed in -dBov.
            db = -db;
            // Ensure that the calculated audio level is within the range
            // between MIN_AUDIO_LEVEL and MAX_AUDIO_LEVEL.
            if (db > 127D)
                db = 127D;
            else if (db < 0D)
                db = 0D;
        } else {
            db = 127D;
        }

        return -db;
    }
}
