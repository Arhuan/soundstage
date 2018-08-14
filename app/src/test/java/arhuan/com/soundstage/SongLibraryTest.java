package arhuan.com.soundstage;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class SongLibraryTest {
    private SongLibrary testSongLibrary;

    @Before
    public void runBefore() {
        this.testSongLibrary = new SongLibrary();

        ArrayList<Song> testSongList = new ArrayList<>();

        testSongList.add(new Song(133, "Redbone", "Childish Gambino", 326000));
        testSongList.add(new Song(144, "American Pie", "Don McLean", 515000));
        testSongList.add(new Song(899, "90210", "Travis Scott", 339000));
        testSongList.add(new Song(1, "goosebumps", "Travis Scott", 243000));
        testSongList.add(new Song(42, "Dancing Queen", "ABBA", 230000));

        this.testSongLibrary.setSongList(testSongList);
    }

    @Test
    public void testSortSongListAlphabetically() {
        this.testSongLibrary.sortSongsAlphabetically();

        ArrayList<Song> sortedSongList = new ArrayList<>();

        sortedSongList.add(new Song(899, "90210", "Travis Scott", 339000));
        sortedSongList.add(new Song(144, "American Pie", "Don McLean", 515000));
        sortedSongList.add(new Song(42, "Dancing Queen", "ABBA", 230000));
        sortedSongList.add(new Song(1, "goosebumps", "Travis Scott", 243000));
        sortedSongList.add(new Song(133, "Redbone", "Childish Gambino", 326000));

        assertEquals(sortedSongList, this.testSongLibrary.getSongList());
    }

    @Test
    public void testSortSongListArtist() {
        this.testSongLibrary.sortSongsArtist();

        ArrayList<Song> sortedSongList = new ArrayList<>();

        sortedSongList.add(new Song(42, "Dancing Queen", "ABBA", 230000));
        sortedSongList.add(new Song(133, "Redbone", "Childish Gambino", 326000));
        sortedSongList.add(new Song(144, "American Pie", "Don McLean", 515000));
        sortedSongList.add(new Song(899, "90210", "Travis Scott", 339000));
        sortedSongList.add(new Song(1, "goosebumps", "Travis Scott", 243000));

        assertEquals(sortedSongList, this.testSongLibrary.getSongList());
    }
}
