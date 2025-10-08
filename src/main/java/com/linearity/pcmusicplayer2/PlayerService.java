package com.linearity.pcmusicplayer2;

import com.linearity.pcmusicplayer2.annotations.DoubleRange;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


import static com.linearity.pcmusicplayer2.Consts.PLAYLIST_PATH;
import static com.linearity.pcmusicplayer2.Consts.SUPPORT_SONG_FORMATS;
import static com.linearity.utils.ShuffleUtils.getShuffledList;

@ParametersAreNonnullByDefault
public class PlayerService {
    private MediaPlayer mediaPlayer = null;
    @Nullable
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    private void setMediaPlayer(@Nullable MediaPlayer mediaPlayer) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
        }
        this.mediaPlayer = mediaPlayer;
    }
    public enum PlayMode {
        ORDERED,CYCLE,RANDOM
    }
    public enum PlayState {
        PAUSE,PLAYING,
    }
    public AtomicReference<PlayState> playStateReference = new AtomicReference<>(PlayState.PAUSE);
    public PlayMode[] modes = PlayMode.values();
    public AtomicInteger currentModeIndex = new AtomicInteger(0);
    public PlayMode currentPlayMode(){
        int pickIndex = currentModeIndex.get() % modes.length;
        if (pickIndex < 0){
            pickIndex += modes.length;
        }
        return modes[pickIndex];
    }
    //'to' should contain from.get(currentModeIndex)
    //more strict:from and to should have same size and elements(can be different order)
    public void indexConverting(List<Path> from, List<Path> to){
        int oldIndex = currentPlaylistIndex.get();
        Path playPath = from.get(oldIndex);
        int newIndex = to.indexOf(playPath);
        currentPlaylistIndex.set(newIndex);
    }
    public PlayMode switchPlayMode(){

        return switchPlayModeInner();
    }
    private synchronized PlayMode switchPlayModeInner(){
        PlayMode switchFromMode = currentPlayMode();
        PlayMode switchToMode = pickNextPlayMode();
        if (!currentPlaylist.isEmpty()){
            if (switchToMode == PlayMode.RANDOM) {
                generateRandomPlaylist();
                indexConverting(currentPlaylist, randomPlaylist);
            } else if (switchFromMode == PlayMode.RANDOM) {
                indexConverting(randomPlaylist, currentPlaylist);
            }
        }
        return switchToMode;
    }
    private PlayMode pickNextPlayMode(){
        int pickIndex = currentModeIndex.addAndGet(1) % modes.length;
        if (pickIndex < 0){
            pickIndex += modes.length;
        }
        return modes[pickIndex];
    }

    public final ObservableList<Path> playlistPaths = FXCollections.observableArrayList();
    public final ObservableList<Path> currentPlaylist = FXCollections.observableArrayList();
    public final ObservableList<Path> currentPlaylistSearched = FXCollections.observableArrayList();

    public final List<Path> randomPlaylist = new CopyOnWriteArrayList<>();

    public AtomicInteger currentPlaylistIndex = new AtomicInteger(0);

    public void generateRandomPlaylist() {
        randomPlaylist.clear();
        randomPlaylist.addAll(getShuffledList(currentPlaylist));
    }

    public static final PlayerService INSTANCE = new PlayerService();
    private PlayerService() {
        playlistPaths.addAll(readPlaylists());
        playlistPaths.addListener((ListChangeListener<Path>) change -> {
            //save paths
            savePlaylists(playlistPaths.toArray(new Path[0]));
        });
    }
    public synchronized void searchSong(String searchByText) {
        currentPlaylistSearched.clear();
        if (searchByText.isEmpty()) {
            return;
        }
        List<Path> searchedSongs = new ArrayList<>();
        currentPlaylist.forEach(playlistPath -> {
            if (playlistPath.getFileName().toString().contains(searchByText)) {
                searchedSongs.add(playlistPath);
            }
        });
        currentPlaylistSearched.addAll(searchedSongs);
    }
    public void considerAddSong(Path songPath) {
        synchronized (currentPlaylist) {
            String lowerName = songPath.getFileName().toString().toLowerCase();
            for (String ending : SUPPORT_SONG_FORMATS) {
                if (lowerName.endsWith(ending)) {
                    currentPlaylist.add(songPath);
                }
            }
        }
    }
    public void considerAddSong(File songFile) {
        considerAddSong(songFile.toPath());
    }

    public synchronized void addPlaylist(Path path) {
        playlistPaths.add(path);
    }
    public void readSongsFromFolder(Path path) {
        readSongsFromFolder(path.toFile());
    }
    public void readSongsFromFolder(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {return;}
        for (File f1 : files) {
            if (f1.isDirectory()) {
                readSongsFromFolder(f1);
            }else if(f1.isFile()) {
                String lowerName = f1.getName().toLowerCase();
                if (lowerName.endsWith(".musiclist")) {
                    readSongsFromPlaylist(f1);
                }else {
                    considerAddSong(f1);
                }
            }
        }
    }

    public void readSongsFromPlaylist(Path path) {
        readSongsFromPlaylist(path.toFile());
    }

    public void readSongsFromPlaylist(File file) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.lines().forEach(line -> {
                File f = new File(line);
                //TODO:Read songs from file/folder
                if (f.exists()) {
                    if (f.isDirectory()) {
                        readSongsFromFolder(f);
                    }else if(f.isFile()) {
                        String lowerName = f.getName().toLowerCase();
                        if (lowerName.endsWith(".musiclist")) {
                            readSongsFromPlaylist(f);
                        }else {
                            considerAddSong(f);
                        }
                    }
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            currentPlaylist.clear();
        }
    }

    private void savePlaylists(Path[] paths) {
        synchronized (playlistPaths) {
            try {
                String[] converted = new String[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    converted[i] = paths[i].toString();
                }
                File to = PLAYLIST_PATH.toFile();
                if (!to.exists()) {
                    to.createNewFile();
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(to));
                out.writeObject(converted);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private Path[] readPlaylists() {
        synchronized (playlistPaths) {
            try {
                File from = PLAYLIST_PATH.toFile();
                if (!from.exists()) {
                    return new Path[0];
                }

                String[] converted = (String[]) new ObjectInputStream(new FileInputStream(from)).readObject();
                Path[] paths = new Path[converted.length];
                for (int i = 0; i < converted.length; i++) {
                    paths[i] = Path.of(converted[i]);
                }
                return paths;
            } catch (Exception e) {
                e.printStackTrace();
                return new Path[0];
            }
        }
    }

    public void playSpecificSong(Path songPath) {
        PlayMode mode = currentPlayMode();
        switch (mode) {
            case ORDERED, CYCLE:
                currentModeIndex.set(currentPlaylist.indexOf(songPath));
                break;
            case RANDOM:
                currentModeIndex.set(randomPlaylist.indexOf(songPath));
                break;
        }
        playSong(songPath);
    }

    public int pickIndexNext(List<Path> paths) {
        int pickIndex = currentPlaylistIndex.addAndGet(1) % paths.size();
        if (pickIndex < 0) {
            pickIndex += paths.size();
        }
        return pickIndex;
    }

    public int pickIndexPrev(List<Path> paths) {
        int pickIndex = currentPlaylistIndex.addAndGet(-1) % paths.size();
        if (pickIndex < 0) {
            pickIndex += paths.size();
        }
        return pickIndex;
    }

    public void playPrevSong() {
        switch (currentPlayMode()) {
            case RANDOM: {
                if (randomPlaylist.isEmpty()) {return;}
                playSong(randomPlaylist.get(pickIndexPrev(randomPlaylist)));
                return;
            }
            case ORDERED: {
                if (currentPlaylist.isEmpty()) {return;}
                playSong(currentPlaylist.get(pickIndexPrev(currentPlaylist)));
                return;
            }
            case CYCLE: {
                if (currentPlaylist.isEmpty()) {return;}
                playSong(currentPlaylist.get(currentPlaylistIndex.get()));
                return;
            }
        }
    }
    public void playNextSong() {
        switch (currentPlayMode()) {
            case RANDOM: {
                if (randomPlaylist.isEmpty()) {return;}
                playSong(randomPlaylist.get(pickIndexNext(randomPlaylist)));
                return;
            }
            case ORDERED: {
                if (currentPlaylist.isEmpty()) {return;}
                playSong(currentPlaylist.get(pickIndexNext(currentPlaylist)));
                return;
            }
            case CYCLE: {
                if (currentPlaylist.isEmpty()) {return;}
                playSong(currentPlaylist.get(currentPlaylistIndex.get()));
                return;
            }
        }
    }

    public void playSong(Path songPath) {
        Media media = new Media(songPath.toFile().toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        setMediaPlayer(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(PlayerService.INSTANCE::playNextSong);
        SongListController.notifyMediaPlayerChanged(mediaPlayer,songPath);
        mediaPlayer.play();
        playStateReference.set(PlayState.PLAYING);
    }
    public PlayState pauseSong(){
        MediaPlayer player = getMediaPlayer();
        if (player != null) {
            player.pause();
            this.playStateReference.set(PlayState.PAUSE);
            return PlayState.PAUSE;
        }
        return this.playStateReference.get();
    }
    public PlayState resumeSong(){
        MediaPlayer player = getMediaPlayer();
        if (player != null) {
            player.play();
            this.playStateReference.set(PlayState.PLAYING);
            return PlayState.PLAYING;
        }
        return this.playStateReference.get();
    }
    public void changeVolume(@DoubleRange(min = 0.,max = 1.)double volume) {
        MediaPlayer player = getMediaPlayer();
        if (player != null) {
            player.setVolume(volume);
        }
    }
}
