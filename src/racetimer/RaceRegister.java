
public class RaceRegister {

    private final String id;
    private final String name;
    private final String gender;
    private final String birthDate;
    private final String age;
    private final String category;
    private final String club;
    private final Boolean brChallenge;

    public RaceRegister(String id, String name, String gender, String birthDate, String age, String category, String club, String brChallenge) {
        this.id=id;
        this.name=name;
        this.gender=gender;
        this.birthDate=birthDate;
        this.age=age;
        this.category=category;
        this.club=club;
        this.brChallenge = brChallenge.length()>0;
    }

    public String getClub() {
        return club;
    }

    @Override
    public String toString() {
        return
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", age='" + age + '\'' +
                ", category='" + category + '\''+
                ", brChallenge='" + brChallenge + '\''+
        ", club='" + club + '\'';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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
        return RaceRegister.getCategoryName(this.category);
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
                return  category;
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
                return  13;
        }

    }

    public String getIsBruChallenge() {
        return this.brChallenge ? "Y" : "N";
    }
}
