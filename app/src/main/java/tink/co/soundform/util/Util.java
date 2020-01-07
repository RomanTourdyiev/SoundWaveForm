package tink.co.soundform.util;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import tink.co.soundform.App;
import tink.co.soundform.R;
import tink.co.soundform.Record;
import tink.co.soundform.callback.MusicListReadyCallback;
import tink.co.soundform.ui.fragment.PlayerFragment;

import static tink.co.soundform.Config.RECORD;
import static tink.co.soundform.Config.RECORDS;
import static tink.co.soundform.Config.REQUEST_PERMISSIONS_CODE;

/**
 * Created by Tourdyiev Roman on 2020-01-04.
 */
public class Util {

    private static Gson gson;
    private static FragmentManager fragmentManager;
    private MusicListReadyCallback musicListReadyCallback;

    public void setMusicListReadyCallback(MusicListReadyCallback musicListReadyCallback) {
        this.musicListReadyCallback = musicListReadyCallback;
    }

    private static Util instance = new Util();

    private Util() {

    }

    public static Util getInstance() {
        gson = new Gson();
        fragmentManager = App.getActivity().getSupportFragmentManager();
        return instance;
    }

    public void permissionsRequestDialog(final String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(App.getActivity());
        builder.setTitle(App.getActivity().getResources().getString(R.string.storage_permission));
        builder.setMessage(App.getActivity().getResources().getString(R.string.permission_message));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(App.getActivity(),
                        new String[]{code},
                        REQUEST_PERMISSIONS_CODE);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar snackbar = Snackbar.make(
                        App.getActivity().findViewById(android.R.id.content),
                        App.getContext().getResources().getString(R.string.permissions_request_cancel_warning),
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(App.getContext().getResources().getString(android.R.string.yes),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(App.getActivity(),
                                        new String[]{code},
                                        REQUEST_PERMISSIONS_CODE);
                            }
                        });
                snackbar.show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showFragment(final Fragment fragment, final String TAG) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showFragmentImmediately(fragment, TAG);
            }
        }, 300);
    }

    public void showFragmentImmediately(Fragment fragment, String TAG) {
        fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment, TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(TAG)
                .commit();
    }

    public String durationFormat(long duration) {

        int h = (int) TimeUnit.MILLISECONDS.toHours(duration);
        duration -= (int) TimeUnit.HOURS.toMillis(h);
        int m = (int) TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= (int) TimeUnit.MINUTES.toMillis(m);
        int s = (int) TimeUnit.MILLISECONDS.toSeconds(duration);

        return h > 0 ? String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s) : String.format(Locale.ENGLISH, "%02d:%02d", m, s);
    }

    public void getMusicList(final String path) {
        Log.d("pathMusic", path);

        if (musicListReadyCallback != null) {
            musicListReadyCallback.OnStartMusicList();
        }

        if (path.equalsIgnoreCase("/storage/emulated") || path.equalsIgnoreCase("/storage")) {
            getStoragesList();
        } else {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    List<Record> records = new ArrayList<>();
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                    try {
                        File rootFolder = new File(path);
                        File[] files = rootFolder.listFiles();

                        for (File file : files) {

                            Record record = new Record();
                            record.setFolder(file.isDirectory());
                            record.setPath(file.getAbsolutePath());
                            record.setSongName(file.getName());

                            if (file.isDirectory()) {
                                records.add(record);
                            } else {
                                if (file.getName().endsWith(".mp3")) {
                                    mmr.setDataSource(file.getAbsolutePath());
                                    record.setSongName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                                    record.setAlbumCover(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                                    record.setAlbumCoverResource(mmr.getEmbeddedPicture());
                                    record.setArtistName(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                                    record.setDuration(Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                                    records.add(record);
                                }
                            }

                        }

                    } catch (Exception e) {

                    }

                    if (musicListReadyCallback != null) {
                        Collections.sort(records, new Comparator<Record>() {
                            @Override
                            public int compare(Record record1, Record record2) {
                                int c =  Boolean.compare(record2.isFolder(),record1.isFolder());
                                if (c == 0){
                                    c = record1.getSongName().compareToIgnoreCase(record2.getSongName());
                                }
                                return c;
                            }
                        });
                        musicListReadyCallback.OnMusicListReady(records);
                    }

                }
            });
        }
    }

    public void getStoragesList() {
        List<Record> records = new ArrayList<>();

        String sdCard = getExternalStoragePath();
        Record record = new Record();
        record.setFolder(true);
        record.setPath(sdCard);
        record.setSongName("SDCard");
        records.add(record);

        File internal = Environment.getExternalStorageDirectory();
        record = new Record();
        record.setFolder(internal.isDirectory());
        record.setPath(internal.getAbsolutePath());
        record.setSongName(internal.getName());
        records.add(record);

        if (musicListReadyCallback != null) {
            musicListReadyCallback.OnMusicListReady(records);
        }

    }

    private static String getExternalStoragePath() {

        StorageManager mStorageManager = (StorageManager) App.getContext().getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getParentParent(List<Record> records) {
        if (records.size() == 0) {
            return "";
        }
        String path = records.get(0).getPath();
        String parent = path.substring(0, path.lastIndexOf("/"));
        return parent.substring(0, parent.lastIndexOf("/"));
    }

    public String getParent(List<Record> records) {
        if (records.size() == 0) {
            return "";
        }
        String path = records.get(0).getPath();
        return path.substring(0, path.lastIndexOf("/"));
    }

    public void reloadPlayer(List<Record> records, int position) {
        final PlayerFragment fragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        Type listType = new TypeToken<List<Record>>() {
        }.getType();
        bundle.putString(RECORDS, gson.toJson(records, listType));
        bundle.putInt(RECORD, position);
        fragment.setArguments(bundle);
        App.getActivity().onBackPressed();

        showFragmentImmediately(fragment, RECORD);
    }
}
