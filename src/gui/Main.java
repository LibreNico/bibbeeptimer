package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Runner;
import util.RaceUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static util.RaceUtil.BACKUP_PREFIX;
import static util.RaceUtil.CSV_EXTENSION;

//TODO need a refactoring too long and not MVC!
public class Main extends Application {

    public static final String INIT_TIMER = "00:00:00.000";
    public static final String APP_TITLE = "BibBeep - run race timer by joggans.be";
    public static final String LABEL_RUNNERS = "Imported runners ";
    public static final String LABEL_BACKUP = "Backup/report folder";
    public static final String LOAD_BACKUP = "Load backup";
    public static final String LOAD_RUNNER = "Load runners";
    public static final String REPORT_TITLE = "Report";
    private static final String BIB_SCANNING_TITLE = "Bib scanning";
    private static final String LABEL_WITH_CHECK_DIGIT = " with last (check) digit?";

    //TODO make a model
    Set<String> alreadyBeeped = new HashSet<>();
    private static Map<String, Runner> mapIdRunner = new HashMap<>();
    private static Map<String, String> mapIdTime = new HashMap<>();
    private boolean hasRaceStarted;
    private int mins = 0;
    private int secs = 0;
    private int hours = 0;
    private int millis = 0;

    private Timeline timeline;
    private final FileChooser fileChooser = new FileChooser();
    private static StringBuilder inputKeyBordBuffer = new StringBuilder();
    private ListView<Object> listFinisher;
    private BufferedWriter backupWriter;
    private Label timerLabel;
    private Label importedRunnersLabel;
    private CheckBox removeLastDigitLabel;
    private String backupPath = System.getProperty("user.home");
    private String uniqueNameFile;
    public static final Label TEXT_NOTIFICATION = new Label("Here will come notifications...");


    @Override
    public void start(Stage stage) {

        BorderPane bPane = new BorderPane();
        bPane.setTop(createHeader(stage));
        bPane.setCenter(createFinisherList());


        Scene scene = new Scene(bPane);
        handelKeyBord(scene);

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);

        stage.setMinWidth(700);
        stage.setMinHeight(500);

