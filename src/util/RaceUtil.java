package util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoField.MILLI_OF_DAY;

public class RaceUtil {


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


    public static void backupData(String inputScan, BufferedWriter writer) {

        if(writer != null){
            try {
                writer.write(inputScan+"\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        //Toolkit.getDefaultToolkit().beep();
        RaceUtil.tone(1000, 100);

    }

    public static BufferedWriter createBackup()  {


        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
            String formatTime = LocalDateTime.now().format(formatter);

            File yourFile = new File("bibbeep_backup_" + formatTime + ".csv");
            yourFile.createNewFile();

            return Files.newBufferedWriter(Paths.get(yourFile.getPath()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public static void closeBackup(BufferedWriter backupWriter) {
        if(backupWriter != null){
            try {
                backupWriter.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
