package com.example.jimmythreeeyes.formu.shapes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by JimmyThreeEyes on 12/15/2014.
 */
public class Line extends Bond {

    float x2, y2;


    public Line(float x, float y, float x2, float y2) {
        super(x, y);
        this.x2 = x2;
        this.y2 = y2;
    }

    public void draw(Canvas c){
        c.drawLine(x, y, x2, y2, paint);
    }

    public void setX2(float x2){
        this.x2 = x2;
    }

    public void setY2(float y2){
        this.y2 = y2;
    }

    public float getX2(){
        return x2;
    }

    public float getY2(){
        return y2;
    }


}
