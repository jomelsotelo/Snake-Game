package com.example.snake;

import android.graphics.Point;
abstract class GameObject  {
    protected Point range;
    protected int size;

    public GameObject(Point location, int size){
        this.range = location;
        this.size = size;
    }

    public Point getLocation(){
        return range;
    }

    public int getSize(){
        return size;
    }
}
