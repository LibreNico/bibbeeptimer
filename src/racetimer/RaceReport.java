package racetimer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RaceReport {
    private static final String HTML_HEADER = "";
          /* "<!DOCTYPE html>\n" +
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
            "<body>\n" ;*/
    private static final String TABLE_HEADER = "<table>\n" +
            "  <tr>\n" +
            "    <th width=\"7%\">Pos.</th>\n" +
            "    <th width=\"7%\">Num.</th>\n" +
            "    <th width=\"30%\">Nom|Naam</th>\n" +
            "    <th width=\"20%\">Temps|Tijd</th>\n" +
            "    <th width=\"26%\">Club</th>\n" +
            "    <th width=\"6%\">Gender</th>\n" +
            "  </tr>";
    private static final String HTML_FOOTER = "";
            /*"</body>\n" +
            "</html>";*/
    private static final String TABLE_FOOTER = "</table>\n";
    private static Map<String, RaceRegister> mapRegistration;

    //camréo go pro ipv crieur
    // grand chrono >> tablet

    //manque un laptop


    //read subcription.csv
    // new Subcription(id, first, lastname, bithyear, group )
    // subcription.put(id , ractimer)


    public static void main(String[] args) throws IOException {

        loadRegistrationsInMemory();
        loadSortAnndOutputResults();

    }

    private static void loadRegistrationsInMemory() throws IOException {
        mapRegistration = Files.list(Paths.get(".")).map(String::valueOf)
                .filter(path -> path.startsWith("./inscription_"))
                .flatMap(path -> {

                    try {
                        BufferedReader reader = Files.newBufferedReader(Paths.get(path));
                        reader.readLine(); // this will read the first line
                        return reader.lines().map(RaceReport::toRegister);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toMap(raceReport -> raceReport.getId(), raceReport -> raceReport));
    }

    private static void loadSortAnndOutputResults() throws IOException {
        String pathResult = Files.list(Paths.get(".")).map(String::valueOf)
                .filter(path -> path.startsWith("./jracetime"))
                .sorted().reduce((a, b) -> b)
                .orElse(null);

        System.out.println("pathResult"+pathResult);

        BufferedReader reader = Files.newBufferedReader(Paths.get(pathResult));
        reader.readLine();


        List<RaceResult>  listRace = reader
                .lines()
                .map(RaceReport::toResult)
                .peek(raceResult -> raceResult.setPerson(mapRegistration.get(raceResult.getId())))
                .filter(raceResult -> raceResult.getPerson() != null)
                .collect(Collectors.toList());

        //Comparator<RaceResult> byCategory = Comparator.comparing(o -> o.getScore());
        Comparator<RaceResult> byTime = Comparator.comparing(RaceResult::getTime);


      //  listRace = listRace.stream().sorted(byCategory.thenComparing(byTime)).collect(Collectors.toList());

        listRace = listRace.stream().sorted(byTime).collect(Collectors.toList());

        String pathName = exportHTMLFile(listRace, "result_bycatgeory_", true);
        RaceUtil.printInfo(new StringBuilder().append("Race report by category is generated : ").append(pathName));

        exportHTMLFile(listRace, "result_all_", false);
        RaceUtil.printInfo(new StringBuilder().append("Race report general is generated : ").append(pathName));


    }


    private static String exportHTMLFile(List<RaceResult> listRace, String title, boolean isByCatgory) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String formatTime = dateTime.format(formatter);

        String pathname = title + formatTime + ".html";
        File yourFile = new File(pathname);
        yourFile.createNewFile();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(yourFile.getPath()))) {
            writer.write(HTML_HEADER);
            //writer.write("<h1 style=\"color:#1279be;\">Joggans - Sentiers du Laerbeek / Laarbeeksepaden "+dateTime.getYear()+"</h1>");

            Map<String, List<RaceResult>> mapCategory;
           if(isByCatgory){
               mapCategory = listRace.stream().collect(Collectors.groupingBy(raceResult -> raceResult.getPerson().getCategory()));
           }else{
               mapCategory = new HashMap<>();
               mapCategory.put("aucune/geen", listRace);
           }
            Comparator<String> byCategory = Comparator.comparing(RaceRegister::getCategoryScore);
            mapCategory.keySet().stream().sorted(byCategory).forEach(
                    category -> {
                        try {
                            writer.write("<h2> Catégorie/categorie "+RaceRegister.getCategoryName(category)+"</h2>\n");
                            writer.write(TABLE_HEADER);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        AtomicInteger counter = new AtomicInteger(1);
                        mapCategory.get(category).stream().forEach(
                                raceResult -> {
                                    try {
                                        writer.write("<tr>\n" +
                                                "    <td>" +counter.getAndIncrement()+"</td>\n" +
                                                "    <td>" +raceResult.getId()+"</td>\n" +
                                                "    <td>"+raceResult.getPerson().getName()+"</td>\n" +
                                                "    <td>"+raceResult.getTime()+"</td>\n" +
                                                "    <td>"+raceResult.getPerson().getClub()+"</td>\n" +
                                               "    <td>"+raceResult.getPerson().getGender()+"</td>\n" +
                                                "  </tr>");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );

                        try {
                            writer.write(TABLE_FOOTER);
                            writer.write("<h3>Total/totaal: "+counter.decrementAndGet()+"</h3>\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

            );

            writer.write(HTML_FOOTER);
        }

        return pathname;
    }


    private static RaceResult toResult(String line) {
        String[] values = line.split(";");

       // System.out.println(line);
       // System.out.println(values.length);
        if (values.length >= 2) {
            return new RaceResult(values[0], values[1]);
        }
        //error
        RaceUtil.printError(new StringBuilder().append("#toResult ").append(line).append(" is not accepted."));
        return null;
    }

    private static RaceRegister toRegister(String line) {
        String[] values = line.split(";", -1);

       // System.out.println(line);
       // System.out.println(values.length);


        if (values.length >= 7) {
            String id = values[0]; //442
            String name = values[1] + " " + values[2]; //PELLIZZARI  //IGOR
            String gender = values[4]; //M
            String birthDate = values[3]; //1970
            String age = values[5]; //48
            String category = values[6]; //V1
            String club = values[7];
          //  String bxlChallenge = values[8];

            return new RaceRegister(id, name, gender, birthDate, age, category, club, "");
        }

        RaceUtil.printError(new StringBuilder().append("#toRegister ").append(line).append(" is not accepted."));
        return null;

    }


}
