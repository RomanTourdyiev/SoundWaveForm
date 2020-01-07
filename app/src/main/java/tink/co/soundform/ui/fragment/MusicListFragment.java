package tink.co.soundform.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tink.co.soundform.App;
import tink.co.soundform.R;
import tink.co.soundform.Record;
import tink.co.soundform.callback.BackCallback;
import tink.co.soundform.callback.MusicListReadyCallback;
import tink.co.soundform.ui.adapter.MusicListAdapter;
import tink.co.soundform.util.Util;

import static tink.co.soundform.Config.PATH;

/**
 * Created by Tourdyiev Roman on 2020-01-06.
 */
public class MusicListFragment extends Fragment implements MusicListReadyCallback, BackCallback {

    private Util util;
    private MusicListAdapter musicListAdapter;

    private RecyclerView recycler;
    private ProgressBar progressBar;

    private List<Record> records = new ArrayList<>();

    @Override
    public void OnMusicListReady(final List<Record> records) {
        Log.d("OnMusicList", "OnMusicListReady");
        this.records.clear();
        this.records.addAll(records);
        Log.d("musicCheck", records.size() + "");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("parentParent", System.currentTimeMillis() + util.getParentParent(records));
                musicListAdapter.setIsRoot(util.getParentParent(records).equalsIgnoreCase(""));
                musicListAdapter.notifyDataSetChanged();
                recycler.getLayoutManager().scrollToPosition(0);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void OnStartMusicList() {
        Log.d("OnMusicList", "OnStartMusicList");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        String path = util.getParentParent(records);
        if (path.length() > 0) {
            util.getMusicList(path);
        } else {
            App.getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        util = Util.getInstance();
        util.setMusicListReadyCallback(this);
        App.getActivity().setBackCallback(this);
        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);
        findViews(rootView);
        initViews();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            util.getMusicList(bundle.getString(PATH));
        } else {
            util.getStoragesList();
        }
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        util.setMusicListReadyCallback(null);
        App.getActivity().setBackCallback(null);
    }

    private void findViews(View rootView) {
        recycler = rootView.findViewById(R.id.recycler_view);
        progressBar = rootView.findViewById(R.id.progress_bar);
    }

    private void initViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(App.getActivity());
        recycler.setLayoutManager(layoutManager);
        musicListAdapter = new MusicListAdapter(records);
        musicListAdapter.setIsRoot(true);
        recycler.setAdapter(musicListAdapter);
    }
}
