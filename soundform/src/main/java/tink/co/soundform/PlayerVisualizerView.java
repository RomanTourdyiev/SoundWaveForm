package tink.co.soundform;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static tink.co.soundform.Config.VISUALIZER_HEIGHT;

/**
 * Created by Tourdyiev Roman on 2019-09-16.
 */
public class PlayerVisualizerView extends View {

    private Context context;
    private Util util;
    private PlayerVisualizerReadyCallback playerVisualizerReadyCallback;
    private Random random;
    /**
     * bytes array converted from file.
     */
    private byte[] bytes;
    private List<RandomRect> randomRects;

    /**
     * Percentage of audio sample scale
     * Should updated dynamically while audioPlayer is played
     */
    private float denseness;

    /**
     * Canvas painting for sample scale, filling played part of audio sample
     */
    private Paint playedStatePainting = new Paint();
    /**
     * Canvas painting for sample scale, filling not played part of audio sample
     */
    private Paint notPlayedStatePainting = new Paint();

    private int width;
    private int height;

    public PlayerVisualizerView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PlayerVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setPlayerVisualizerReadyCallback(PlayerVisualizerReadyCallback callback) {
        this.playerVisualizerReadyCallback = callback;
    }

    private void init() {
        util = Util.getInstance();
        random = new Random();
        bytes = null;
        playedStatePainting.setStrokeWidth(1f);
        playedStatePainting.setAntiAlias(true);
        playedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.inactive));
        notPlayedStatePainting.setStrokeWidth(1f);
        notPlayedStatePainting.setAntiAlias(true);
        notPlayedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(final AppCompatActivity appCompatActivity, final Record record) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(record.getPath());
                final byte[] bytes = util.fileToBytes(file);
                appCompatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData(bytes);
                    }
                });

                if (playerVisualizerReadyCallback != null) {
                    playerVisualizerReadyCallback.onPlayerVisualizerReady();
                }
            }
        });
    }

    private void setData(byte[] bytes) {
        this.bytes = bytes;
        invalidate();
    }

    /**
     * Update player percent. 0 - file not played, 1 - full played
     *
     * @param percent
     */
    public void updatePlayerPercent(float percent) {
        denseness = (int) Math.ceil(width * percent);
        if (denseness < 0) {
            denseness = 0;
        } else if (denseness > width) {
            denseness = width;
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (randomRects == null) {
            randomRects = new ArrayList<>();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null || width == 0) {
            return;
        }
        float totalBarsCount = width / util.dp(context, 6);
        if (totalBarsCount <= 0.1f) {
            return;
        }
        byte value;
        int samplesCount = (bytes.length * 8 / 5);
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        int y = (height - util.dp(context, VISUALIZER_HEIGHT)) / 2;
        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {

            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int bitPointer = a * 5;
            int byteNum = bitPointer / Byte.SIZE;
            int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
            int currentByteCount = Byte.SIZE - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;
            value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int b = 0; b < drawBarCount; b++) {

                int x = barNum * util.dp(context, 6);
                Log.d("printB", System.currentTimeMillis() + "_" + String.valueOf(barNum));
                float left = x;
                float top = y + util.dp(context, VISUALIZER_HEIGHT - Math.max(1, VISUALIZER_HEIGHT * value / 31.0f));
                float right = x + util.dp(context, 4);
                float bottom = y + util.dp(context, VISUALIZER_HEIGHT);

                float delta;
                if (randomRects.size() == barNum) {
                    float barHeight = bottom - top;
                    delta = util.dp(context, VISUALIZER_HEIGHT) / 2f - barHeight / 2f;
                    // add some randomness
                    // find out how much we can move
                    float spaceRemain = util.dp(context, VISUALIZER_HEIGHT) - barHeight;
                    // we can move up to half of that
                    spaceRemain = spaceRemain / 2f;
                    // restrict random number, so waveform looks more uniform
                    if (spaceRemain > util.dp(context, 8)) {
                        spaceRemain = util.dp(context, 8);
                    }
                    float randomDelta = -spaceRemain + 2 * random.nextFloat() * spaceRemain;
                    delta = delta + randomDelta;

                    int chuncks = 1;
                    // height is 1/3
                    if (barHeight <= util.dp(context, VISUALIZER_HEIGHT) / 3f && barHeight > util.dp(context, VISUALIZER_HEIGHT) / 4f) {
                        chuncks = random.nextInt(3 - 1) + 1; // could be 2 or 1 chuncks
                    } else if (barHeight > util.dp(context, VISUALIZER_HEIGHT) / 3f) {
                        chuncks = random.nextInt(4 - 1) + 1; // could be 3 to 1 chuncks
                    }

                    Log.d("randomRect", String.valueOf(chuncks));

                    float[] chunckHeights = new float[chuncks];
                    // create set of rounded rects
                    for (int i = 0; i < chuncks - 1; i++) {
                        float chunckHeight = util.dp(context, 8);
                        if (barHeight / chuncks > chunckHeight) {
                            chunckHeight = util.dp(context, 8) + random.nextFloat() * (barHeight / chuncks - util.dp(context, 8));
                        }
                        chunckHeights[i] = chunckHeight;
                    }
                    randomRects.add(new RandomRect(delta, chuncks, chunckHeights));
                } else {
                    delta = randomRects.get(barNum).getDelta();
                }

                top = top - delta;
                bottom = bottom - delta;

                float r = util.dp(context, 2);

                float[] chunckHeights = randomRects.get(barNum).getChunckHeights();
                for (int i = 0; i < chunckHeights.length; i++) {
                    float corrTop = 0;
                    for (int j = 0; j < i; j++) {
                        corrTop = corrTop + chunckHeights[j];
                    }
                    if (x < denseness && x + util.dp(context, 2) < denseness) {
                        canvas.drawRoundRect(left,
                                top + corrTop,
                                right,
                                (i != chunckHeights.length - 1 ? top + corrTop + chunckHeights[i] - util.dp(context, 1) : bottom),
                                r, r, notPlayedStatePainting);
                    } else {
                        canvas.drawRoundRect(
                                left,
                                top + corrTop,
                                right,
                                (i != chunckHeights.length - 1 ? top + corrTop + chunckHeights[i] - util.dp(context, 1) : bottom),
                                r, r, playedStatePainting);
                        if (x < denseness) {
                            canvas.drawRoundRect(left,
                                    top + corrTop,
                                    right,
                                    (i != chunckHeights.length - 1 ? top + corrTop + chunckHeights[i] - util.dp(context, 1) : bottom),
                                    r, r, notPlayedStatePainting);
                        }
                    }
                }


                barNum++;
            }

        }
    }


}
