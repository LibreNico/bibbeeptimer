import javax.sound.sampled.LineUnavailableException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class RaceTimer {


    public static void main(String[] args) throws IOException, LineUnavailableException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        String formatTime = LocalDateTime.now().format(formatter);

        Set<String> alreadyBeeped = new HashSet<>();

        File yourFile = new File("jracetimer_" + formatTime + ".csv");
        yourFile.createNewFile();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(yourFile.getPath()))) {
            System.out.println("Joggans - Laerbeek 2018 - Version 1.0.5");
            System.out.println("RaceTimer | Scan bib and record time to Excel");
            System.out.println("---------------------------------------------");
            System.out.println("Type 'start' + enter to begin.");
            System.out.println("Type 'quit' + enter to stop.");

            System.out.print("> ");
            Scanner scan = new Scanner(System.in);
            String triggerStart = scan.nextLine();

            if (triggerStart.equals("start")) {

                LocalTime startTime = LocalTime.now();
                outputData("ID;TIME;START;NOW;BIB\n", writer);
                while (true) {

                    System.out.print("> ");
                    String bibScan = scan.nextLine();

                    if (bibScan.length() < 2) {
                        RaceUtil.printError(new StringBuilder().append(bibScan).append(" is not long enough."));
                        continue;
                    }

                    if (bibScan.equals("quit")) {
                        break;
                    }

                    String number = bibScan.substring(0, bibScan.length() - 1);
                    if (!RaceUtil.isNumeric(number)) {
                        RaceUtil.printError(new StringBuilder().append(number).append(" is not numeric."));
                        continue;
                    }

                    if (alreadyBeeped.contains(number)) {
                        RaceUtil.printError(new StringBuilder().append(number).append(" already scan."));
                        continue;
                    }

                    LocalTime now = LocalTime.now();
                    String elapsedTime = elapsedTime(startTime, now);
                    StringBuilder inputScan = new StringBuilder()
                            .append(number).append(";")
                            .append(elapsedTime).append(";")
                            .append(startTime).append(";").append(now).append(";")
                            .append(bibScan).append(";").append("\n");

                    RaceUtil.printInfo(new StringBuilder().append("The bib ").append(number).append(" is scanned with a time of ").append(elapsedTime));

                    outputData(inputScan.toString(), writer);

                    alreadyBeeped.add(number);

                }
            }


        }
    }



    private static String elapsedTime(LocalTime timeStart, LocalTime timeNow) {

        long milliseconds = RaceUtil.getMillisecond(timeNow) - RaceUtil.getMillisecond(timeStart);

        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        long restMilliseconds = milliseconds - ((seconds * 1000) + (minutes * 1000 * 60) + (hours * 1000 * 60 * 60));

        return new StringBuilder()
                .append(RaceUtil.paddingLeftTwoZero(hours))
                .append(":").append(RaceUtil.paddingLeftTwoZero(minutes))
                .append(":").append(RaceUtil.paddingLeftTwoZero(seconds))
                .append(".").append(RaceUtil.paddingRightThreeZero(restMilliseconds))
                .toString();
    }


    private static void outputData(String inputScan, BufferedWriter writer) throws IOException, LineUnavailableException {

        writer.write(inputScan);
        writer.flush();

        //Toolkit.getDefaultToolkit().beep();
        RaceUtil.tone(1000, 100);

    }


}
