package com.linearity.pcmusicplayer2;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.linearity.pcmusicplayer2.Consts.LIST_CELL_COLORS;
import static com.linearity.utils.FormatTimeUtils.formatTimeForTimer;

public class SongListController {
    private final Image playmode_ordered_image = new Image(this.getClass().getResource("icons/playmode_ordered.png").toString(),0,0,true,true,false);
    private final Image playmode_cycle_image = new Image(this.getClass().getResource("icons/playmode_cycle.png").toString(),0,0,true,true,false);
    private final Image playmode_random_image = new Image(this.getClass().getResource("icons/playmode_random.png").toString(),0,0,true,true,false);
    private final Image click_to_pause_image = new Image(this.getClass().getResource("icons/click_to_pause.png").toString(),0,0,true,true,false);
    private final Image click_to_resume_image = new Image(this.getClass().getResource("icons/click_to_resume.png").toString(),0,0,true,true,false);

    public static SongListController INSTANCE = null;
    @FXML
    public GridPane titleBarContainer;
    @FXML
    public ListView<Path> songListContainerMain;
    @FXML
    public GridPane allContainer;
    @FXML
    public GridPane musicPlayerContainer;
    @FXML
    public GridPane leftContainer;
    @FXML
    public Label titleText;
    @FXML
    public TextField searchbar;
    @FXML
    public ListView<Path> searchedContainerMain;
    @FXML
    public ListView<Path> songsContainerMain;
    @FXML
    public GridPane playerController;
    @FXML
    public GridPane songsScrolls;
    @FXML
    public GridPane searchedContainer;
    @FXML
    public VBox searchBarContainer;
    @FXML
    public Label addSongList;
    @FXML
    public GridPane progressBarContainer;
    @FXML
    public Label timerText;
    @FXML
    public ColoredProgressSlider player_progressbar;
    private final Queue<ChangeListener<? super Boolean>> progressBarValueChangingListeners = new ConcurrentLinkedQueue<>();
    @FXML
    public Label totalTimeText;
    @FXML
    public HBox control_panel_grid_item;
    @FXML
    public HBox prev_song;
    @FXML
    public Image prev_song_image;
    @FXML
    public GridPane pause_resume;
    @FXML
    public ImageView pause_resume_image_container;
    @FXML
    public Image next_song_image;
    @FXML
    public HBox next_song;
    @FXML
    public Image change_playmode_image;
    @FXML
    public HBox change_playmode;
    @FXML
    public ImageView prev_song_image_container;
    @FXML
    public ImageView change_playmode_image_container;
    @FXML
    public ImageView next_song_image_container;
    @FXML
    public ImageView song_image_view;
    @FXML
    public GridPane song_image_view_container;
    @FXML
    public TextField song_name_text;
    @FXML
    public TextField song_artist_text;

