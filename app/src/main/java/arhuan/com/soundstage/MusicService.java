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
        this.player.setOnCompletionListener(this);
        this.player.setOnErrorListener(this);
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        this.player.setOnPreparedListener(listener);
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
        if (this.player.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void playSong() {
        this.player.reset();
        Song playSong = this.songs.get(this.songPosition);
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            this.player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        this.player.prepareAsync();
    }

    public void playPrev() {
        this.songPosition--;
        if (this.songPosition < 0)
            this.songPosition = this.songs.size() - 1;
        playSong();
    }

    public void playNext() {
        this.songPosition++;
        if (this.songPosition >= this.songs.size())
            this.songPosition = 0;
        playSong();
    }

    public int getPosition(){
        return this.player.getCurrentPosition();
    }

    public int getDuration(){
        return this.player.getDuration();
    }

    public boolean isPlaying(){
        return this.player.isPlaying();
    }

    public void pausePlayer(){
        this.player.pause();
    }

    public void seek(int posn){
        this.player.seekTo(posn);
    }

    public void go(){
        this.player.start();
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
