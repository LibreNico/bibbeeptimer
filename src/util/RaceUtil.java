package util;

import gui.Main;
import model.Runner;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.MILLI_OF_DAY;

public class RaceUtil {


    public static final String CSV_EXTENSION = ".csv";
    public static final String BACKUP_PREFIX = "/bibbeep_backup_";
    private static final String HTML_EXTENSION = ".html";
    private static final String REPORT_ALL_PREFIX = "/bibbeep_report_all";
    private static final String REPORT_CATEGORY_PREFIX = "/bibbeep_report_category";

    private static final String HTML_HEADER =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<meta charset=\"UTF-8\">\n" +
                    "<style>\n" +
                    "table, td, th {    \n" +
                    "    border: 1px solid #ddd;\n" +
                    "    text-align: left;\n" +
                    "}\n" +
                    "\n" +
                    "table {\n" +
                    "    border-collapse: collapse;\n" +
                    "    width: 100%;\n" +
                    "}\n" +
                    "\n" +
                    "th, td {\n" +
                    "    padding: 15px;\n" +
                    "}\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" ;
    private static final String TABLE_HEADER = "<table>\n" +
            "  <tr>\n" +
            "    <th width=\"7%\">Pos.</th>\n" +
            "    <th width=\"7%\">Num.</th>\n" +
            "    <th width=\"30%\">Nom|Naam</th>\n" +
            "    <th width=\"20%\">Temps|Tijd</th>\n" +
            "    <th width=\"26%\">Club</th>\n" +
            "    <th width=\"6%\">Gender</th>\n" +
            "  </tr>";
    private static final String HTML_FOOTER = "</body>\n" +
    "</html>";

    private static final String TABLE_FOOTER = "</table>\n";
    private static final String CREATE_FILE = "Create file";

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

    public static void pushErrorNotification(String title, String msg) {
        pushNotification(title, msg,true);
    }

    public static void pushInfoNotification(String title, String msg) {
        pushNotification(title, msg,false);
    }

    private static void pushNotification(String title, String msg, boolean isError) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formatTime = LocalDateTime.now().format(formatter);
        Main.TEXT_NOTIFICATION.setText((isError?"[ERROR]":"[INFO]")+"["+title+" - "+ formatTime +"] "+msg);
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

    public static String createUniqueNameFile(String prefix, String extension) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
        String formatTime = LocalDateTime.now().format(formatter);
        return  prefix + formatTime + extension;
    }

    public static BufferedWriter createBufferWriter(String backupPath, String uniqueName)  {


        String pathname = backupPath + uniqueName ;
        try {
            pushInfoNotification( CREATE_FILE, "Report/Backup file: "+pathname);
            File yourFile = new File(pathname);
            yourFile.createNewFile();

            return Files.newBufferedWriter(Paths.get(yourFile.getPath()));
        } catch (IOException ioException) {
            pushErrorNotification(CREATE_FILE, "Error create file: "+pathname+" because "+ioException.getMessage());
            ioException.printStackTrace();
        }

        return null;
    }

    public static void closeBackup(BufferedWriter backupWriter) {
        if(backupWriter != null){
            try {
                backupWriter.close();
            } catch (IOException ioException) {
                pushErrorNotification("Close file", "Error closing file: "+ioException.getMessage());
                ioException.printStackTrace();
            }
        }
    }


    public static boolean isCSVFile(File file) {

        String extension = "";

        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }

        return extension.equalsIgnoreCase("csv");
    }


    public static String exportHTMLFile(String pathReport, List<Runner> listRace, boolean isByCatgory) throws IOException {


        String uniqueName = createUniqueNameFile(isByCatgory?REPORT_CATEGORY_PREFIX:REPORT_ALL_PREFIX, HTML_EXTENSION);


        Comparator<Runner> byTime = Comparator.comparing(Runner::getTime);
        listRace = listRace.stream().sorted(byTime).collect(Collectors.toList());

        try (BufferedWriter writer = createBufferWriter(pathReport, uniqueName)) {
            writer.write(HTML_HEADER);

            Map<String, List<Runner>> mapCategory;
            if (isByCatgory) {
                mapCategory = listRace.stream().collect(Collectors.groupingBy(raceResult -> raceResult.getCategory()));
            } else {
                mapCategory = new HashMap<>();
                mapCategory.put("aucune/geen", listRace);
            }
            Comparator<String> byCategory = Comparator.comparing(Runner::getCategoryScore);
            mapCategory.keySet().stream().sorted(byCategory).forEach(
                    category -> {
                        try {
                            writer.write("<h2> Catégorie/categorie " + Runner.getCategoryName(category) + "</h2>\n");
                            writer.write(TABLE_HEADER);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        AtomicInteger counter = new AtomicInteger(1);
                        mapCategory.get(category).stream().forEach(
                                raceResult -> {
                                    try {
                                        writer.write("<tr>\n" +
                                                "    <td>" + counter.getAndIncrement() + "</td>\n" +
                                                "    <td>" + raceResult.getId() + "</td>\n" +
                                                "    <td>" + raceResult.getFirstName() + " " + raceResult.getLastName() + "</td>\n" +
                                                "    <td>" + raceResult.getTime() + "</td>\n" +
                                                "    <td>" + raceResult.getClub() + "</td>\n" +
                                                "    <td>" + raceResult.getGender() + "</td>\n" +
                                                "  </tr>");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );

                        try {
                            writer.write(TABLE_FOOTER);
                            writer.write("<h3>Total/totaal: " + counter.decrementAndGet() + "</h3>\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

            );

            writer.write(HTML_FOOTER);
        }

        return uniqueName;
    }

}
