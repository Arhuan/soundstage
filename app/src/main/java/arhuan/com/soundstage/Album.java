package arhuan.com.soundstage;

import java.util.ArrayList;

public class Album {
    private String title;
    private String artist;
    private ArrayList<Song> songList;

    public Album() {

    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public ArrayList<Song> getSongs() {
        return this.songList;
    }
}
