package arhuan.com.soundstage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songList;
    private LayoutInflater songInflater;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.songList = songs;
        this.songInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.songList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // map to song layout
        LinearLayout songLayout = (LinearLayout) this.songInflater.inflate(R.layout.song, viewGroup, false);
        //get title and artist views
        TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLayout.findViewById(R.id.song_artist);
        //get song using position
        Song currSong = this.songList.get(i);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        //set position as tag
        songLayout.setTag(i);
        return songLayout;
    }

}
