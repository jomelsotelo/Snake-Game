package com.example.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable, Updatable {

    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;

    // Typeface for custom font
    private Typeface mCustomFont;
    // Background image
    private Bitmap mBackgroundBitmap;

    private int pauseX;
    private int pauseY;

    private boolean mNewGame = true;

    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);
        init(context, size);
    }

    private void init(Context context, Point size) {
        loadCustomFont(context);
        loadBackgroundImage(context);
        calcBlockSize(size);
        initializeSoundPool();
        initializeDrawingObjects();
        callGameObjectConstructors(context, size);
    }

    private void loadCustomFont(Context context) {
        AssetManager assetManager = context.getAssets();
        mCustomFont = Typeface.createFromAsset(assetManager, "Custom.ttf");
    }

    private void loadBackgroundImage(Context context) {
        mBackgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);
    }

    private void calcBlockSize(Point size) {
        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;
    }

    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSP = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
    }

    private void initializeDrawingObjects() {
        mSurfaceHolder = getHolder();
        mPaint = new Paint();
        // Set custom font to Paint
        mPaint.setTypeface(mCustomFont);
    }

    private void callGameObjectConstructors(Context context, Point size) {
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        mApple = new Apple(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
        mSnake = new Snake(context, new Point(NUM_BLOCKS_WIDE, mNumBlocksHigh), blockSize);
    }

    // Called to start a new game
    public void newGame() {
        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        // Get the apple ready for dinner
        mApple.spawn();
        // Reset the mScore
        mScore = 0;
        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }

    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if (!mPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }
            draw();
        }
    }

    // Check to see if it is time for an update
    public boolean updateRequired() {
        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if (mNextFrameTime <= System.currentTimeMillis()) {
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime = System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }
        return false;
    }

    // Update all the game objects
    public void update() {

        // Move the snake
        mSnake.move();

        // Did the head of the snake eat the apple?
        if (mSnake.checkDinner(mApple.getLocation())) {
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();

            // Add to mScore
            mScore = mScore + 1;

            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);
            mNewGame = true;
            mPaused = true;
        }

    }

    // Do all the drawing
    public void draw() {
        // Get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();
            drawBackgroundImage();
            drawScreenItems();
            drawPauseScreen();
        }
    }

    private void drawBackgroundImage() {
        Rect destRect = new Rect(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
        mCanvas.drawBitmap(mBackgroundBitmap, null, destRect, null);
    }

    private void drawScreenItems() {
        // Draw the score
        draw(mScore);
        // Draws the name on the top right corner
        mPaint.setTextSize(70);
        mCanvas.drawText(getResources().getString(R.string.names), mCanvas.getWidth()-540, 70, mPaint);
        // Draw the apple and the snake
        draw(mApple);
        draw(mSnake);
    }

    private void drawPauseScreen() {
        drawPauseButton();
        drawPauseText();
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    private void drawPauseButton(){
        pauseX = 20;
        pauseY = mCanvas.getHeight() - 20;

        if (!mPaused) {
            mPaint.setTextSize(70);
            mCanvas.drawText(getResources().getString(R.string.pause), pauseX, pauseY, mPaint);
        }
    }

    private void drawPauseText(){
        if (mPaused && mNewGame) {
            // Set the size and color of the mPaint for the text
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(250);
            mCanvas.drawText(getResources().getString(R.string.tap_to_play), 400, 600, mPaint);
        }
        else if (mPaused && !mNewGame) {
            mPaint.setTextSize(250);
            mCanvas.drawText(getResources().getString(R.string.resume), 600, 600, mPaint);
        }
    }

    // Overloaded draw method to draw snake
    public void draw(Snake snake) {
        // Draw the snake
        snake.draw(mCanvas, mPaint);
    }

    // Overloaded draw method to draw apple
    public void draw(Apple apple) {
        // Draw the apple
        apple.draw(mCanvas, mPaint);
    }

    // Overloaded draw method to draw score
    public void draw(int score) {
        // Set the size and color of the mPaint for the text
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        mPaint.setTextSize(120);
        // Draw the score
        mCanvas.drawText("" + score, 20, 120, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int touchX = (int) motionEvent.getX();
        int touchY = (int) motionEvent.getY();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused && mNewGame==false) {
                    mPaused = false;
                    return true;
                } else {
                    // If the game is running, it checks if the touch event is within the pause button
                    if (touchX >= pauseX && touchX <= pauseX + 220 &&
                            touchY <= pauseY && touchY >= pauseY - 100 && !mSnake.detectDeath()) {
                        mPaused = true;
                        return true;
                    }
                }
                if (mPaused && mNewGame==true) {
                    mPaused = false;
                    newGame();
                    mNewGame = false;
                    return true;
                }
                // If the touch event is not within any button and the game is running, let the Snake class handle the input
                if (!mPaused) {
                    mSnake.switchHeading(motionEvent);
                }
                break;
            default:
                break;
        }
        return true;
    }

    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}