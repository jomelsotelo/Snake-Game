package com.example.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Apple extends GameObject implements Drawable{

    private static final int HIDE_LOCATION_X = -10;
    private static final int OFFSET = 1;

    // The location of the apple on the grid
    // Not in pixels
    private Point location = new Point();

    // An image to represent the apple
    private Bitmap mBitmapApple;

    /// Set up the apple in the constructor
    Apple(Context context, Point sr, int s){
        super(sr, s);
        // Hide the apple off-screen until the game starts
        location.x = HIDE_LOCATION_X;

        // Load the image to the bitmap
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);

        // Resize the bitmap
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    // Overloaded the apple constructor to specify the location explicitly
    Apple(Context context, Point sr, int s, Point initialLocation) {
        super(sr, s);
        location = initialLocation;

        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    // Overloading the apple constructor to specify the location using individual coordinates
    Apple(Context context, Point sr, int s, int x, int y) {
        super(sr, s);
        location.x = x;
        location.y = y;

        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    // This is called every time an apple is eaten
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(range.x) + OFFSET;
        location.y = random.nextInt(range.y - OFFSET) + OFFSET;
    }

    //Dynamic Polymorphism
    @Override
    public Point getLocation(){
        return location;
    }

    // Draw the apple
    @Override
    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(mBitmapApple,
                location.x * size, location.y * size, paint);

    }

}