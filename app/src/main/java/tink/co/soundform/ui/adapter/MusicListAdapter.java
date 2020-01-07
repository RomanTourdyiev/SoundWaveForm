package tink.co.soundform.ui.adapter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tink.co.soundform.App;
import tink.co.soundform.R;
import tink.co.soundform.Record;
import tink.co.soundform.ui.fragment.PlayerFragment;
import tink.co.soundform.util.Util;

import static tink.co.soundform.Config.RECORD;
import static tink.co.soundform.Config.RECORDS;

/**
 * Created by Tourdyiev Roman on 2020-01-06.
 */
public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    private Gson gson;
    private Util util;
    private List<Record> records;
    private boolean isRoot;
    private static final int FOLDER_UP = 0;
    private static final int FOLDER = 1;

    public MusicListAdapter(List<Record> records) {
        util = Util.getInstance();
        gson = new Gson();
        Log.d("parentParent", System.currentTimeMillis() + util.getParentParent(records));
        this.records = records;
    }

    public void setIsRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    @Override
    public MusicListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.item_music, viewGroup, false);
        return new MusicListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicListAdapter.ViewHolder viewHolder, int i) {

        final View itemView = viewHolder.itemView;
        final int position = viewHolder.getAdapterPosition();

        if (getItemViewType(position) == FOLDER_UP) {
            viewHolder.albumCover.setImageResource(0);
            viewHolder.songName.setText("...");
            viewHolder.songName.setTextColor(App.getContext().getResources().getColor(R.color.colorAccent));
            viewHolder.duration.setVisibility(View.GONE);
            viewHolder.artistName.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String path = util.getParentParent(records);
                    if (path.length() > 0) {
                        util.getMusicList(util.getParentParent(records));
                    }
                }
            });

        } else {
            final Record record = records.get(position - (isRoot ? 0 : 1));
            viewHolder.songName.setText(record.getSongName());
            if (!record.isFolder()) {
                Glide
                        .with(App.getContext())
                        .load(record.getAlbumCoverResource())
                        .asBitmap()
                        .placeholder(R.drawable.ic_music_note_black_24dp)
                        .into(viewHolder.albumCover);
                viewHolder.artistName.setText(record.getArtistName());
                viewHolder.songName.setTextColor(App.getContext().getResources().getColor(android.R.color.white));
                viewHolder.duration.setText(Util.getInstance().durationFormat(record.getDuration()));
                viewHolder.duration.setVisibility(View.VISIBLE);
                viewHolder.artistName.setVisibility(View.VISIBLE);
            } else {
                viewHolder.albumCover.setImageResource(R.drawable.ic_folder_open_black_24dp);
                viewHolder.artistName.setVisibility(View.GONE);
                viewHolder.duration.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (record.isFolder()) {
                        util.getMusicList(record.getPath());
                    } else {

                        PlayerFragment fragment = new PlayerFragment();
                        Bundle bundle = new Bundle();
                        Type listType = new TypeToken<List<Record>>() {
                        }.getType();
                        List<Record> recordsOnly = new ArrayList<>();
                        for (int j = 0; j < records.size(); j++) {
                            if (!records.get(j).isFolder()){
                                recordsOnly.add(records.get(j));
                            }
                        }
                        bundle.putString(RECORDS, gson.toJson(recordsOnly, listType));

                        for (int j = 0; j < recordsOnly.size(); j++) {
                            if (recordsOnly.get(j).equals(record)){
                                bundle.putInt(RECORD, j);
                            }
                        }
                        fragment.setArguments(bundle);
                        Util.getInstance().showFragment(fragment, RECORD);

                    }
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (isRoot) {
            return FOLDER;
        } else {
            if (position == 0) {
                return FOLDER_UP;
            } else {
                return FOLDER;
            }
        }
    }

    @Override
    public int getItemCount() {
        return records.size() + (isRoot ? 0 : 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView albumCover;
        private TextView artistName;
        private TextView songName;
        private TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.albumCover = itemView.findViewById(R.id.album_cover);
            this.songName = itemView.findViewById(R.id.song_name);
            this.artistName = itemView.findViewById(R.id.artist_name);
            this.duration = itemView.findViewById(R.id.duration);
        }
    }
}
