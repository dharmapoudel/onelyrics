package com.lyricslover.onelyrics.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.helpers.PermissionHelper;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.misc.OfflineSongsUtil;
import com.lyricslover.onelyrics.misc.Utils;
import com.lyricslover.onelyrics.pojos.AppPreferences;
import com.lyricslover.onelyrics.pojos.Song;
import com.lyricslover.onelyrics.services.LyricsService;

import org.jsoup.internal.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;


public class LyricsViewAdapter extends RecyclerView.Adapter<LyricsViewAdapter.ViewHolder> implements OnClickListener, Filterable {

    private Activity activity;
    private List<Song> offlineLyricsList;
    //public Resources res;
    //private Song song = null;
    //private ArrayList<Song> offlineSongsList = null;
    private HashMap<String, Song> offlineSongsMap = new HashMap<>();
    //private SharedPreferences sharedPreferences;
    private final boolean highlightOfflineSongsLyrics;
    private List<Song> filteredSongs;

    private LyricsViewAdapterListener listener;


    protected static final String TAG = LyricsViewAdapter.class.getSimpleName();


    LyricsViewAdapter(Activity a, List<Song> d, LyricsViewAdapterListener listener) {
        this.activity = a;
        this.offlineLyricsList = d;
        this.filteredSongs = d;
        this.listener = listener;

        AppPreferences appPreferences = AppPreferences.getInstance(a.getApplicationContext());

        ArrayList<Song> offlineSongsList;

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        highlightOfflineSongsLyrics = appPreferences.getPreference(Constants.HIGHLIGHT_OFFLINE_SONG_LYRICS, false);
        //sharedPreferences.getBoolean(Constants.HIGHLIGHT_OFFLINE_SONG_LYRICS, false);

        if (PermissionHelper.allPermissionsAvailable(activity)) {
            offlineSongsList = new ArrayList<>();
            OfflineSongsUtil.getOfflineSongs(activity.getContentResolver(), offlineSongsList);
            offlineSongsMap = OfflineSongsUtil.convertListToHashMap(offlineSongsList);
        }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchText = charSequence.toString();
                if (searchText.isEmpty()) {
                    offlineLyricsList = filteredSongs;
                } else {
                    List<Song> filteredList = new ArrayList<>();

                    for (Song song : offlineLyricsList) {
                        if (song.getArtist().toLowerCase().contains(searchText) || song.getTrack().toLowerCase().contains(searchText)) {
                            filteredList.add(song);
                        }
                    }
                    offlineLyricsList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = offlineLyricsList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // offlineLyricsList = (ArrayList<Song>) filterResults.values;

                List<?> result = (List<?>) filterResults.values;
                List<Song> tempList = new ArrayList<>();
                for (Object object : result) {
                    if (object instanceof Song) {
                        tempList.add((Song) object); // <-- add to temp
                    }
                }
                offlineLyricsList = tempList; // <-- set filtered
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView track;
        TextView artist;
        RelativeLayout viewBackground;
        RelativeLayout viewForeground;

        ViewHolder(View v) {
            super(v);
            track = v.findViewById(R.id.track);
            artist = v.findViewById(R.id.artist);
            albumArt = v.findViewById(R.id.albumArt);
            viewBackground = v.findViewById(R.id.view_background);
            viewForeground = v.findViewById(R.id.view_foreground);
        }
    }

    void updateItems(List<Song> songsList) {
        offlineLyricsList = songsList;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.customlist_adapter, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        if (!offlineLyricsList.isEmpty()) {

            Song song = offlineLyricsList.get(position);
            holder.track.setText(song.getTrack());
            holder.artist.setText(song.getArtist());

            //TODO: refresh view immediately on toggle change
            // set font style bold if toggle is ON and song is available offline
            if (highlightOfflineSongsLyrics && offlineSongsMap.get(Utils.getSongTitle(song)) != null) {
                holder.track.setTypeface(null, Typeface.BOLD);
                holder.artist.setTypeface(null, Typeface.BOLD);
            } else {
                holder.track.setTypeface(null, Typeface.NORMAL);
                holder.artist.setTypeface(null, Typeface.NORMAL);
            }

            try {
                String albumArtPath = Utils.getAlbumArtPathFromSong(song, holder.albumArt.getContext());
                albumArtPath = StringUtil.isBlank(albumArtPath) ? getURLForResource() : albumArtPath;
                Glide.with(holder.albumArt.getContext())
                        .load(albumArtPath)
                        //.apply(new RequestOptions().placeholder(R.drawable.music_icon_default))
                        .into(holder.albumArt);

            } catch (Exception e) {
                Log.e(TAG, "Error occured while fetching the album art. ");
            }


            holder.itemView.setOnClickListener(view -> {
                view.setSelected(true);
                Song song1 = offlineLyricsList.get(position);
                listener.onLyricsViewItemSelected(song1);
                // show lyrics panel when list item is clicked
                Intent intent1 = new Intent(activity, LyricsService.class);
                intent1.putExtra(Constants.ARTIST, song1.getArtist());
                intent1.putExtra(Constants.TRACK, song1.getTrack());
                intent1.putExtra(Constants.OPEN_LYRICS_PANEL, true);
                intent1.putExtra(Constants.OFFLINE_READ, true);
                intent1.putExtra(Constants.TRACK_CHANGED, true);
                activity.startService(intent1);
            });

            holder.itemView.setOnLongClickListener(view -> {
                view.setSelected(true);
                Song song2 = offlineLyricsList.get(position);
                listener.onLyricsViewItemSelected(song2);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(song2.getFileName());
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getMimeTypeFromExtension(Constants.TXT_FILE_EXTENSION);
                Uri contentUri = FileProvider.getUriForFile(activity, Constants.AUTHORITY, file);
                String title = view.getContext().getString(R.string.chooser_title);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(contentUri, type);
                activity.startActivity(Intent.createChooser(intent, title));
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return offlineLyricsList.isEmpty() ? 1 : offlineLyricsList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        //do nothing
    }


    void removeItem(int position) {
        offlineLyricsList.remove(position);
        // notify the item removed by position to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    private String getURLForResource() {
        String url = "android.resource://" ;
        if (R.class.getPackage() != null) {
            url +=  R.class.getPackage().getName();
        }
        url += "/" + R.drawable.music_icon;
        return url;
    }


    public interface LyricsViewAdapterListener {
        void onLyricsViewItemSelected(Song song);
    }

}