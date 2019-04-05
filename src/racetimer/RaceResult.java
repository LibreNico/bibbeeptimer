

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RaceResult {

    private final String id;
    private final LocalTime time;
    private RaceRegister person;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss.SSS");


    public RaceResult(String id, String time) {
        this.id=id;
        this.time= LocalTime.parse(time, formatter);
    }

    public String getId() {
        return id;
    }

    public void setPerson(RaceRegister person) {
        if(person == null){
            RaceUtil.printError(new StringBuilder()
                    .append("#setPerson ")
                    .append(this.toString())
                    .append(" has no person."));
        }

        this.person = person;
    }

    public LocalTime getTime() {
        return time;
    }

    public RaceRegister getPerson() {
        return person;
    }



    @Override
    public String toString() {
        return "RaceResult:" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", person={" + person +
                '}';
    }


}