        stage.show();

    }

    private Node createHeader(Stage stage) {

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);

        ObservableList list = vBox.getChildren();
        list.addAll(createHeaderButtons(stage), createStopWatch(), createInfoBox(), createNotificationBox());

        return vBox;
    }

    private Node createNotificationBox() {
        HBox infoBox = new HBox();
        infoBox.setSpacing(5);
        infoBox.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        ObservableList list = infoBox.getChildren();

        ImageView imageNotif = new ImageView(new Image(getClass().getResourceAsStream("icons/bell-o.png")));


        list.addAll(imageNotif, TEXT_NOTIFICATION);
        return infoBox;
    }

    private Node createInfoBox() {
        HBox infoBox = new HBox();
        infoBox.setSpacing(5);

        ObservableList list = infoBox.getChildren();

        importedRunnersLabel = new Label(LABEL_RUNNERS + mapIdRunner.size() + LABEL_WITH_CHECK_DIGIT);
        RaceUtil.pushInfoNotification(LABEL_BACKUP, backupPath);
        removeLastDigitLabel = new CheckBox();
        removeLastDigitLabel.setSelected(true);
        removeLastDigitLabel.setTooltip(new Tooltip("The scanning system take all digit from the bib or remove the last one?"));

        list.addAll(importedRunnersLabel ,removeLastDigitLabel);
        return infoBox;
    }

    private void handelKeyBord(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {

            if (hasRaceStarted) {

                if (key.getCode() == KeyCode.ENTER) {
                    String bib = inputKeyBordBuffer.toString();


                    if ((!removeLastDigitLabel.isSelected() && bib.length() < 2)
                            || (removeLastDigitLabel.isSelected() && bib.length() < 1)) {
                        RaceUtil.pushErrorNotification(BIB_SCANNING_TITLE, "Scanned input " + bib + " is not long enough.");
                    }else if (!RaceUtil.isNumeric(bib)) {
                        RaceUtil.pushErrorNotification(BIB_SCANNING_TITLE,"Scanned input " + bib + " is not numeric.");
                    } else if (alreadyBeeped.contains(bib)) {
                        RaceUtil.pushErrorNotification(BIB_SCANNING_TITLE, "Scanned input " + bib + " already scan.");
                    } else {

                        if(!removeLastDigitLabel.isSelected()){
                            bib = bib.substring(0, bib.length() - 1);
                        }

                        String time = timerLabel.getText();
                        mapIdTime.put(bib, time);
                        updateListFinisher(bib, time);
                        alreadyBeeped.add(bib);
                        String data = bib + ";" + time;
                        //RaceUtil.printInfo(BIB_SCANNING_TITLE, "Bib scan: " + data);
                        RaceUtil.backupData(data, backupWriter);
                    }

                    inputKeyBordBuffer.setLength(0);


                } else {
                    inputKeyBordBuffer.append(key.getText());
                }

            }
        });
    }

    private void updateListFinisher(String bib, String time) {
        if (mapIdRunner.containsKey(bib)) {
            listFinisher.getItems().add(mapIdRunner.get(bib).getName() + " - " + time);
        } else {
            listFinisher.getItems().add(bib + " - " + time);
            RaceUtil.pushErrorNotification(LOAD_RUNNER,"Runner with bib " + bib + " not found!");
        }
    }



    private Label createStopWatch() {
        //https://gist.github.com/SatyaSnehith/167779aac353b4e79f8dfae4ed23cb85
        timerLabel = new Label(INIT_TIMER);
        timerLabel.setFont(Font.font("Verdana", 35));
        timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> change(timerLabel)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        return timerLabel;
    }

    private void change(Label text) {
        if (millis == 1000) {
            secs++;
            millis = 0;
        }
        if (secs == 60) {
            mins++;
            secs = 0;
        }

        if(mins==60){
            hours++;
            mins=0;
        }

        text.setText(
                  (((hours / 10) == 0) ? "0" : "") + hours + ":"
                + (((mins / 10) == 0) ? "0" : "") + mins + ":"
                + (((secs / 10) == 0) ? "0" : "") + secs + "."
                + (((millis / 10) == 0) ? "00" : (((millis / 100) == 0) ? "0" : "")) + millis++
        );
    }


    private Node createFinisherList() {
        listFinisher = new ListView<>();
        listFinisher.setPrefWidth(100);
        listFinisher.setPrefHeight(100);
        return listFinisher;
    }

    private Parent createHeaderButtons(Stage stage) {
        Button buttonStartStop = new Button("Start");

        ImageView imageStart = new ImageView(new Image(getClass().getResourceAsStream("icons/play.png")));
        ImageView imageStop = new ImageView(new Image(getClass().getResourceAsStream("icons/stop.png")));
        buttonStartStop.setGraphic(imageStart);


        buttonStartStop.setOnAction(e -> {
            if (buttonStartStop.getText().equals("Start")) {
                hasRaceStarted = true;
                buttonStartStop.setText("Stop");
                buttonStartStop.setGraphic(imageStop);
                listFinisher.getItems().clear();
                timeline.play();
                uniqueNameFile = RaceUtil.createUniqueNameFile(BACKUP_PREFIX, CSV_EXTENSION);
                backupWriter = RaceUtil.createBufferWriter(backupPath, uniqueNameFile);

            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation stop");
                alert.setHeaderText("Stop the race?");
                //alert.setContentText();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    hasRaceStarted = false;
                    buttonStartStop.setText("Start");
                    buttonStartStop.setGraphic(imageStart);
                    timeline.pause();
                    timerLabel.setText(INIT_TIMER);
                    alreadyBeeped.clear();
                    RaceUtil.closeBackup(backupWriter);
                    RaceUtil.pushInfoNotification("Race stopped", "Backup store in " + backupPath + uniqueNameFile);

                }

            }
        });

        Button buttonAddRunners = new Button("Runner");
        Image image2 = new Image(getClass().getResourceAsStream("icons/plus.png"));
        buttonAddRunners.setGraphic(new ImageView(image2));
        buttonAddRunners.setTooltip(new Tooltip("Import runners from csv file (template in the documentation)."));


        buttonAddRunners.setOnAction(
                e -> {
                    fileChooser.setTitle(LOAD_RUNNER);
                    fileChooser.setInitialDirectory(new File(backupPath));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        if (RaceUtil.isCSVFile(file)) {

                            if (Runner.loadRunnerCsvFile(file, mapIdRunner)) {
                                importedRunnersLabel.setText(LABEL_RUNNERS + mapIdRunner.size() + LABEL_WITH_CHECK_DIGIT);
                            } else {
                                RaceUtil.pushErrorNotification(LOAD_RUNNER, "Parsing error while reading: " + file.getName());
                            }

                        } else {
                            RaceUtil.pushErrorNotification(LOAD_RUNNER, "The selected file is not CSV format: " + file.getName());
                        }

                    }
         });

        Button buttonReport = new Button(REPORT_TITLE);
        Image imagePrint = new Image(getClass().getResourceAsStream("icons/print.png"));
        buttonReport.setGraphic(new ImageView(imagePrint));
        buttonReport.setTooltip(new Tooltip("Export as HTML the ranking (with and without category) of the runners."));

        buttonReport.setOnAction(e -> {

            List<Runner> listRace = new ArrayList<>();
            for(String bib: mapIdTime.keySet()){

                Runner runner;
                if(mapIdRunner.containsKey(bib)){
                    runner = mapIdRunner.get(bib);
                    runner.setTime(mapIdTime.get(bib));

                }else{
                    RaceUtil.pushErrorNotification(REPORT_TITLE,"Runner with bib " + bib + " not found!");
                    runner = new Runner(bib, mapIdTime.get(bib));
                }

                listRace.add(runner);

            }

            try {
                String reportByCat = RaceUtil.exportHTMLFile(backupPath, listRace, true);
                String reportAll = RaceUtil.exportHTMLFile(backupPath, listRace, false);
                RaceUtil.pushInfoNotification("Report created", reportByCat+" and "+reportAll + " in "+backupPath);
            } catch (IOException e1) {
                RaceUtil.pushErrorNotification(REPORT_TITLE, "Export report failde: " + e1.getMessage());
                e1.printStackTrace();
            }
        });


        Button buttonSetBackup = new Button("Set folder");
        ImageView editImageView = new ImageView(new Image(getClass().getResourceAsStream("icons/folder-open-o.png")));
        buttonSetBackup.setGraphic(editImageView);
        buttonSetBackup.setTooltip(new Tooltip("Set the folder where backups and reports will be store (by default the home folder)."));


        buttonSetBackup.setOnAction(
                e -> {

                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Select export folder");
                    File defaultDirectory = new File(backupPath);
                    chooser.setInitialDirectory(defaultDirectory);
                    File selectedDirectory = chooser.showDialog(stage);
                    if (selectedDirectory != null) {
                        backupPath = selectedDirectory.getAbsolutePath();
                        RaceUtil.pushInfoNotification(LABEL_BACKUP,  backupPath);
                    }

                });

        Button buttonLoadBackup = new Button("Backup");
        Image imageInbox = new Image(getClass().getResourceAsStream("icons/inbox.png"));
        buttonLoadBackup.setGraphic(new ImageView(imageInbox));
        buttonLoadBackup.setTooltip(new Tooltip("Load a previous backup."));

        buttonLoadBackup.setOnAction(
                e -> {
                    fileChooser.setTitle(LOAD_BACKUP);
                    fileChooser.setInitialDirectory(new File(backupPath));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {

                        if (RaceUtil.isCSVFile(file)) {

                            if (Runner.loadBackupFile(file, mapIdTime)) {
                                listFinisher.getItems().clear();
                                mapIdTime.forEach(this::updateListFinisher);
                            } else {
                                RaceUtil.pushErrorNotification(LOAD_BACKUP,"Parsing error while reading: " + file.getName());
                            }

                        } else {
                            RaceUtil.pushErrorNotification(LOAD_BACKUP,"The selected file is not CSV format: " + file.getName());
                        }

                    }
                });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(5);

        ObservableList list = buttonBox.getChildren();
        buttonBox.setMargin(buttonStartStop, new Insets(10, 0, 10, 10));

        list.addAll(buttonStartStop, buttonAddRunners, buttonSetBackup, buttonReport, buttonLoadBackup);

        return buttonBox;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
