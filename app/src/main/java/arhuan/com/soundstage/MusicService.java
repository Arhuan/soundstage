package arhuan.com.soundstage;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private final IBinder musicBinder = new MusicBinder();

    public void onCreate() {
        super.onCreate();
        this.player = new MediaPlayer();
        this.songPosition = 0;
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        this.player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.player.setOnPreparedListener(this);
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.player.stop();
        this.player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public void playSong() {
        this.player.reset();
        Song playSong = this.songs.get(this.songPosition);
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try{
            this.player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        this.player.prepareAsync();
    }

    public void setSong(int songIndex) {
        this.songPosition = songIndex;
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
