module com.linearity.pcmusicplayer2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires jsr305;
    requires javafx.media;
    requires mp3agic;

    opens com.linearity.pcmusicplayer2 to javafx.fxml;
    exports com.linearity.pcmusicplayer2;
    exports com.linearity.utils;
    opens com.linearity.utils to javafx.fxml;
}