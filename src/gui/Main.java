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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Runner;
import util.RaceUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends Application {

    public static final String INIT_STOPWACHT = "00:00:00";
    public static final String APP_TITLE = "BibBeep - run race timer by joggans.be";
    private static final String COPYLEFT = "Bib scanning and timing software made by the joggans.be club";
    public static final String LABEL_RUNNERS = "Imported runners: ";
    public static final String LABEL_BACKUP = "Backup folder: ";
    private int mins = 0;
    private int secs = 0;
    private int millis = 0;
    private Label stopwatchLabel;
    private Timeline timeline;
    private final FileChooser fileChooser = new FileChooser();
    private static StringBuilder inputKeyBordBuffer = new StringBuilder();
    private ListView<Object> listFinisher;
    Set<String> alreadyBeeped = new HashSet<>();
    private static Map<String, Runner> mapIdRunner = new HashMap<>();
    private static Map<String, String> mapIdTime = new HashMap<>();

    private boolean hasRaceStarted;
    private BufferedWriter backupWriter;
    private Label importedRunnersLabel;
    private Label backupFolderLabel;
    private javafx.scene.control.CheckBox removeLastDigitLabel;
    private String backupPath = System.getProperty("user.home");
    private String uniqueNameFile;


    @Override
    public void start(Stage stage) {

        BorderPane bPane = new BorderPane();
        bPane.setTop(createHeader(stage));
        //bPane.setBottom(createAbout());
        //bPane.setLeft(new TextField("Left"));
        //bPane.setRight(new TextField("Right"));
        bPane.setCenter(createFinisherList());


        //Creating a scene object
        Scene scene = new Scene(bPane);

        handelKeyBord(scene);

        //Setting title to the Stage
        stage.setTitle(APP_TITLE);

        //Adding scene to the stage
        stage.setScene(scene);

        stage.setMinWidth(700);
        stage.setMinHeight(500);

        //Displaying the contents of the stage
        stage.show();


    }

    private Node createHeader(Stage stage) {

        //Instantiating the VBox class
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);

        //retrieving the observable list of the VBox
        ObservableList list = vBox.getChildren();

        //Adding all the nodes to the observable list
        list.addAll(createHeaderButtons(stage), createStopWatch(), createInfoBox());

        return vBox;
    }

    private Node createInfoBox() {
        HBox infoBox = new HBox();
        infoBox.setSpacing(5);

        ObservableList list = infoBox.getChildren();

        importedRunnersLabel = new Label(LABEL_RUNNERS + mapIdRunner.size());
        backupFolderLabel = new Label(LABEL_BACKUP + backupPath);
        removeLastDigitLabel = new javafx.scene.control.CheckBox("take last digit?");

       // infoBox.setMargin(removeLastDigitLabel, new Insets(0, 0, 0, 10));

        list.addAll(removeLastDigitLabel, new Label(" | "), importedRunnersLabel , new Label(" | "), backupFolderLabel);
        return infoBox;
    }

    private Node createAbout() {
        HBox aboutBar = new HBox();
        /*ImageView imageCopyleft = new ImageView(new Image(getClass().getResourceAsStream("icons/copyleft.png")));
        imageCopyleft.setFitHeight(12);
        imageCopyleft.setFitWidth(12);*/

        Label copyleft = new Label(COPYLEFT);
        aboutBar.setSpacing(5);
        aboutBar.getChildren().addAll(/*imageCopyleft,*/ copyleft);
        return aboutBar;
    }

    private void handelKeyBord(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {

            if (hasRaceStarted) {

                if (key.getCode() == KeyCode.ENTER) {
                    String bib = inputKeyBordBuffer.toString();


                    if ((!removeLastDigitLabel.isSelected() && bib.length() < 2)
                            || (removeLastDigitLabel.isSelected() && bib.length() < 1)) {
                        RaceUtil.printError("Scanned input " + bib + " is not long enough.");
                    }else if (!RaceUtil.isNumeric(bib)) {
                        RaceUtil.printError("Scanned input " + bib + " is not numeric.");
                    } else if (alreadyBeeped.contains(bib)) {
                        RaceUtil.printError("Scanned input " + bib + " already scan.");
                    } else {

                        if(!removeLastDigitLabel.isSelected()){
                            bib = bib.substring(0, bib.length() - 1);
                        }

                        String time = stopwatchLabel.getText();
                        mapIdTime.put(bib, time);
                        updateListFinisher(bib, time);
                        alreadyBeeped.add(bib);
                        String data = bib + ";" + time;
                        RaceUtil.printInfo("Bib scan: " + data);
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
            RaceUtil.printError("Runner with bib " + bib + " not found!");
        }
    }



    private Label createStopWatch() {
        //https://gist.github.com/SatyaSnehith/167779aac353b4e79f8dfae4ed23cb85
        stopwatchLabel = new Label(INIT_STOPWACHT);
        stopwatchLabel.setFont(Font.font("Verdana", 35));
        timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> change(stopwatchLabel)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        return stopwatchLabel;
    }

    void change(Label text) {
        if (millis == 1000) {
            secs++;
            millis = 0;
        }
        if (secs == 60) {
            mins++;
            secs = 0;
        }
        text.setText((((mins / 10) == 0) ? "0" : "") + mins + ":"
                + (((secs / 10) == 0) ? "0" : "") + secs + ":"
                + (((millis / 10) == 0) ? "00" : (((millis / 100) == 0) ? "0" : "")) + millis++);
    }


    private Node createFinisherList() {
        listFinisher = new ListView<>();
        listFinisher.setPrefWidth(100);
        listFinisher.setPrefHeight(100);
        return listFinisher;
    }

    private Parent createHeaderButtons(Stage stage) {
        //Creating button1
        Button buttonStartStop = new Button("Start");

        ImageView imageStart = new ImageView(new Image(getClass().getResourceAsStream("icons/media-play-outline.png")));
        ImageView imageStop = new ImageView(new Image(getClass().getResourceAsStream("icons/media-stop-outline.png")));
        buttonStartStop.setGraphic(imageStart);


        buttonStartStop.setOnAction(e -> {
            if (buttonStartStop.getText().equals("Start")) {
                hasRaceStarted = true;
                buttonStartStop.setText("Stop");
                buttonStartStop.setGraphic(imageStop);
                listFinisher.getItems().clear();
                timeline.play();
                uniqueNameFile = RaceUtil.createUniqueNameFile();
                backupWriter = RaceUtil.createBackup(backupPath, uniqueNameFile);

            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText("Stop the race?");
                alert.setContentText("Backup store in " + backupPath + uniqueNameFile);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    hasRaceStarted = false;
                    buttonStartStop.setText("Start");
                    buttonStartStop.setGraphic(imageStart);
                    timeline.pause();
                    stopwatchLabel.setText(INIT_STOPWACHT);
                    alreadyBeeped.clear();
                    RaceUtil.closeBackup(backupWriter);

                }

            }
        });


        //Creating button2
        Button buttonAddRunners = new Button("Runner");
        Image image2 = new Image(getClass().getResourceAsStream("icons/plus-outline.png"));
        buttonAddRunners.setGraphic(new ImageView(image2));


        buttonAddRunners.setOnAction(
                e -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {

                        if (isCSVFile(file)) {


                            if (Runner.loadRunnerCsvFile(file, mapIdRunner)) {

                                importedRunnersLabel.setText(LABEL_RUNNERS + mapIdRunner.size());

                            } else {

                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error Dialog");
                                alert.setContentText("Parsing error while reading: " + file.getName());
                                alert.showAndWait();

                            }

                        } else {

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error Dialog");
                            alert.setContentText("The selected file is not CSV format: " + file.getName());
                            alert.showAndWait();

                        }

                    }
                });

        //Creating button3
        Button buttonReport = new Button("Report");
        Image image3 = new Image(getClass().getResourceAsStream("icons/printer.png"));
        buttonReport.setGraphic(new ImageView(image3));


        Button buttonSetBackup = new Button("Backup");
        ImageView editImageView = new ImageView(new Image(getClass().getResourceAsStream("icons/folder.png")));
        buttonSetBackup.setGraphic(editImageView);


        buttonSetBackup.setOnAction(
                e -> {

                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Select backup folder");
                    File defaultDirectory = new File(backupPath);
                    chooser.setInitialDirectory(defaultDirectory);
                    File selectedDirectory = chooser.showDialog(stage);
                    if (selectedDirectory != null) {
                        backupPath = selectedDirectory.getAbsolutePath();
                        backupFolderLabel.setText(LABEL_BACKUP + backupPath);
                    }

                });


        //Creating button4
        Button buttonLoadBackup = new Button("Load");
        Image image5 = new Image(getClass().getResourceAsStream("icons/download-outline.png"));
        buttonLoadBackup.setGraphic(new ImageView(image5));

        buttonLoadBackup.setOnAction(
                e -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {

                        if (isCSVFile(file)) {

                            if (Runner.loadBackupFile(file, mapIdTime)) {
                                listFinisher.getItems().clear();
                                mapIdTime.forEach((bib, time) -> updateListFinisher(bib, time));
                            } else {

                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error Dialog");
                                alert.setContentText("Parsing error while reading: " + file.getName());
                                alert.showAndWait();

                            }

                        } else {

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error Dialog");
                            alert.setContentText("The selected file is not CSV format: " + file.getName());
                            alert.showAndWait();

                        }

                    }
                });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(5);

        ObservableList list = buttonBox.getChildren();
        buttonBox.setMargin(buttonStartStop, new Insets(10, 0, 10, 10));

        list.addAll(buttonStartStop, buttonAddRunners, buttonReport, buttonSetBackup, buttonLoadBackup);

        return buttonBox;
    }



    private boolean isCSVFile(File file) {

        String extension = "";

        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }

        return extension.equalsIgnoreCase("csv");
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }


    public static void main(String[] args) {
        launch(args);
    }
}
