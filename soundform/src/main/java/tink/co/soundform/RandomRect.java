package tink.co.soundform;

/**
 * Created by Tourdyiev Roman on 2019-09-16.
 */
public class RandomRect {

    private float delta;
    private int chuncks;
    private float[] chunckHeights;

    public RandomRect(float delta, int chuncks, float[] chunckHeights) {
        this.delta = delta;
        this.chuncks = chuncks;
        this.chunckHeights = chunckHeights;
    }

    private RandomRect() {
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public int getChuncks() {
        return chuncks;
    }

    public void setChuncks(int chuncks) {
        this.chuncks = chuncks;
    }

    public float[] getChunckHeights() {
        return chunckHeights;
    }

    public void setChunckHeights(float[] chunckHeights) {
        this.chunckHeights = chunckHeights;
    }
}
