package model;

import javafx.scene.control.Label;
import util.RaceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import static gui.Main.LOAD_RUNNER;

public class Runner {

    private static final String UNKNOWN = "Unknown";
    private String id;
    private String lastName;
    private String firstName;
    private String gender;
    private String birthDate;
    private String age;
    private String category;
    private String club;
    private Boolean brChallenge;
    private LocalTime time;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss.SSS");

    public Runner(String id, String time){
        this.id=id;
        this.lastName = UNKNOWN;
        this.firstName = UNKNOWN;
        this.gender = UNKNOWN;
        this.birthDate = UNKNOWN;
        this.age = UNKNOWN;
        this.category = UNKNOWN;
        this.club = UNKNOWN;
        this.brChallenge = false;
        setTime(time);
    }


    public Runner(String id, String firstName, String lastName, String gender, String birthDate, String age, String category, String club, boolean brChallenge) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.age = age;
        this.category = category;
        this.club = club;
        this.brChallenge = brChallenge;
    }



    public LocalTime getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time= LocalTime.parse(time, formatter);
    }

    public static boolean loadRunnerCsvFile(File file, Map<String, Runner> mapIdRunner) {


        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
            if(!isHeaderColumnWithRightSize(reader.readLine())){
                return false;
            }

            Map<String, Runner> loadedRunners =
                    reader.lines()
                            .filter(line -> !line.startsWith(";"))
                            .map(Runner::parseRunner)
                            .collect(Collectors.toMap(raceReport -> raceReport.getId(), raceReport -> raceReport));

            if (loadedRunners != null && loadedRunners.size() > 0) {
                mapIdRunner.putAll(loadedRunners);
                return true;
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean loadBackupFile(File file, Map<String, String> mapIdTime) {


        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));

            Map<String, String> loadedRunners =
                    reader.lines()
                            .map(Runner::parseTime)
                            .collect(Collectors.toMap(runner -> runner.getId(), runner -> runner.getTime()));

            if (loadedRunners != null && loadedRunners.size() > 0) {
                mapIdTime.putAll(loadedRunners);
                return true;
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static RunnerTime parseTime(String line) {
        String[] values = line.split(";", -1);

        if (values.length == 2) {
            String id = values[0]; //442
            String time = values[1];

            return new RunnerTime(id, time);
        } else {
            RaceUtil.pushErrorNotification(LOAD_RUNNER,"Import runner " + line + " failed.");
            return null;
        }
    }

    private static boolean isHeaderColumnWithRightSize(String line) {
        String[] values = line.split(";", -1);
        return values.length >= 7;
    }

    private static Runner parseRunner(String line) {
        String[] values = line.split(";", -1);

        if (values.length >= 7) {
            String id = values[0]; //442
            String lastName = values[1];
            String firstName = values[2]; //PELLIZZARI  //IGOR
            String gender = values[4]; //M
            String birthDate = values[3]; //1970
            String age = values[5]; //48
            String category = values[6]; //V1
            String club = values[7];
            //String bxlChallenge = values[8];

            return new Runner(id, firstName, lastName, gender, birthDate, age, category, club, false);
        } else {
            RaceUtil.pushErrorNotification(LOAD_RUNNER, "Import runner " + line + " failed.");
            return null;
        }

    }

    public String getClub() {
        return club;
    }

    @Override
    public String toString() {
        return
                "id='" + id + '\'' +
                        ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' +
                        ", gender='" + gender + '\'' +
                        ", birthDate='" + birthDate + '\'' +
                        ", age='" + age + '\'' +
                        ", category='" + category + '\'' +
                        ", brChallenge='" + brChallenge + '\'' +
                        ", club='" + club + '\'';
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getId() {
        return id;
    }


    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getAge() {
        return age;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getBrChallenge() {
        return brChallenge;
    }

    public String getCategoryName() {
        return Runner.getCategoryName(this.category);
    }

    public static String getCategoryName(String category) {

        switch (category) {
            case "ED":
                return "ED - Espoirs dammes | Vrouwen hoop";
            case "EH":
                return "EH - Espoirs hommes | Mannen hoop";
            case "D":
                return "SD - Seniors dames | Vrouwen senior";
            case "S":
                return "SH - Seniors hommes | Mannen senior";
            case "A1":
                return "A1 - Aînées 1 | Oudste 1";
            case "V1":
                return "V1 - Vétérans 1 | Veteranen 1";
            case "A2":
                return "A2 - Aînées 2 | Oudste 2";
            case "V2":
                return "V2 - Vétérans 2 | Veteranen 2";
            case "A3":
                return "A3 - Aînées 3 | Oudste 3";
            case "V3":
                return "V3 - Vétérans 3 | Veteranen 3";
            case "A4":
                return "A4 - Aînées 4 | Oudste 4";
            case "V4":
                return "V4 - Vétérans 4 | Veteranen 4";
            default:
                return category;
        }

    }


    public static Integer getCategoryScore(String category) {

        switch (category) {
            case "ED":
                return 1;
            case "EH":
                return 2;
            case "D":
                return 3;
            case "S":
                return 4;
            case "A1":
                return 5;
            case "V1":
                return 6;
            case "A2":
                return 7;
            case "V2":
                return 8;
            case "A3":
                return 9;
            case "V3":
                return 10;
            case "A4":
                return 11;
            case "V4":
                return 12;
            default:
                return 13;
        }

    }

    public String getIsBruChallenge() {
        return this.brChallenge ? "Y" : "N";
    }

    public String getName() {
        return this.firstName + " "+this.lastName+" ("+this.id+") ";
    }
}
