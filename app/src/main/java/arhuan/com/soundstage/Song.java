package arhuan.com.soundstage;

public class Song {
    private long id;
    private String title;
    private String artist;
    private long length; // length of song, in ms

    public Song(long id, String title, String artist, long length) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.length = length;
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public long getLength() {
        return this.length;
    }
}
