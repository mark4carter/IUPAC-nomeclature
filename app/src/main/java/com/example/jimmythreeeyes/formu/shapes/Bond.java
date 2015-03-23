package com.example.jimmythreeeyes.formu.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by JimmyThreeEyes on 12/15/2014.
 */
public abstract class Bond {

    float x, y;
    Paint paint = new Paint();
    int borderColor;

    public Bond(float x, float y){
        this.x = x;
        this.y = y;
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);

    }

    public abstract void draw(Canvas c);


    public void setBorderColor (int borderColor){
        this.borderColor = borderColor;
    }

    public int getBorderColor(){
        return borderColor;
    }

    public float getX(){
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY(){
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
