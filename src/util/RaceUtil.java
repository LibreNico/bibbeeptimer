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
    public static final String BACKUP_PREFIX = "/bb_backup_";
    private static final String HTML_EXTENSION = ".html";
    public static final String REPORT_ALL_PREFIX = "/bb_report";
    public static final String REPORT_CATEGORY_PREFIX = "/bb_reportByCategory";

    private static final String HTML_HEADER =
            "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>" +
                    "#result { font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100%; } " +
                    "#result td, #result th { border: 1px solid #ddd; padding: 3px; }" +
                    "#result tr:nth-child(even){background-color: #f2f2f2;}" +
                    "#result tr:hover {background-color: #ddd;}" +
                    "#result th { text-align: left; background-color: #1d1db9; color: white;}" +
                    "</style></head><body>";
    private static final String TABLE_HEADER = "<table id=\"result\"><tr>" +
            "<th width=\"7%\">Pos.</th>" +
            "<th width=\"7%\">Nr.</th>" +
            "<th width=\"30%\">Name</th>" +
            "<th width=\"20%\">Time</th>" +
            "<th width=\"26%\">Team</th>" +
            "<th width=\"6%\">Gender</th></tr>";
    private static final String HTML_FOOTER = "</body></html>";

    private static final String TABLE_FOOTER = "</table>";
    private static final String CREATE_FILE = "Create file";
    public static final String NO_CATGORY = "aucune/geen";

    public static float SAMPLE_RATE = 8000f;

    public static void tone(int hz, int msecs, double vol)
            throws LineUnavailableException {
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
        for (int i = 0; i < msecs * 8; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
            sdl.write(buf, 0, 1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    public static void tone(int hz, int msecs) {
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
        pushNotification(title, msg, true);
    }

    public static void pushInfoNotification(String title, String msg) {
        pushNotification(title, msg, false);
    }

    private static void pushNotification(String title, String msg, boolean isError) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formatTime = LocalDateTime.now().format(formatter);
        Main.TEXT_NOTIFICATION.setText((isError ? "[ERROR]" : "[INFO]") + "[" + title + " - " + formatTime + "] " + msg);
    }


    public static void backupData(String inputScan, BufferedWriter writer) {

        if (writer != null) {
            try {
                writer.write(inputScan + "\n");
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
        return prefix + formatTime + extension;
    }

    public static String today() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm");
        String formatTime = LocalDateTime.now().format(formatter);
        return formatTime;
    }

    public static BufferedWriter createBufferWriter(String backupPath, String uniqueName) {


        String pathname = backupPath + uniqueName;
        try {
            pushInfoNotification(CREATE_FILE, "Report/Backup file: " + pathname);
            File yourFile = new File(pathname);
            yourFile.createNewFile();

            return Files.newBufferedWriter(Paths.get(yourFile.getPath()));
        } catch (IOException ioException) {
            pushErrorNotification(CREATE_FILE, "Error create file: " + pathname + " because " + ioException.getMessage());
            ioException.printStackTrace();
        }

        return null;
    }

    public static void closeBackup(BufferedWriter backupWriter) {
        if (backupWriter != null) {
            try {
                backupWriter.close();
            } catch (IOException ioException) {
                pushErrorNotification("Close file", "Error closing file: " + ioException.getMessage());
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

    public static String exportHTMLReportByTimeAndCategory(String pathReport, List<Runner> listRace) throws IOException {
        return exportHTMLFile(pathReport, listRace, true);
    }

    public static String exportHTMLReportByTime(String pathReport, List<Runner> listRace) throws IOException {
        return exportHTMLFile(pathReport, listRace, false);
    }


    private static String exportHTMLFile(String pathReport, List<Runner> runners, boolean isByCatgory) throws IOException {


        StringBuilder htmlPage = new StringBuilder();
        htmlPage.append(HTML_HEADER);

        htmlPage.append("<h1>[BibBeep report] ").append(today()).append(" by the Joggans club.</h1>");

        Map<String, List<Runner>> runnersSorted = sortRunnersByTimeAndCotegory(runners, isByCatgory);

        runnersSorted.keySet().stream().sorted(Comparator.comparing(Runner::getCategoryScore)).forEach(
                category -> {
                    if (isByCatgory) {
                        htmlPage.append("<h2>Category: ").append(Runner.getCategoryName(category)).append("</h2>");
                    }
                    htmlPage.append(TABLE_HEADER);
                    AtomicInteger counter = new AtomicInteger(1);
                    runnersSorted.get(category).stream().forEach(
                            raceResult -> htmlPage.append("<tr>")
                                    .append("<td>").append(counter.getAndIncrement()).append("</td>")
                                    .append("<td>").append(raceResult.getId()).append("</td>")
                                    .append("<td>").append(raceResult.getFirstName()).append(" ").append(raceResult.getLastName()).append("</td>")
                                    .append("<td>").append(raceResult.getTime()).append("</td>")
                                    .append("<td>").append(raceResult.getClub()).append("</td>")
                                    .append("<td>").append(raceResult.getGender()).append("</td>")
                                    .append("</tr>")
                    );

                    htmlPage.append(TABLE_FOOTER);
                    htmlPage.append("<h3>Total/totaal: ").append(counter.decrementAndGet()).append("</h3>");

                }

        );

        htmlPage.append(HTML_FOOTER);

        return createReport(htmlPage.toString(), pathReport, isByCatgory);
    }

    private static String createReport(String htmlPage, String pathReport, boolean isByCatgory) {

        String uniqueName = createUniqueNameFile((isByCatgory ? REPORT_CATEGORY_PREFIX : REPORT_ALL_PREFIX), HTML_EXTENSION);
        try (BufferedWriter writer = createBufferWriter(pathReport, uniqueName)) {
            writer.write(htmlPage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uniqueName;
    }

    private static Map<String, List<Runner>> sortRunnersByTimeAndCotegory(List<Runner> runners, boolean isByCategory) {
        List<Runner> runnersSortByTime = runners.stream().sorted(Comparator.comparing(Runner::getTime)).collect(Collectors.toList());
        Map<String, List<Runner>> runnersGroupByCategory;
        if (isByCategory) {
            runnersGroupByCategory = runnersSortByTime.stream().collect(Collectors.groupingBy(raceResult -> raceResult.getCategory()));
        } else {
            runnersGroupByCategory = new HashMap<>();
            runnersGroupByCategory.put(NO_CATGORY, runnersSortByTime);
        }
        return runnersGroupByCategory;
    }

}
