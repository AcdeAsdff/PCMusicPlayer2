package com.linearity.pcmusicplayer2;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;

import java.util.concurrent.atomic.AtomicBoolean;


public class ColoredProgressSlider extends Slider {

    private final ObjectProperty<String> progressedColor = new SimpleObjectProperty<>("#39c5bbFF");
    private final ObjectProperty<String> remainingColor = new SimpleObjectProperty<>("#AAAAAA80");

    public ColoredProgressSlider() {
        super();

        // Recolor track when value or color changes
        valueProperty().addListener((obs, oldVal, newVal) -> updateTrackFill());
//        progressedColor.addListener((obs, oldVal, newVal) -> updateTrackFill());
//        remainingColor.addListener((obs, oldVal, newVal) -> updateTrackFill());

        // Wait until layout is ready before lookup
        Platform.runLater(this::updateTrackFill);
    }
    public String getProgressedColor() {
        return progressedColor.get();
    }
    public void setProgressedColor(String progressedColor) {
        this.progressedColor.set(progressedColor);
    }
    public ObjectProperty<String> progressedColorProperty() {
        return progressedColor;
    }
    public String getRemainingColor() {
        return remainingColor.get();
    }
    public void setRemainingColor(String remainingColor) {
        this.remainingColor.set(remainingColor);
    }
    public ObjectProperty<String> remainingColorProperty() {
        return remainingColor;
    }

    private void updateTrackFill() {
        Region track = (Region) lookup(".track");
        if (track == null) return;

        double ratio = (getValue() - getMin()) / (getMax() - getMin());
        // Clamp between 0–1
        ratio = Math.max(0, Math.min(1, ratio));
        if (Double.isNaN(ratio)) {
            ratio = 0;
            System.out.println("ratio is NaN:" + getValue() + " " + getMin() + " " + getMax());
        }
        String style = String.format(
                "-fx-background-color: linear-gradient(to right, %s %.2f%%, %s %.2f%%); "
                        + "-fx-background-radius: 0; -fx-border-radius: 0;",
                getProgressedColor(), ratio * 100,
                getRemainingColor(), ratio * 100
        );

        track.setStyle(style);
    }
}
