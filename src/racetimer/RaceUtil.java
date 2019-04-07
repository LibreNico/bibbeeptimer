package racetimer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.time.LocalTime;

import static java.time.temporal.ChronoField.MILLI_OF_DAY;

public class RaceUtil {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private static final String ERROR_TAG = "[ERROR] ";
    private static final String INFO_TAG = "[INFO] ";

    public static void printError(StringBuilder message){
        System.out.println(new StringBuilder().append(RaceUtil.ANSI_RED).append(ERROR_TAG).append(message).append(RaceUtil.ANSI_RESET));
    }

    public static void printInfo(StringBuilder message){
        System.out.println(new StringBuilder().append(RaceUtil.ANSI_BLUE).append(INFO_TAG).append(message).append(RaceUtil.ANSI_RESET));
    }


    public static float SAMPLE_RATE = 8000f;

    public static void tone(int hz, int msecs, double vol)
            throws LineUnavailableException
    {
        byte[] buf = new byte[1];
        AudioFormat af =
                new AudioFormat(
                        SAMPLE_RATE, // sampleRate
                        8,           // sampleSizeInBits
                        1,           // channels
                        true,        // signed
                        false);      // bigEndian
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i=0; i < msecs*8; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
            sdl.write(buf,0,1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    public static void tone(int hz, int msecs)

    {
        try {
            tone(hz, msecs, 1.0);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static long getMillisecond(LocalTime date1) {
        return date1.get(MILLI_OF_DAY);
    }

    public static String paddingLeftTwoZero(int value) {
        return String.format("%02d", value);
    }

    public static String paddingRightThreeZero(long value) {
        return String.format("%03d", value);
    }

    public static boolean isNumeric(String cadena) {
        try {
            Long.parseLong(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static void printError(String msg) {
        System.out.println("[ERROR] "+msg);
    }

    public static void printInfo(String msg) {
        System.out.println("[INFO] "+msg);
    }
}
