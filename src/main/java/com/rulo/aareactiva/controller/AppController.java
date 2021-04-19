package com.rulo.aareactiva.controller;

import com.rulo.aareactiva.domain.*;
import com.rulo.aareactiva.service.CanalesServiceImp;
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

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class AppController implements Initializable {

    public ComboBox<Ambits> cbFiltro;
    public ListView<ChannelData> lvListaCanales;
    public WebView wvLogoCanal;
    public Label lbNombreCanal;
    public Label lbWebCanal;
    public Label lbPaisCanal;
    public Label lbTipoCanal;
    public Label lbEmisionCanal;
    public MediaView mvMultimedia;
    public ProgressIndicator piLoadMedia;
    public ProgressIndicator piLoadChannels;

    private CanalesServiceImp canalesService;
    private ObservableList<Countries> countries;
    private ObservableList<Ambits> ambits;
    private ObservableList<Channels> channels;
    private ObservableList<ChannelData> channelsData;

    private Media media;
    private MediaPlayer player;

    private File file;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        canalesService = new CanalesServiceImp();

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
        canalesService.getListasTV()
                .doOnCompleted(() -> piLoadChannels.setVisible(false))
                .doOnError(throwable -> System.out.println("[Error] " + throwable.getMessage()))
                .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                .subscribe(this::setChannelsData);
    }

    @FXML
    private void listarCanalesRadio(ActionEvent event) {
        clearListView();
        piLoadChannels.setVisible(true);
        canalesService.getListasRadio()
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

    @FXML
    private void exportarLista(ActionEvent event) {
        CompletableFuture.runAsync(() -> {
            Platform.runLater(() -> {
                try {
                    CSVExport();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });
    }

//    @FXML
//    private void exportarListaZIP(ActionEvent event) {
//        CompletableFuture.supplyAsync(() -> {
//            Platform.runLater(() -> {
//                try {
//                    CSVExport();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            return file;
//        })
//                .thenAcceptAsync(this::CSVCompress);
//    }

    @FXML
    private void exportarListaZIP(ActionEvent event) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return CSVExport();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).thenAccept(System.out::println).whenComplete((a, b) -> System.out.println("Fin"));
    }

    private void clearListView() {
        lvListaCanales.getItems().clear();
        countries.clear();
        ambits.clear();
        channels.clear();
        channelsData.clear();
    }

    private void setChannelsData(TDT lista) {
        Platform.runLater(() -> {
            countries.addAll(Arrays.asList(lista.getCountries()));
            for (Countries country : countries) {
                ambits.addAll(Arrays.asList(country.getAmbits()));
            }
            for (Ambits ambit : ambits) {
                channels.addAll(Arrays.asList(ambit.getChannels()));
            }

            for (Countries country : countries) {
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

    private int cuenta() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 9;
    }

    private File CSVExport() throws Exception {
        if (channelsData.size() == 0)
            return null;

        FileChooser fileChooser = new FileChooser();
        File fichero = fileChooser.showSaveDialog(null);

        if (fichero == null)
            return null;

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
        return fichero;


//        Platform.runLater(() -> {
//            try {
//                FileChooser fileChooser = new FileChooser();
//                File fichero = fileChooser.showSaveDialog(null);
//
//                if (fichero == null)
//                    return;
//
//                FileWriter fileWriter = new FileWriter(fichero + ".csv");
//                CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
//
//                for (ChannelData channel : channelsData) {
//                    printer.printRecord(
//                            channel.getNombre(),
//                            channel.getSitio(),
//                            channel.getRepro()
//                    );
//                }
//
//                file = fileWriter;
//                printer.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    private void CSVCompress(File p) {
        if (p == null) {
            System.out.println("Nada que exportar");
            return;
        }

        System.out.println("Impirme: " + p);

    }

}