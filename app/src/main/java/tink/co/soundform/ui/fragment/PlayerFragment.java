package tink.co.soundform.ui.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tink.co.soundform.App;
import tink.co.soundform.PlayerVisualizerReadyCallback;
import tink.co.soundform.PlayerVisualizerView;
import tink.co.soundform.R;
import tink.co.soundform.Record;
import tink.co.soundform.callback.BackCallback;
import tink.co.soundform.callback.PlaybackCallback;
import tink.co.soundform.util.RecordUtils;
import tink.co.soundform.util.Util;

import static tink.co.soundform.Config.MUSIC_LIST;
import static tink.co.soundform.Config.PATH;
import static tink.co.soundform.Config.RECORD;
import static tink.co.soundform.Config.RECORDS;

/**
 * Created by Tourdyiev Roman on 2020-01-07.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, PlaybackCallback, PlayerVisualizerReadyCallback, BackCallback {

    private Gson gson;
    private Util util;
    private RecordUtils recordUtils;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    private ImageView close;
    private TextView songTitle;
    private TextView artist;
    private TextView duration;
    private TextView timeProgress;
    private ImageView albumCover;
    private ImageView albumCoverLarge;
    private ImageView rew15;
    private ImageView rev;
    private ImageView play;
    private ImageView fwd;
    private ImageView fwd15;
    private CardView speedx05;
    private CardView speedx1;
    private CardView speedx2;
    private ProgressBar progressBar;
    private ProgressBar playProgressBar;
    private ProgressBar playerVisualizerLoading;
    private PlayerVisualizerView playerVisualizerView;

    private Record record;
    private List<Record> records;
    private int position;
    private boolean buffered = false;
    private boolean prepared = false;

    @Override
    public void onProgressChanged(final int currentPosition, final int mediaFileLengthInMilliseconds) {
        final int progress = (int) Math.ceil(((float) TimeUnit.MILLISECONDS.toSeconds(currentPosition) /
                TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds)) * 100);
        App.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
                playerVisualizerView.updatePlayerPercent(progress / 100f);
                timeProgress.setText(util.durationFormat(currentPosition));
                Log.d("progresscheck", currentPosition + "");
                Log.d("progresscheck", mediaFileLengthInMilliseconds + "");
                if (TimeUnit.MILLISECONDS.toSeconds(currentPosition) == TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds)) {
                    togglePlayControls(false);
                }
            }
        });
    }

    @Override
    public void onPlayPause(final boolean nowPlaying) {
        App.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                togglePlayControls(nowPlaying);
            }
        });
    }

    @Override
    public void onMediaPlayerCompletion() {
        togglePlayControls(false);
    }

    @Override
    public void onMediaPlayerPrepared() {
        prepared = true;
    }

    @Override
    public void onBufferingUpdate(int percent) {
        buffered = true;
        progressBar.setSecondaryProgress(percent);
    }

    @Override
    public void onPlayerVisualizerReady() {
        App.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerVisualizerLoading.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                playProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.close) {
            App.getActivity().onBackPressed();
        } else if (v.getId() == R.id.rew_15) {
            if (!prepared) return;
            recordUtils.seekBy((int) TimeUnit.SECONDS.toMillis(-15));
        } else if (v.getId() == R.id.rev) {
            recordUtils.releaseMediaPlayer();
            recordUtils.setPlaybackCallback(null);
            playerVisualizerView.setPlayerVisualizerReadyCallback(null);
            util.reloadPlayer(records, position - 1);
        } else if (v.getId() == R.id.play) {
            if (!buffered) playProgressBar.setVisibility(View.VISIBLE);
            if (!buffered) play.setVisibility(View.GONE);
            recordUtils.toggleAudio(record);
        } else if (v.getId() == R.id.fwd) {
            recordUtils.releaseMediaPlayer();
            recordUtils.setPlaybackCallback(null);
            playerVisualizerView.setPlayerVisualizerReadyCallback(null);
            util.reloadPlayer(records, position + 1);
        } else if (v.getId() == R.id.fwd_15) {
            if (!prepared) return;
            recordUtils.seekBy((int) TimeUnit.SECONDS.toMillis(15));
        } else if (v.getId() == R.id.speed_x05) {
            if (!prepared) return;
            toggleSpeedControls();
            recordUtils.setPlaybackSpeed(0.5f);
            speedx05.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.colorAccent));
        } else if (v.getId() == R.id.speed_x1) {
            if (!prepared) return;
            toggleSpeedControls();
            recordUtils.setPlaybackSpeed(1.0f);
            speedx1.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.colorAccent));
        } else if (v.getId() == R.id.speed_x2) {
            if (!prepared) return;
            toggleSpeedControls();
            recordUtils.setPlaybackSpeed(2.0f);
            speedx2.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public void onBackPressed() {
        recordUtils.stopAudio(record);
        String path = util.getParent(records);
        if (path.length() > 0) {
            Bundle bundle = new Bundle();
            bundle.putString(PATH, path);
            MusicListFragment fragment = new MusicListFragment();
            fragment.setArguments(bundle);
            util.showFragment(fragment, MUSIC_LIST);
        }
    }

    private void toggleSpeedControls() {
        speedx05.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.inactive));
        speedx1.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.inactive));
        speedx2.setCardBackgroundColor(App.getContext().getResources().getColor(R.color.inactive));
        togglePlayControls(true);
    }

    private void togglePlayControls(boolean nowPlaying) {
        playProgressBar.setVisibility(View.GONE);
        play.setVisibility(View.VISIBLE);
        play.setColorFilter(App.getContext().getResources().getColor(nowPlaying ? R.color.colorAccent : android.R.color.white));
        play.setImageResource(nowPlaying ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        gson = new Gson();
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(RECORDS)) {
                Type listType = new TypeToken<List<Record>>() {
                }.getType();
                records = gson.fromJson(bundle.getString(RECORDS), listType);
                position = bundle.getInt(RECORD);
                record = records.get(position);
                Log.d("positionPlayer", record.toString());
            }
        }

        util = Util.getInstance();
        recordUtils = RecordUtils.getInstance();
        recordUtils.initMediaPlayer();
        recordUtils.setPlaybackCallback(this);
        App.getActivity().setBackCallback(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        findViews(rootView);
        initViews();
        return rootView;
    }

    @Override
    public void onDetach() {
        Log.d("recordFragment", "onDetach");
        recordUtils.releaseMediaPlayer();
        App.getActivity().setBackCallback(null);
        super.onDetach();
    }


    protected void findViews(View rootView) {
        close = rootView.findViewById(R.id.close);
        songTitle = rootView.findViewById(R.id.song_title);
        artist = rootView.findViewById(R.id.artist);
        duration = rootView.findViewById(R.id.duration);
        timeProgress = rootView.findViewById(R.id.time_progress);
        albumCover = rootView.findViewById(R.id.album_cover);
        albumCoverLarge = rootView.findViewById(R.id.album_cover_large);
        rew15 = rootView.findViewById(R.id.rew_15);
        rev = rootView.findViewById(R.id.rev);
        play = rootView.findViewById(R.id.play);
        fwd = rootView.findViewById(R.id.fwd);
        fwd15 = rootView.findViewById(R.id.fwd_15);
        speedx05 = rootView.findViewById(R.id.speed_x05);
        speedx1 = rootView.findViewById(R.id.speed_x1);
        speedx2 = rootView.findViewById(R.id.speed_x2);
        progressBar = rootView.findViewById(R.id.progress_bar);
        playProgressBar = rootView.findViewById(R.id.play_progress);
        playerVisualizerView = rootView.findViewById(R.id.player_visualizer_view);
        playerVisualizerLoading = rootView.findViewById(R.id.player_visualizer_loading);
    }

    private void initViews() {

        close.setOnClickListener(this);
        rew15.setOnClickListener(this);
        play.setOnClickListener(this);
        fwd15.setOnClickListener(this);
        speedx05.setOnClickListener(this);
        speedx1.setOnClickListener(this);
        speedx2.setOnClickListener(this);

        timeProgress.setText("00:00");

        if (record != null) {
            songTitle.setText(record.getSongName());
            artist.setText(record.getArtistName());
            duration.setText(util.durationFormat(record.getDuration()));

            Glide
                    .with(App.getContext())
                    .load(record.getAlbumCoverResource())
                    .asBitmap()
                    .placeholder(getResources().getDrawable(R.drawable.ic_music_note_black_24dp))
                    .into(albumCover);

            Glide
                    .with(App.getContext())
                    .load(record.getAlbumCoverResource())
                    .asBitmap()
                    .placeholder(getResources().getDrawable(R.drawable.ic_music_note_black_24dp))
                    .into(albumCoverLarge);

            if (position == 0) {
                // first record
                rev.setOnClickListener(null);
                rev.setAlpha(0.1f);
                fwd.setOnClickListener(this);
            } else if (position == records.size() - 1) {
                // last record
                rev.setOnClickListener(this);
                fwd.setOnClickListener(null);
                fwd.setAlpha(0.1f);
            } else {
                // somewhere in between
                rev.setOnClickListener(this);
                fwd.setOnClickListener(this);
            }
        }

        progressBar.setMax(100);
        playerVisualizerView.updateVisualizer(App.getActivity(), record);
        playerVisualizerView.setPlayerVisualizerReadyCallback(this);
        play.setVisibility(View.GONE);
        playProgressBar.setVisibility(View.VISIBLE);
    }

}
