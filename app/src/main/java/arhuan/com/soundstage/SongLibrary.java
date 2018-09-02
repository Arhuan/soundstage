package arhuan.com.soundstage;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;

public class SongLibrary extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle navigationToggle;
    private ListView songView;
    private ArrayList<Song> songList;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;
    private boolean playbackPaused = false;
    private MusicController musicController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_library);

        this.drawerLayout = findViewById(R.id.library);
        this.navigationToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.string.open, R.string.close);

        this.drawerLayout.addDrawerListener(this.navigationToggle);
        this.navigationToggle.syncState();

        this.songView = findViewById(R.id.songList);
        this.songList = new ArrayList<>();
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            initSongList();
        }

        SongAdapter songAdapter = new SongAdapter(this, this.songList);
        this.songView.setAdapter(songAdapter);

        setMusicController();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            musicService.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    musicService.go();
                    musicController.show(0);
                }
            });
            //pass list
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.playbackPaused = true;
    }

    @Override
    protected void onDestroy() {
        stopService(this.playIntent);
        this.musicService = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.paused) {
            setMusicController();
            this.paused = false;
        }
    }

    @Override
    protected void onStop() {
        this.musicController.hide();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.songlibrary_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void songSelected(View view) {
        this.musicService.setSong(Integer.parseInt(view.getTag().toString()));
        this.musicService.playSong();
        if(this.playbackPaused) {
            setMusicController();
            this.playbackPaused = false;
        }
        this.musicController.show(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.navigationToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.nav_settings:
                // user selected to go to settings UI menu
                return true;

            case R.id.songLibrarySearch:
                // change action bar to a search bar
                return true;

            case R.id.byTitle:
                // user selected to sort by title
                sortSongsAlphabetically();
                this.songView.setAdapter(new SongAdapter(this, this.songList));
                this.musicService.setList(this.songList);
                return true;

            case R.id.byArtist:
                // user selected to sort by artist
                sortSongsArtist();
                this.songView.setAdapter(new SongAdapter(this, this.songList));
                this.musicService.setList(this.songList);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int lengthColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                long id = musicCursor.getInt(idColumn);
                String title = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                long length = musicCursor.getInt(lengthColumn);
                this.songList.add(new Song(id, title, artist, length));
            } while (musicCursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    initSongList();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void sortSongsAlphabetically() {
        // sorts the song list alphabetically by title
        this.songList = sortSongsAlphabetically(this.songList);
    }

    private ArrayList<Song> sortSongsAlphabetically(ArrayList<Song> songs) {
        // sorts the song list alphabetically by title
        if (songs.size() <= 1) {
            return songs;
        }

        ArrayList<Song> list1 = new ArrayList<>();
        ArrayList<Song> list2 = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            if (i < songs.size() / 2) {
                list1.add(songs.get(i));
            } else {
                list2.add(songs.get(i));
            }
        }

        list1 = sortSongsAlphabetically(list1);
        list2 = sortSongsAlphabetically(list2);

        return sortSongsAlphabeticallyMerge(list1, list2);
    }

    private ArrayList<Song> sortSongsAlphabeticallyMerge(ArrayList<Song> list1, ArrayList<Song> list2) {
        ArrayList<Song> result = new ArrayList<>();
        int idx1 = 0;
        int idx2 = 0;

        while (!list1.isEmpty() && !list2.isEmpty() && idx1 < list1.size() && idx2 < list2.size()) {
            if (list1.get(idx1).getTitle().compareToIgnoreCase(list2.get(idx2).getTitle()) < 0) {
                result.add(list1.get(idx1));
                idx1++;
            } else {
                result.add(list2.get(idx2));
                idx2++;
            }
        }

        while (idx1 < list1.size()) {
            result.add(list1.get(idx1));
            idx1++;
        }

        while (idx2 < list2.size()) {
            result.add(list2.get(idx2));
            idx2++;
        }

        return result;
    }

    public void sortSongsArtist() {
        // sorts the song list alphabetically by artist name, if artist name is the same -> sort by title of song
        this.songList = sortSongsArtist(this.songList);
    }

    private ArrayList<Song> sortSongsArtist(ArrayList<Song> songs) {
        if (songs.size() <= 1) {
            return songs;
        }

        ArrayList<Song> list1 = new ArrayList<>();
        ArrayList<Song> list2 = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            if (i < songs.size() / 2) {
                list1.add(songs.get(i));
            } else {
                list2.add(songs.get(i));
            }
        }

        list1 = sortSongsArtist(list1);
        list2 = sortSongsArtist(list2);

        return sortSongsArtistMerge(list1, list2);
    }

    private ArrayList<Song> sortSongsArtistMerge(ArrayList<Song> list1, ArrayList<Song> list2) {
        ArrayList<Song> result = new ArrayList<>();
        int idx1 = 0;
        int idx2 = 0;

        while (!list1.isEmpty() && !list2.isEmpty() && idx1 < list1.size() && idx2 < list2.size()) {
            if (list1.get(idx1).getArtist().compareToIgnoreCase(list2.get(idx2).getArtist()) == 0) {
                if (list1.get(idx1).getTitle().compareToIgnoreCase(list2.get(idx2).getTitle()) < 0) {
                    result.add(list1.get(idx1));
                    idx1++;
                } else {
                    result.add(list2.get(idx2));
                    idx2++;
                }
            } else if (list1.get(idx1).getArtist().compareToIgnoreCase(list2.get(idx2).getArtist()) < 0) {
                result.add(list1.get(idx1));
                idx1++;
            } else {
                result.add(list2.get(idx2));
                idx2++;
            }
        }

        while (idx1 < list1.size()) {
            result.add(list1.get(idx1));
            idx1++;
        }

        while (idx2 < list2.size()) {
            result.add(list2.get(idx2));
            idx2++;
        }

        return result;
    }

    public void setSongList(ArrayList<Song> newList) {
        this.songList = newList;
    }

    public ArrayList<Song> getSongList() {
        return this.songList;
    }

    @Override
    public void start() {
        this.musicService.go();
    }

    @Override
    public void pause() {
        this.playbackPaused = true;
        this.musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (this.musicService != null && this.musicBound && this.musicService.isPlaying())
            return this.musicService.getDuration();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (this.musicService != null && this.musicBound && this.musicService.isPlaying())
            return this.musicService.getPosition();
        return 0;

    }

    @Override
    public void seekTo(int i) {
        this.musicService.seek(i);
    }

    @Override
    public boolean isPlaying() {
        if (this.musicService != null && this.musicBound)
            return this.musicService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void playNext() {
        this.musicService.playNext();
        if(this.playbackPaused) {
            setMusicController();
            this.playbackPaused = false;
        }
        this.musicController.show(0);
    }

    private void playPrev() {
        this.musicService.playPrev();
        if(this.playbackPaused) {
            setMusicController();
            this.playbackPaused = false;
        }
        this.musicController.show(0);
    }

    public void setMusicController() {
        // set up the music controller
        this.musicController = new MusicController(this, false);
        this.musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        this.musicController.setMediaPlayer(this);
        this.musicController.setAnchorView(findViewById(R.id.songList));
        this.musicController.setEnabled(true);
    }
}