    @FXML
    public void initialize() {
        searchbar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                expandSearch();
                PlayerService.INSTANCE.searchSong(newValue);
            }else {
                collapseSearch();
                PlayerService.INSTANCE.searchSong("");
            }
        });
        addSongList.setOnMouseClicked((MouseEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a songList");
            fileChooser.setInitialDirectory(new File("./"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SongList", "*.musiclist"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                PlayerService.INSTANCE.addPlaylist(file.toPath());
            }
        });
        songListContainerMain.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Path> call(ListView<Path> pathListView) {

                ListCell<Path> cell = new ListCell<>(){
                    @Override
                    public void updateIndex(int newIndex) {
                        super.updateIndex(newIndex);
                        this.setStyle("-fx-background-color: #"+String.format("%08x",(LIST_CELL_COLORS[Math.abs(newIndex)%LIST_CELL_COLORS.length]))+";");
                        if (newIndex < 0){
                            this.setText("");
                            return;
                        }
                        List<Path> path = pathListView.getItems();
                        if (path.size() > newIndex){
                            this.setText(String.valueOf(path.get(newIndex)));
                        }
                    }
                };
                cell.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getClickCount() >= 2 && cell.getItem() != null) {
                        PlayerService.INSTANCE.readSongsFromPlaylist(cell.getItem());
                        PlayerService.INSTANCE.generateRandomPlaylist();
                    }
                });

                return cell;
            }
        });
        songListContainerMain.setItems(PlayerService.INSTANCE.playlistPaths);

        Callback<ListView<Path>,ListCell<Path>> playSongCellFactory = new Callback<>() {
            @Override
            public ListCell<Path> call(ListView<Path> pathListView) {

                ListCell<Path> cell = new ListCell<>(){
                    @Override
                    public void updateIndex(int newIndex) {
                        if (newIndex < 0){
                            this.setText("");
                            return;
                        }
                        super.updateIndex(newIndex);
                        List<Path> path = pathListView.getItems();
                        if (path.size() > newIndex){
                            this.setText(String.valueOf(path.get(newIndex)));
                        }
                        this.setStyle("-fx-background-color: #"+String.format("%08x",(LIST_CELL_COLORS[newIndex%LIST_CELL_COLORS.length]))+";");
                    }
                };
                cell.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getClickCount() >= 2){
                        Path cellItem = cell.getItem();
                        if (cellItem == null){return;}
                        PlayerService.INSTANCE.playSong(cellItem);
                    }
                });
                return cell;
            }
        };

        songsContainerMain.setCellFactory(playSongCellFactory);
        songsContainerMain.setItems(PlayerService.INSTANCE.currentPlaylist);
        searchedContainerMain.setCellFactory(playSongCellFactory);
        searchedContainerMain.setItems(PlayerService.INSTANCE.currentPlaylistSearched);


        {
            DoubleProperty valueProperty = player_progressbar.valueProperty();
            ChangeListener<? super Number> progressBarValueListener = (observable, oldValue, newValue) -> {
                if (!player_progressbar.isValueChanging()){
                    return;
                }
                MediaPlayer mediaPlayer = PlayerService.INSTANCE.getMediaPlayer();
                if (PlayerService.INSTANCE.getMediaPlayer() == null){
                    return;
                }
                String totalTime = formatTimeForTimer((int) mediaPlayer.getTotalDuration().toSeconds());
                totalTimeText.setText(
                        formatTimeForTimer((int) ((player_progressbar.getValue()/1000)))
                                + "/" + totalTime);
            };
            valueProperty.addListener(progressBarValueListener);
        }


        {
            ChangeListener<? super Boolean> changeListener = (observable, oldValue, newValue) -> {

                MediaPlayer mediaPlayer = PlayerService.INSTANCE.getMediaPlayer();
                if (PlayerService.INSTANCE.getMediaPlayer() == null){
                    return;
                }
                String totalTime = formatTimeForTimer((int) mediaPlayer.getTotalDuration().toSeconds());

                if (!newValue){
                    mediaPlayer.seek(Duration.millis(player_progressbar.getValue()));
                    totalTimeText.setText(totalTime);
                }
            };
            BooleanProperty valueChangedProperty = player_progressbar.valueChangingProperty();
            valueChangedProperty.addListener(changeListener);
        }
        player_progressbar.setOnMousePressed((MouseEvent event) -> {
            MediaPlayer mediaPlayer = PlayerService.INSTANCE.getMediaPlayer();
            if (PlayerService.INSTANCE.getMediaPlayer() == null){
                return;
            }
            double mouseX = event.getX();
            double width = player_progressbar.getWidth();
            double percent = mouseX / width;
            mediaPlayer.seek(Duration.millis(percent*mediaPlayer.getTotalDuration().toMillis()));
            String totalTime = formatTimeForTimer((int) mediaPlayer.getTotalDuration().toSeconds());
            totalTimeText.setText(totalTime);
        });
        {
            ImageView[] toResize = new ImageView[]{
                    prev_song_image_container, pause_resume_image_container, next_song_image_container, change_playmode_image_container
            };
            DoubleBinding widthOrHeightMin = Bindings.createDoubleBinding(() -> Math.min(control_panel_grid_item.getHeight(), control_panel_grid_item.getWidth()),
                    control_panel_grid_item.heightProperty(),
                    control_panel_grid_item.widthProperty()
            );
            for (ImageView imageView : toResize) {
                imageView.setManaged(true);
                imageView.fitWidthProperty().unbind();
                imageView.fitHeightProperty().unbind();
                imageView.minWidth(0);
                imageView.minHeight(0);
                ChangeListener<Number> listener = (observable, oldValue, newValue) -> {
                    double setTo = widthOrHeightMin.get();
                    imageView.setFitHeight(setTo);
                    imageView.setFitWidth(setTo);
                };
                allContainer.heightProperty().addListener(listener);
                allContainer.widthProperty().addListener(listener);
            }
            ChangeListener<Number> sizeChangeListener = (observableValue, number, t1) -> {
                double minSize = Math.min(control_panel_grid_item.getHeight(),control_panel_grid_item.getWidth());
                for (ImageView view : toResize){
                    view.setFitWidth(minSize);
                    view.setFitHeight(minSize);
                }
            };
            control_panel_grid_item.widthProperty().addListener(sizeChangeListener);
            control_panel_grid_item.heightProperty().addListener(sizeChangeListener);
        }
        {
            ImageView[] toResize = new ImageView[]{song_image_view};
            for (ImageView imageView : toResize) {
                imageView.setManaged(true);
                imageView.fitWidthProperty().unbind();
                imageView.fitHeightProperty().unbind();
                imageView.minWidth(0);
                imageView.minHeight(0);
                imageView.fitHeightProperty().bind(song_image_view_container.heightProperty());
                imageView.fitWidthProperty().bind(song_image_view_container.widthProperty());
            }

        }

        prev_song.setOnMouseClicked(mouseEvent -> PlayerService.INSTANCE.playPrevSong());
        next_song.setOnMouseClicked(mouseEvent -> PlayerService.INSTANCE.playNextSong());
        pause_resume.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
                PlayerService.PlayState state = PlayerService.INSTANCE.playStateReference.get();
                switch (state) {
                    case PLAYING:
                        setImageByState(PlayerService.INSTANCE.pauseSong());
                        return;
                    case PAUSE:
                        setImageByState(PlayerService.INSTANCE.resumeSong());
                        return;

                }
            }
        });
        change_playmode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
                setImageByPlayMode(PlayerService.INSTANCE.switchPlayMode());
            }
        });
        INSTANCE = this;

    }


    private void setImageByState(PlayerService.PlayState playState) {
        switch (playState) {
            case PLAYING:
                pause_resume_image_container.setImage(click_to_pause_image);
                return;
            case PAUSE:
                pause_resume_image_container.setImage(click_to_resume_image);
        }
    }
    private void setImageByPlayMode(PlayerService.PlayMode playMode) {
        switch (playMode){
            case ORDERED -> {
                change_playmode_image_container.setImage(playmode_ordered_image);
                return;
            }
            case CYCLE -> {
                change_playmode_image_container.setImage(playmode_cycle_image);
                return;
            }
            case RANDOM -> {
                change_playmode_image_container.setImage(playmode_random_image);
                return;
            }
        }
    }
    public void collapseSearch() {
        songsScrolls.getRowConstraints().get(0).percentHeightProperty().setValue(0);
        songsScrolls.getRowConstraints().get(1).percentHeightProperty().setValue(100);
    }
    public void expandSearch() {
        songsScrolls.getRowConstraints().get(0).percentHeightProperty().setValue(70);
        songsScrolls.getRowConstraints().get(1).percentHeightProperty().setValue(30);
    }

    public static void notifyMediaPlayerChanged(MediaPlayer mediaPlayer,Path currentPlaying) {
        if (INSTANCE == null){
            new RuntimeException("SongListController is null!").printStackTrace();
            return;
        }

        mediaPlayer.setOnPlaying(() -> {
                    PlayerService.INSTANCE.playStateReference.set(PlayerService.PlayState.PLAYING);
                    INSTANCE.setImageByState(PlayerService.PlayState.PLAYING);
                    INSTANCE.player_progressbar.setMax(mediaPlayer.getTotalDuration().toMillis());
                    INSTANCE.totalTimeText.setText(formatTimeForTimer((int) mediaPlayer.getTotalDuration().toSeconds()));
                }
        );
        INSTANCE.player_progressbar.setValue(0.);

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, played) -> {
            if (!INSTANCE.player_progressbar.isValueChanging()){
                double playedTime = played.toMillis();
                INSTANCE.player_progressbar.setValue(playedTime);
                INSTANCE.timerText.setText(formatTimeForTimer((int) played.toSeconds()));
            }
        });
        mediaPlayer.setOnPaused(() -> {
                    PlayerService.INSTANCE.playStateReference.set(PlayerService.PlayState.PAUSE);
                    INSTANCE.setImageByState(PlayerService.PlayState.PAUSE);
                }
        );
        Image songImage = null;
        String title = currentPlaying.getFileName().toString();
        String aratistsName = "";
        if (title.endsWith(".mp3")){
            try {
                Mp3File mp3File = new Mp3File(currentPlaying);
                ID3v1 tag1 = mp3File.hasId3v1Tag()?mp3File.getId3v1Tag():null;
                ID3v2 tag2 = mp3File.hasId3v2Tag()?mp3File.getId3v2Tag():null;
                if (tag1 != null) {
                    String gotTitle = tag1.getTitle();
                    if (gotTitle != null && !gotTitle.isEmpty()) {
                        title = gotTitle;
                    }
                    String gotAuthor = tag1.getTitle();
                    if (gotAuthor != null && !gotAuthor.isEmpty()) {
                        aratistsName = gotAuthor;
                    }
                }
                if (tag2 != null) {
                    String gotTitle = tag2.getTitle();
                    if (gotTitle != null && !gotTitle.isEmpty()) {
                        title = gotTitle;
                    }
                    String gotAuthor = tag2.getArtist();
                    if (gotAuthor != null && !gotAuthor.isEmpty()) {
                        aratistsName = gotAuthor;
                    }
                    byte[] image = tag2.getAlbumImage();
                    if (image != null) {
                        songImage = new Image(new ByteArrayInputStream(image));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        INSTANCE.song_name_text.setText(title);
        INSTANCE.song_artist_text.setText(aratistsName);
        INSTANCE.song_image_view.setImage(songImage);
        //TODO:Impl
    }
}
