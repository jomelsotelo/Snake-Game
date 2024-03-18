package com.example.snake;

import android.graphics.Point;
abstract class GameObject  {
    protected Point location;
    protected int size;

    public GameObject(Point location, int size){
        this.location = location;
        this.size = size;
    }

    public Point getLocation(){
        return location;
    }

    public int getSize(){
        return size;
    }

}
