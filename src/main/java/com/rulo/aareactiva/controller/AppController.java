package com.rulo.aareactiva.controller;

import com.rulo.aareactiva.domain.*;
import com.rulo.aareactiva.service.ChannelServiceImp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.schedulers.Schedulers;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class AppController implements Initializable {

    public Label lbNombreCanal, lbWebCanal, lbPaisCanal, lbTipoCanal, lbEmisionCanal;
    public ProgressIndicator piLoadMedia, piLoadChannels;
    public ListView<ChannelData> lvListaCanales;
    public ComboBox<Ambit> cbFiltro;
    public MediaView mvMultimedia;
    public WebView wvLogoCanal;

    private ChannelServiceImp canalesService;
    private ObservableList<Countrie> countries;
    private ObservableList<Ambit> ambits;
    private ObservableList<Channel> channels;
    private ObservableList<ChannelData> channelsData;

    private Media media;
    private MediaPlayer player;

    private File file;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        canalesService = new ChannelServiceImp();

        countries = FXCollections.observableArrayList();

        ambits = FXCollections.observableArrayList();
        cbFiltro.setItems(ambits);

        channels = FXCollections.observableArrayList();

        channelsData = FXCollections.observableArrayList();
        lvListaCanales.setItems(channelsData);

        piLoadMedia.setVisible(false);
        piLoadChannels.setVisible(false);
    }

    @FXML
    private void listarCanalesTV(ActionEvent event) {
        clearListView();
        piLoadChannels.setVisible(true);
        canalesService.getListaTV()
                .doOnCompleted(() -> piLoadChannels.setVisible(false))
                .doOnError(throwable -> System.out.println("[Error] " + throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(this::setChannelsData);
    }

    @FXML
    private void listarCanalesRadio(ActionEvent event) {
        clearListView();
        piLoadChannels.setVisible(true);
        canalesService.getListaRadios()
                .doOnCompleted(() -> piLoadChannels.setVisible(false))
                .doOnError(throwable -> System.out.println("[Error] " + throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(this::setChannelsData);
    }

    @FXML
    private void filtrarLista(ActionEvent event) {
        List<ChannelData> filteredChannels = new ArrayList<>(channelsData);
        channelsData.clear();

        filteredChannels = filteredChannels.stream()
                .filter(c -> c.getTipo().equals(cbFiltro.getSelectionModel().getSelectedItem().getName()))
                .collect(Collectors.toList());

        channelsData.addAll(filteredChannels);
    }

    @FXML
    private void seleccionarCanal(Event event) {
        ChannelData channel = lvListaCanales.getSelectionModel().getSelectedItem();
        if (channel == null)
            return;

        wvLogoCanal.getEngine().load(channel.getImagen());
        lbNombreCanal.setText(channel.getNombre());
        lbWebCanal.setText(channel.getSitio());
        lbPaisCanal.setText(channel.getPais());
        lbTipoCanal.setText(channel.getTipo());
        lbEmisionCanal.setText(channel.getRepro());

        if (!channel.getRepro().equals("Canal sin emisión"))
            setMedia(channel.getRepro());
    }

    @FXML
    private void exportarLista(ActionEvent event) {
        CompletableFuture.runAsync(() -> Platform.runLater(this::CSVExport));
    }

    @FXML
    private void exportarListaZIP(Event event) {
// --------- Así funciona bien ----------------------------------------------------------------//
//        File file = CSVExport();
//        CompletableFuture.supplyAsync(() -> file.getAbsolutePath().concat(".zip"))
//                .thenAccept(System.out::println)
//                .whenComplete((nada, error) -> Platform.runLater(() -> compressFile(file)));
// --------------------------------------------------------------------------------------------//

// --------- Así me tira un NullPointerException ----------------------------------------------//
//        CompletableFuture.supplyAsync(this::CSVExport)
//                .whenComplete((file, throwable) -> Platform.runLater(() -> compressFile(file)));
// --------------------------------------------------------------------------------------------//

// --------- Así no hace nada -----------------------------------------------------------------//
//        CompletableFuture.supplyAsync(this::CSVExport)
//                .whenComplete((file, throwable) -> {
//                    System.out.println(file.getAbsolutePath());
//                    Platform.runLater(() -> compressFile(file));
//                });
// --------------------------------------------------------------------------------------------//

        CompletableFuture.supplyAsync(() -> {
            Platform.runLater(this::CSVExport);
            return file;
        }).thenAccept(csvFile -> Platform.runLater(() -> compressFile(file)));
    }

    private void setChannelsData(TDT lista) {
        Platform.runLater(() -> {
            // Se rellena la lista de countries
            countries.addAll(Arrays.asList(lista.getCountries()));

            // Se rellena la lista de ambits
            for (Countrie country : countries)
                ambits.addAll(Arrays.asList(country.getAmbits()));

            // Se rellena la lista de channels
            for (Ambit ambit : ambits)
                channels.addAll(Arrays.asList(ambit.getChannels()));

            // Se rellena la lista de channnelsData
            for (Countrie country : countries) {
                for (int j = 0; j < country.getAmbits().length; j++) {
                    for (int k = 0; k < country.getAmbits()[j].getChannels().length; k++) {
                        ChannelData channelData = ChannelData.builder()
                                .pais(country.getName())
                                .tipo(country.getAmbits()[j].getName())
                                .nombre(country.getAmbits()[j].getChannels()[k].getName())
                                .imagen(country.getAmbits()[j].getChannels()[k].getLogo())
                                .sitio(country.getAmbits()[j].getChannels()[k].getWeb())
                                .repro(country.getAmbits()[j].getChannels()[k].getOptions().length > 0 ? country.getAmbits()[j].getChannels()[k].getOptions()[0].getUrl() : "Canal sin emisión")
                                .build();
                        channelsData.add(channelData);
                    }
                }
            }
        });
    }

    private void setMedia(String url) {
        piLoadMedia.setVisible(true);
        Thread thread = new Thread(() -> {
            if (mvMultimedia.getMediaPlayer() != null)
                player.stop();

            media = new Media(url);

            player = new MediaPlayer(media);
            player.setOnReady(() -> piLoadMedia.setVisible(false));
            player.setAutoPlay(true);

            mvMultimedia.setMediaPlayer(player);
        });
        thread.start();
    }

    private void CSVExport() {
        if (channelsData.size() == 0)
            return;

        FileChooser fileChooser = new FileChooser();
        File fichero = fileChooser.showSaveDialog(null);

        if (fichero == null)
            return;

        try {
            FileWriter fileWriter = new FileWriter(fichero + ".csv");
            CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            for (ChannelData channel : channelsData) {
                printer.printRecord(
                        channel.getNombre(),
                        channel.getSitio(),
                        channel.getRepro()
                );
            }

            printer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        file = fichero;
    }

    private void compressFile(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().concat(".zip"));
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath().concat(".csv"));
            ZipEntry zipEntry = new ZipEntry(file.getName().concat(".csv"));

            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >=0){
                zos.write(bytes, 0, length);
            }
            zos.close();
            fis.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearListView() {
        lvListaCanales.getItems().clear();
        countries.clear();
        ambits.clear();
        channels.clear();
        channelsData.clear();
    }

}