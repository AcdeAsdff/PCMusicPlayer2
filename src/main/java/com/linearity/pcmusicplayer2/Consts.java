package com.linearity.pcmusicplayer2;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Consts {
    public static final Path PLAYLIST_PATH = Paths.get("playlists.patharr");
    public static final int[] LIST_CELL_COLORS = new int[]{0x39c5bb80,0x01878680};
    public static final String[] SUPPORT_SONG_FORMATS = new String[]{"mp3", "wav"};
}
