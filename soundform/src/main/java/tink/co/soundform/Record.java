package tink.co.soundform;

import androidx.annotation.Nullable;

/**
 * Created by Tourdyiev Roman on 2020-01-06.
 */
public class Record {

    private String albumCover;
    private String artistName;
    private String songName;
    private String path;
    private long duration;
    private boolean isFolder;
    private byte[] albumCoverResource;

    public Record() {
    }

    public Record(String albumCover, String artistName, String songName, String path, long duration, boolean isFolder, byte[] albumCoverResource) {
        this.albumCover = albumCover;
        this.artistName = artistName;
        this.songName = songName;
        this.path = path;
        this.duration = duration;
        this.isFolder = isFolder;
        this.albumCoverResource = albumCoverResource;
    }

    public String getAlbumCover() {
        return albumCover;
    }

    public void setAlbumCover(String albumCover) {
        this.albumCover = albumCover;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getAlbumCoverResource() {
        return albumCoverResource;
    }

    public void setAlbumCoverResource(byte[] albumCoverResource) {
        this.albumCoverResource = albumCoverResource;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Record) {
            Record record = (Record) obj;
            return record.getPath().equalsIgnoreCase(this.path);
        }
        return false;
    }
}
