package com.example.jimmythreeeyes.formu;

        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;
        import android.content.Context;
        import android.util.AttributeSet;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.PorterDuff;
        import android.graphics.PorterDuffXfermode;
        import android.util.TypedValue;
        import android.widget.Toast;

        import com.example.jimmythreeeyes.formu.shapes.Bond;
        import com.example.jimmythreeeyes.formu.shapes.Line;

        import java.util.ArrayList;
        import java.util.Vector;

/**
 * Created by JimmyThreeEyes on 12/8/2014.
 */
public class DrawingView extends View{


    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize, lastBrushSize;

    private boolean erase=false;
    private boolean isFinalBond=false;

    public Bond tempBond;

    public ArrayList <Bond> bondList = new ArrayList <Bond>(1);
    public ArrayList <Float> possX = new ArrayList <Float>(1);
    public ArrayList <Float> possY = new ArrayList <Float>(1);
    public ArrayList <Float> endpointX = new ArrayList <Float>(1);
    public ArrayList <Float> endpointY = new ArrayList <Float>(1);

    public ArrayList <Float> coordListX1 = new ArrayList <Float>(1);
    public ArrayList <Float> coordListY1 = new ArrayList <Float>(1);
    public ArrayList <Float> coordListX2 = new ArrayList <Float>(1);
    public ArrayList <Float> coordListY2 = new ArrayList <Float>(1);

    public ArrayList <Float> tallyTally = new ArrayList <Float>(1);
    public ArrayList <Float> tallyX1 = new ArrayList <Float>(1);
    public ArrayList <Float> tallyY1 = new ArrayList <Float>(1);
    public ArrayList <Float> tallyX2 = new ArrayList <Float>(1);
    public ArrayList <Float> tallyY2 = new ArrayList <Float>(1);
    public ArrayList <Float> tallyK = new ArrayList <Float>(1);

    public ArrayList <Float> chainPointsX = new ArrayList <Float>(1);
    public ArrayList <Float> chainPointsY = new ArrayList <Float>(1);

    public ArrayList <Float> tempChainPointsX = new ArrayList <Float>(1);
    public ArrayList <Float> tempChainPointsY = new ArrayList <Float>(1);

    public ArrayList <Integer> groupList = new ArrayList<Integer>(1);

    public float highestTally = 0;

    public int firstGroup, secondGroup, group;

    public String disp, methType, preDisp;

    public int leftOrRight = 0; //0 = left, 1 = right

    public String test;

    boolean started = false;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected  void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    protected void onDraw(Canvas canvas) {
        //canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        //canvas.drawPath(drawPath, drawPaint);

        // isFinalBond tells if the user lifted finger to
        //    finalize direction of bond
        if (!isFinalBond) {
            if (tempBond != null) tempBond.draw(canvas);
        } else if (isFinalBond) {
            if (tempBond != null) {

                //add bond points to possible points
                possX.add(tempBond.getX());
                possY.add(tempBond.getY());
                possX.add(((Line)tempBond).getX2());
                possY.add(((Line)tempBond).getY2());

                listCoordinates(tempBond.getX(),  tempBond.getY(),
                        ((Line)tempBond).getX2(), ((Line)tempBond).getY2() );


                updateEndpoints(tempBond.getX(),  tempBond.getY(), ((Line)tempBond).getX2(), ((Line)tempBond).getY2());
                if (endpointX.size() == 1) {
                    endpointX.add(tempBond.getX());
                    endpointY.add(tempBond.getY());
                }




                Log.e("tag", "--Coordinates------");
                for (int i = 0; i < coordListX1.size(); i++){
                    Log.e("tag", "(" + coordListX1.get(i) + "," + coordListY1.get(i) + ") " +
                            "(" + coordListX2.get(i) + "," + coordListY2.get(i) + ")");
                }
                Log.e("tag", "--EndPoints--------" );
                for (int i = 0; i < endpointX.size(); i++){
                    Log.e("tag", "(" + endpointX.get(i) + "," + endpointY.get(i) + ")");
                }

                //tally method
                tallyUp(tempBond.getX(),  tempBond.getY(), ((Line)tempBond).getX2(), ((Line)tempBond).getY2());

                //add bond to list of bonds
                bondList.add(tempBond);
                tempBond = null;
                isFinalBond = false;
            }
        }

        //draw all bonds
        for (int i = 0; i < bondList.size(); i++){
            if(bondList.get(i) != null) {
                bondList.get(i).draw(canvas);
            }
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //drawPath.moveTo(touchX, touchY); <----- OLD PROGRAM

                touchX = Math.round(touchX);
                touchY = Math.round(touchY);

                // if possible points are listed
                // make sure new bonds are connected to old bonds
                if (possX.size() > 1) {

                    //dist formulas used to find closest point to where user touched
                    double dist = 0;
                    int endElement = 0;
                    for (int i = 0; i < possX.size(); i++) {
                        double tempDist = Math.sqrt(
                                Math.pow((possX.get(i)) - (touchX), 2) +
                                        Math.pow((possY.get(i)) - (touchY), 2));
                        if (i == 0) {
                            dist = tempDist;
                        } else {
                            if (tempDist < dist) {
                                dist = tempDist;
                                endElement = i;
                            }
                        }
                    }
                    touchX = possX.get(endElement);
                    touchY = possY.get(endElement);
                }

                //create bond
                tempBond = new Line(touchX, touchY, (touchX + 81), (touchY + 81));

                break;
            case MotionEvent.ACTION_MOVE:
               // drawPath.lineTo(touchX, touchY);

                if (tempBond != null) {
                    if (tempBond instanceof Line) {

                        //formulas used to make sure lines stay the same length
                        float tempTouchX = touchX - tempBond.getX();
                        float tempTouchY = touchY - tempBond.getY();

                        if (tempTouchX >= 80) tempTouchX = 160;
                        if (tempTouchX < 80 && tempTouchX > -80) tempTouchX = 0;
                        if (tempTouchX <= -80) tempTouchX = -160;

                        if (tempTouchY >= 80) tempTouchY = 160;
                        if (tempTouchY < 80 && tempTouchY > -80) tempTouchY = 0;
                        if (tempTouchY <= -80) tempTouchY = -160;

                        if (tempTouchX == 0 && tempTouchY == 0) {
                            tempTouchX = 160;
                            tempTouchY = 160;
                        }

                        ((Line) tempBond).setX2(tempBond.getX() + tempTouchX);
                        ((Line) tempBond).setY2(tempBond.getY() + tempTouchY);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //drawCanvas.drawPath(drawPath, drawPaint);
               // drawPath.reset();  <-----^^^^ OLD PROGRAM


                isFinalBond = true;
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setColor(String newColor) {
        //set color
        invalidate();

        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);

    }

    public void setBrushSize(float newSize) {
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setErase(boolean isErase) {
        //set erase true or false
        erase = isErase;

        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    // create new endpoints delete endpoints that are now connected points
    // needs to finished
    public void updateEndpoints(float x, float y, float x2, float y2) {

        for (int i = 0; i < endpointX.size(); i++) {
            if (x == endpointX.get(i)) {
                if (y == endpointY.get(i)){
                    endpointX.remove(i);
                    endpointY.remove(i);
                }
            }
        }

        endpointX.add(x2);
        endpointY.add(y2);


    }

    public void listCoordinates(float x1, float y1, float x2, float y2) {
        coordListX1.add(x1);
        coordListY1.add(y1);
        coordListX2.add(x2);
        coordListY2.add(y2);
    }

    public void tallyUp (float x1, float y1, float x2, float y2){
        if (started) {
            Log.e("tag", "--Calc Tally------");
            calcTally(x1, y1, x2, y2);
        } else {
            tallyTally.add(2f);
            tallyX1.add(x1);
            tallyY1.add(y1);
            tallyX2.add(x2);
            tallyY2.add(y2);
            started = true;
            Log.e("tag", "--First Tally------");
            for (int i = 0; i < tallyTally.size(); i++){
                Log.e("tag", "T = " + tallyTally.get(i) + " x1 = " + tallyX1.get(i) + " y1 = " + tallyY1.get(i) +
                            "    x2 = " + tallyX2.get(i) + " y2 = " + tallyY2.get(i));
            }

        }
    }

    public void calcTally(float x1, float y1, float x2, float y2){

        ////create BOOLEAN endpointDeleted?  to show if end point is deleted to avoid iteration through memory

        int tempTally = 0;

        ////create BOOLEAN endpointDeleted? to show if end point is deleted to avoid iteration through memory

        //check to see if endpoint...
        //if endpoint... delete any tallyEntries
        for (int i = 0; i < tallyX1.size(); i++) {
            if (tallyX1.get(i).equals(x1) && tallyY1.get(i).equals(y1)){
                tallyX1.remove(i);
                tallyY1.remove(i);
                tallyX2.remove(i);
                tallyY2.remove(i);
                tallyTally.remove(i);
                Log.e("tag", "--Removed Tally------");
                if (tallyTally.size() > 0) {
                    for (int j = 0; j < tallyTally.size(); j++) {
                        Log.e("tag", "T = " + tallyTally.get(j) + " x1 = " + tallyX1.get(j) + " y1 = " + tallyY1.get(j) +
                                "    x2 = " + tallyX2.get(j) + " y2 = " + tallyY2.get(j));
                    }
                    i -= 1;
                } else {
                    Log.e("tag", "--Empty Tally------");
                }
            } else if (tallyX2.get(i).equals(x1) && tallyY2.get(i).equals(y1)){
                tallyX1.remove(i);
                tallyY1.remove(i);
                tallyX2.remove(i);
                tallyY2.remove(i);
                tallyTally.remove(i);
                Log.e("tag", "--Removed Tally------");
                if (tallyTally.size() > 0) {
                    for (int j = 0; j < tallyTally.size(); j++) {
                        Log.e("tag", "T = " + tallyTally.get(j) + " x1 = " + tallyX1.get(j) + " y1 = " + tallyY1.get(j) +
                                "    x2 = " + tallyX2.get(j) + " y2 = " + tallyY2.get(j));
                    }
                    i -= 1;
                } else {
                    Log.e("tag", "--Empty Tally------");
                }
            }
        }

        Vector <Float> tempX = new Vector <Float>(1);
        Vector <Float> tempY = new Vector <Float>(1);
        Vector <Float> tempX2 = new Vector <Float>(1);
        Vector <Float> tempY2 = new Vector <Float>(1);
        Vector <Float> tempK = new Vector <Float>(1);
        //tempK shows how far it has iterated through the coordlist

        //the new endpoints
        //tallyX1.addElement(x2);
       // tallyY1.addElement(y2);

        //reinit tempVectors

        Log.e("tag", "x1 = " + x1 + " y1 = " + y1 +
                "    x2 = " + x2 + " y2 = " + y2);

        for (int i = 0; i < coordListX1.size(); i++) {



            //iterate through coordlist find which first-clic- point
            //the point is connected to
            if (((coordListX1.get(i) == x1 && coordListY1.get(i) == y1) &&
                    (coordListX2.get(i) != x2 || coordListY2.get(i) != y2)) ||
                        ((coordListX2.get(i) == x1 && coordListY2.get(i) == y1) &&
                            (coordListX1.get(i) != x2 || coordListY1.get(i) != y2))) {

                //if point is found, add one
                Log.e("tag", "--Found one point @i = " + i + ", add one tally------");
                tempTally += 1;


                if (x1 == coordListX1.get(i) && y1 == coordListY1.get(i)){
                    tempX.addElement(coordListX2.get(i));
                    tempY.addElement(coordListY2.get(i));
                    tempX2.addElement(coordListX1.get(i));
                    tempY2.addElement(coordListY1.get(i));
                    tempK.addElement(0f);
                    Log.e("tag", "--Adding temp entry first------");
                } else {
                    tempX.addElement(coordListX1.get(i));
                    tempY.addElement(coordListY1.get(i));
                    tempX2.addElement(coordListX2.get(i));
                    tempY2.addElement(coordListY2.get(i));
                    tempK.addElement(0f);
                    Log.e("tag", "--Adding temp entry else------");
                }

                for (int j = 0; j < tempX.size(); j++) {
                    for (int k=0; k < coordListX1.size(); k++){

                        Log.e("tag", "Starting loop; j = " +j+ " k = " + k);

                        Log.e("tag", "if (tempK.elementAt(" + j + ") == null || tempK.elementAt(" + j + ") <= 0)");

                        if (tempK.elementAt(j) == null || tempK.elementAt(j) <= 0) {
                            Log.e("tag", "- loop starting from beginning ");
                            if (tempK.elementAt(j) <= 0){
                                float temp = tempK.elementAt(j) + 1;
                                tempK.setElementAt(temp, j);
                            } else {  ///should we just set it to 1 regardless, why not???
                                tempK.setElementAt(1f, j);
                            }

                            Log.e("tag", "tempK.elementAt(" + j + ") = " + tempK.elementAt(j));
                        } else if (k == -2) {
                            Log.e("tag", "Continuation Loop, altering k");
                            k = Math.round(tempK.elementAt(j));
                            Log.e("tag", "k = " + k + " //means last tempX was deleted now we are returning to where last tempX left off");
                            tempK.setElementAt(k + 1f, j);
                        } else if (k > 0) {
                            tempK.setElementAt(k + 1f, j);
                        }

                        /*Log.e("tag", "coordListX1.elementAt(" + k + ") == tempX.elementAt(" + j + ") && coordListY1.elementAt(" + k + ") == tempY.elementAt(" + j + ")");
                        Log.e("tag", coordListX1.elementAt(k) + " == " + tempX.elementAt(j) + " && " + coordListY1.elementAt(k) + " == " + tempY.elementAt(j) );
                        if ((coordListX1.elementAt(k).equals(tempX.elementAt(j)) && coordListY1.elementAt(k).equals(tempY.elementAt(j)))) {
                            Log.e("tag","^^^TRUE");
                        } else {Log.e("tag","^^^False");}

                        Log.e("tag", "coordListX2.elementAt(" + k + ") != tempX2.elementAt(" + j + ") || coordListY2.elementAt(" + k + ") != tempY2.elementAt(" + j + ")");
                        Log.e("tag", coordListX2.elementAt(k) + " != " + tempX2.elementAt(j) + " || " + coordListY2.elementAt(k) + " != " + tempY2.elementAt(j) );
                        if ((coordListX2.elementAt(k).equals(tempX2.elementAt(j)) || coordListY2.elementAt(k).equals(tempY2.elementAt(j)))) {
                            Log.e("tag","^^^TRUE");
                        } else {Log.e("tag","^^^False");}

                        Log.e("tag", "coordListX2.elementAt(" + k + ") == tempX.elementAt(" + j + ") && coordListY2.elementAt(" + k + ") == tempY.elementAt(" + j + ")");
                        Log.e("tag", coordListX2.elementAt(k) + " == " + tempX.elementAt(j) + " && " + coordListY2.elementAt(k) + " == " + tempY.elementAt(j) );
                        if ((coordListX2.elementAt(k).equals(tempX.elementAt(j)) && coordListY2.elementAt(k).equals(tempY.elementAt(j)))) {
                            Log.e("tag","^^^TRUE");
                        } else {Log.e("tag","^^^False");}

                        Log.e("tag", "coordListX1.elementAt(" + k + ") != tempX2.elementAt(" + j + ") || coordListY1.elementAt(" + k + ") != tempY2.elementAt(" + j + ")");
                        Log.e("tag", coordListX1.elementAt(k) + " != " + tempX2.elementAt(j) + " || " + coordListY1.elementAt(k) + " != " + tempY2.elementAt(j) );
                        if ((coordListX1.elementAt(k) != tempX2.elementAt(j) || coordListY1.elementAt(k) != tempY2.elementAt(j))) {
                            Log.e("tag","^^^TRUE");
                        } else {Log.e("tag","^^^False");}*/







                        if (((coordListX1.get(k).equals(tempX.elementAt(j)) && coordListY1.get(k).equals(tempY.elementAt(j))) &&
                                ( (!(coordListX2.get(k).equals(tempX2.elementAt(j))) ) || (!(coordListY2.get(k).equals(tempY2.elementAt(j)))))) ||
                                ((coordListX2.get(k).equals(tempX.elementAt(j)) && coordListY2.get(k).equals(tempY.elementAt(j))) &&
                                        ((!(coordListX1.get(k).equals(tempX2.elementAt(j)))) || (!(coordListY1.get(k).equals(tempY2.elementAt(j))))))){
                            tempTally += 1;

                            Log.e("tag", "With new K we have found a new match");

                            //if connection, add new connection to tempX
                            //then search K again from top(0)
                            if (tempX.elementAt(j).equals(coordListX1.get(k)) && tempY.elementAt(j).equals(coordListY1.get(k))){
                                tempX.addElement(coordListX2.get(k));
                                tempY.addElement(coordListY2.get(k));
                                tempX2.addElement(coordListX1.get(k));
                                tempY2.addElement(coordListY1.get(k));
                                tempK.addElement(0f);
                                k = -1;
                                j++;
                            } else {
                                tempX.addElement(coordListX1.get(k));
                                tempY.addElement(coordListY1.get(k));
                                tempX2.addElement(coordListX2.get(k));
                                tempY2.addElement(coordListY2.get(k));
                                tempK.addElement(0f);
                                k = -1;
                                j++;
                            }

                            for (int a = 0; a < tempX.size(); a++) {
                                Log.e("tag", "tempX = " + tempX.elementAt(a) + " tempY = " + tempY.elementAt(a) + " tempX2 = " + tempX2.elementAt(a) +
                                        "    tempY2 = " + tempY2.elementAt(a) + " tempK = " + tempK.elementAt(a));
                            }
                                                           //size gets bigger for tempX........
                        }

                        Log.e("tag", "k = " + k);

                        // if no more add tally row, then delete last tempX
                        if (k >= (coordListX1.size() - 1)) {
                            Log.e("tag", "Finished coordList, removing from temp");
                            for (int a = 0; a < endpointX.size(); a++) {
                                if (tempX.elementAt(j).equals(endpointX.get(a)) &&
                                    tempY.elementAt(j).equals(endpointY.get(a))) {
                                    Log.e("tag", "Adding Tally");
                                    tallyX1.add(tempX.elementAt(j));
                                    tallyY1.add(tempY.elementAt(j));
                                    tallyX2.add(x2);
                                    tallyY2.add(y2);
                                    tallyTally.add((float) tempTally + 2);
                                    if (tallyTally.get(tallyTally.size() - 1) > highestTally) {
                                        Log.e("tag", "New High Tally");
                                        highestTally = tallyTally.get(tallyTally.size() - 1);
                                        tempChainPointsX = new ArrayList <Float>(1);
                                        tempChainPointsY = new ArrayList <Float>(1);
                                        tempChainPointsX.add(x2);
                                        tempChainPointsY.add(y2);
                                        tempChainPointsX.add(x1);
                                        tempChainPointsY.add(y1);
                                        for (int iChain = 0; iChain < tempX.size(); iChain++){
                                            tempChainPointsX.add(tempX.elementAt(iChain));
                                            tempChainPointsY.add(tempY.elementAt(iChain));
                                        }
                                        findSingleChainDirection(tempChainPointsX, tempChainPointsY);
                                    } else if (tallyTally.get(tallyTally.size() - 1).equals(highestTally)) {

                                        // try to find formual to find which chain to follow when there are multiple
                                        // tallies that are equal to each other.

                                        Toast.makeText(getContext(), "equal tally error",
                                                Toast.LENGTH_LONG).show();

                                        /*Log.e("tag", "Equal to High Tally");
                                        tempChainPointsX = new ArrayList<Float>(1);
                                        tempChainPointsY = new ArrayList<Float>(1);
                                        tempChainPointsX.add(x2);
                                        tempChainPointsY.add(y2);
                                        tempChainPointsX.add(x1);
                                        tempChainPointsY.add(y1);
                                        for (int iChain = 0; iChain < tempX.size(); iChain++){
                                            tempChainPointsX.add(tempX.elementAt(iChain));
                                            tempChainPointsY.add(tempY.elementAt(iChain));
                                        }
                                        processEqualTally(tempChainPointsX, tempChainPointsY);
                                        //compareChains(tempChainPointsX, tempChainPointsY); */
                                    } else if (chainPointsX.size() != 0){
                                        findSingleChainDirection(chainPointsX, chainPointsY);
                                    }
                                } else {
                                    Log.e("tag", "Tally disregarded");
                                }
                            }
                            tempX.removeElementAt(j);
                            tempY.removeElementAt(j);
                            tempX2.removeElementAt(j);
                            tempY2.removeElementAt(j);
                            tempK.removeElementAt(j);
                            tempTally -= 1;
                            k = -3;

                            for (int a = 0; a < tempX.size(); a++) {
                                Log.e("tag", "tempX = " + tempX.elementAt(a) + " tempY = " + tempY.elementAt(a) + " tempX2 = " + tempX2.elementAt(a) +
                                        "    tempY2 = " + tempY2.elementAt(a) + " tempK = " + tempK.elementAt(a));
                            }

                            Log.e("tag", "--------Tally------");
                            for (int a = 0; a < tallyX1.size(); a++) {
                                Log.e("tag", "T = " + tallyTally.get(a) + " x1 = " + tallyX1.get(a) + " y1 = " + tallyY1.get(a) +
                                        "    x2 = " + tallyX2.get(a) + " y2 = " + tallyY2.get(a));
                            }

                            j -= 1; // 2 because, one because remove element, second because j++
                            Log.e("tag", "j = " + j);
                            if (j == -1) {
                                j = tempX.size();
                                k = coordListX1.size();
                            }
                        }
                    }
                }
            }
        }


        if (groupList.size() > 0) {
            Toast.makeText(getContext(), "--" + preDisp + "," + test + "-methyl" + methType + disp,
                    Toast.LENGTH_LONG).show();
        } else if (group == 1) {
            Toast.makeText(getContext(), "--" + preDisp + disp,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "--" + preDisp +"-methyl" + disp,
                    Toast.LENGTH_LONG).show();
        }


    }

    public void findSingleChainDirection(ArrayList<Float> tranXChainPoints, ArrayList<Float> tranYChainPoints){

        Log.e("tag", "findChainDirection method started");
        firstGroup = 0;
        secondGroup = 0;
        group = 0;

        ///STATEMENTS NEED TO BE REFINED

        chainPointsX = new ArrayList<Float>(1);
        chainPointsY = new ArrayList<Float>(1);

        //used to search one direction to see when a group comes up
        findChainDirection(tranXChainPoints, tranYChainPoints);





        if (firstGroup <= secondGroup){
            for (int i = 0; i < tranXChainPoints.size(); i++){
                chainPointsX.add(tranXChainPoints.get(i));
                chainPointsY.add(tranYChainPoints.get(i));
            }

            group = firstGroup + 1;
        }  else if (firstGroup > secondGroup) {
            for (int i = (tranXChainPoints.size() - 1); i >= 0; i--){
                chainPointsX.add(tranXChainPoints.get(i));
                chainPointsY.add(tranYChainPoints.get(i));
            }
            group = secondGroup + 1;
        }

        Log.e("tag", "--------Following Chain---------------");
        for(int i = 0; i < chainPointsX.size(); i++) {
            Log.e("tag", "chainPointsX = " + chainPointsX.get(i) + "  chainPointsY = " + chainPointsY.get(i));
        }

        nameThatChain(group);


    }

    public void nameThatChain(int group) {
        disp = "";

        groupList = new ArrayList<Integer>(1);
        //group now equals index + 1... or normal nomenclature of IUPAC


        if (leftOrRight == 0) {
            for (int i = 1; i < chainPointsX.size(); i++) {
                for (int j = 0; j < coordListX1.size(); j++) {
                    if (chainPointsX.get(i).equals(coordListX1.get(j)) && chainPointsY.get(i).equals(coordListY1.get(j))) {
                        if ((!(chainPointsX.get(i - 1).equals(coordListX2.get(j)))) || (!(chainPointsY.get(i - 1).equals(coordListY2.get(j))))) {
                            if ((!(chainPointsX.get(i + 1).equals(coordListX2.get(j)))) || (!(chainPointsY.get(i + 1).equals(coordListY2.get(j))))) {
                                if (i != (group - 1)){
                                    groupList.add(i + 1);
                                    Log.e("tag", "BBANOTHER branch found at ..i =" + i);
                                }
                            }
                        }
                    } else if (chainPointsX.get(i).equals(coordListX2.get(j)) && chainPointsY.get(i).equals(coordListY2.get(j))) {
                        if ((!(chainPointsX.get(i - 1).equals(coordListX1.get(j)))) || (!(chainPointsY.get(i - 1).equals(coordListY1.get(j))))) {
                            if ((!(chainPointsX.get(i + 1).equals(coordListX1.get(j)))) || (!(chainPointsY.get(i + 1).equals(coordListY1.get(j))))) {
                                if (i != (group - 1)){
                                    groupList.add(i + 1);
                                    Log.e("tag", "BBANOTHER branch found at ..i =" + i);
                                }
                            }
                        }
                    }
                }
                if (i == (chainPointsX.size() - 1)) Log.e("tag", "no branches found");
            }
        }



        if (leftOrRight == 1) {
            for (int i = (chainPointsX.size() - 2); i > 0; i--) {
                for (int j = 0; j < coordListX1.size(); j++) {
                    if (chainPointsX.get(i).equals(coordListX1.get(j)) && chainPointsY.get(i).equals(coordListY1.get(j))) {
                        if ((!(chainPointsX.get(i - 1).equals(coordListX2.get(j)))) || (!(chainPointsY.get(i - 1).equals(coordListY2.get(j))))) {
                            if ((!(chainPointsX.get(i + 1).equals(coordListX2.get(j)))) || (!(chainPointsY.get(i + 1).equals(coordListY2.get(j))))) {

                                if (i != (chainPointsX.size() - group)){
                                        groupList.add(i + 1);
                                        Log.e("tag", "ANOTHER branch found at ..i =" + i);
                                    }

                            }
                        }
                    } else if (chainPointsX.get(i).equals(coordListX2.get(j)) && chainPointsY.get(i).equals(coordListY2.get(j))) {
                        if ((!(chainPointsX.get(i - 1).equals(coordListX1.get(j)))) || (!(chainPointsY.get(i - 1).equals(coordListY1.get(j))))) {
                            if ((!(chainPointsX.get(i + 1).equals(coordListX1.get(j)))) || (!(chainPointsY.get(i + 1).equals(coordListY1.get(j))))) {

                                if (i != (chainPointsX.size() - group)){
                                    groupList.add(i + 1);
                                    Log.e("tag", "ANOTHER branch found at ..i =" + i);
                                }

                            }
                        }
                    }
                }
                if (i == 1) Log.e("tag", "no branches found in Second group");
            }
        }




        test = groupList.toString();

        if (groupList.size() == 1){
            methType = "di";
        }if (groupList.size() == 2){
            methType = "tri";
        }if (groupList.size() == 3){
            methType = "tetra";
        }if (groupList.size() == 4){
            methType = "penta";
        }if (groupList.size() == 5){
            methType = "hexa";
        }if (groupList.size() == 6){
            methType = "hepta";
        }if (groupList.size() == 7){
            methType = "octa";
        }if (groupList.size() == 8){
            methType = "nona";
        }if (groupList.size() == 9){
            methType = "deca";
        }if (groupList.size() > 9){
            methType = "???";
        }


        if (group == 1) {
            preDisp = "";
        } else if (group == 2) {
            preDisp = "2";
        } else if (group == 3) {
            preDisp = "3";
        } else if (group == 4) {
            preDisp = "4";
        } else if (group == 5) {
            preDisp = "5";
        } else if (group == 6) {
            preDisp = "6";
        } else if (group == 7) {
            preDisp = "7";
        } else if (group == 8) {
            preDisp = "8";
        }

        if (chainPointsX.size() == 1){
            disp += "methane";
        } else if (chainPointsX.size() == 2){
            disp += "ethane";
        } else if (chainPointsX.size() == 3){
            disp += "propane";
        } else if (chainPointsX.size() == 4){
            disp += "butane";
        } else if (chainPointsX.size() == 5){
            disp += "pentane";
        } else if (chainPointsX.size() == 6){
            disp += "hexane";
        } else if (chainPointsX.size() == 7){
            disp += "heptane";
        } else if (chainPointsX.size() == 8){
            disp += "octane";
        } else if (chainPointsX.size() == 9){
            disp += "nonane";
        } else if (chainPointsX.size() == 10){
            disp += "decane";
        }else if (chainPointsX.size() > 10){
            disp += "...yeah, just believe it works";
        }


        if (group == 1) {
            if (chainPointsX.size() == 1){
                disp = "Methane";
            } else if (chainPointsX.size() == 2){
                disp = "Ethane";
            } else if (chainPointsX.size() == 3){
                disp = "Propane";
            } else if (chainPointsX.size() == 4){
                disp = "Butane";
            } else if (chainPointsX.size() == 5){
                disp = "Pentane";
            } else if (chainPointsX.size() == 6){
                disp = "Hexane";
            } else if (chainPointsX.size() == 7){
                disp = "Heptane";
            } else if (chainPointsX.size() == 8){
                disp = "Ocatane";
            } else if (chainPointsX.size() == 9){
                disp = "Nonane";
            } else if (chainPointsX.size() == 10){
                disp = "Decane";
            }else if (chainPointsX.size() > 10){
                disp = "...yeah, just believe it works";
            }
        }

        /*Toast.makeText(getContext(), disp,
                Toast.LENGTH_LONG).show();*/
    }

    public void findChainDirection(ArrayList<Float> tranXChainPoints, ArrayList<Float> tranYChainPoints) {

        leftOrRight = -1;

        for (int i = 1; i < tranXChainPoints.size(); i++){
            for (int j = 0; j < coordListX1.size(); j++) {
                if (tranXChainPoints.get(i).equals(coordListX1.get(j)) && tranYChainPoints.get(i).equals(coordListY1.get(j))) {
                    if(   (!(tranXChainPoints.get(i - 1).equals(coordListX2.get(j)))) ||  (!(tranYChainPoints.get(i - 1).equals(coordListY2.get(j)))) ){
                        if(   (!(tranXChainPoints.get(i + 1).equals(coordListX2.get(j)))) ||  (!(tranYChainPoints.get(i + 1).equals(coordListY2.get(j)))) ) {
                            firstGroup = i;
                            Log.e("tag", "FAbranch found at ..i =" + i);
                            j = (coordListX1.size() + 1);
                            i = (tranXChainPoints.size() + 1);
                        }
                    }
                } else if (tranXChainPoints.get(i).equals(coordListX2.get(j)) && tranYChainPoints.get(i).equals(coordListY2.get(j))) {
                    if(   (!(tranXChainPoints.get(i - 1).equals(coordListX1.get(j)))) ||  (!(tranYChainPoints.get(i - 1).equals(coordListY1.get(j)))) ){
                        if(   (!(tranXChainPoints.get(i + 1).equals(coordListX1.get(j)))) ||  (!(tranYChainPoints.get(i + 1).equals(coordListY1.get(j)))) ) {
                            firstGroup = i;
                            Log.e("tag", "FBbranch found at ..i =" + i);
                            j = (coordListX1.size() + 1);
                            i = (tranXChainPoints.size() + 1);
                        }
                    }
                }
            }
            if ( i == (tranXChainPoints.size() - 1)) Log.e("tag", "no branches found");
        }

        //used to search the other direction to find when a group appears
        for (int i = (tranXChainPoints.size() - 2); i > 0; i--){
            for (int j = 0; j < coordListX1.size(); j++) {
                if (tranXChainPoints.get(i).equals(coordListX1.get(j)) && tranYChainPoints.get(i).equals(coordListY1.get(j))) {
                    if(   (!(tranXChainPoints.get(i - 1).equals(coordListX2.get(j)))) ||  (!(tranYChainPoints.get(i - 1).equals(coordListY2.get(j)))) ){
                        if(   (!(tranXChainPoints.get(i + 1).equals(coordListX2.get(j)))) ||  (!(tranYChainPoints.get(i + 1).equals(coordListY2.get(j)))) ) {
                            secondGroup = (tranXChainPoints.size() - (i + 1));
                            Log.e("tag", "FCbranch found at ..secondGroup =" + secondGroup);
                            j = (coordListX1.size() + 1);
                            i = -1;
                        }
                    }
                } else if (tranXChainPoints.get(i).equals(coordListX2.get(j)) && tranYChainPoints.get(i).equals(coordListY2.get(j))) {
                    if(   (!(tranXChainPoints.get(i - 1).equals(coordListX1.get(j)))) ||  (!(tranYChainPoints.get(i - 1).equals(coordListY1.get(j)))) ){
                        if(   (!(tranXChainPoints.get(i + 1).equals(coordListX1.get(j)))) ||  (!(tranYChainPoints.get(i + 1).equals(coordListY1.get(j)))) ) {
                            secondGroup = (tranXChainPoints.size() - (i + 1));
                            Log.e("tag", "FDbranch found at ..secondGroup =" + secondGroup);
                            j = (coordListX1.size() + 1);
                            i = -1;
                        }
                    }
                }
            }
            if (i == 1) Log.e("tag", "no branches found in Second group");
        }

        if (firstGroup <= secondGroup) {
            leftOrRight = 0;
        }  else {
            leftOrRight = 1;
        }
    }

    public void processEqualTally(ArrayList<Float> tranXChainPoints, ArrayList<Float> tranYChainPoints){

        firstGroup = 0;
        secondGroup = 0;
        if (group > 0) group -= 1; //because you add one when trying to name the bond

        chainPointsX = new ArrayList<Float>(1);
        chainPointsY = new ArrayList<Float>(1);

        findChainDirection(tranXChainPoints, tranYChainPoints);


        /*if (firstGroup > secondGroup) firstGroup = secondGroup;

        Log.e("tag", "Before equal group = " + group);

        if ((group == 0) || firstGroup < group){
            chainPointsX = new ArrayList<Float>(1);
            chainPointsY = new ArrayList<Float>(1);
            for (int i = 0; i < tranXChainPoints.size(); i++){
                chainPointsX.add(tranXChainPoints.get(i));
                chainPointsY.add(tranYChainPoints.get(i));
            }
            group = firstGroup + 1;
        } else if (firstGroup >= group) group += 1;

        Log.e("tag", "equal group = " + group);*/


        if (firstGroup <= secondGroup){
            for (int i = 0; i < tranXChainPoints.size(); i++){
                chainPointsX.add(tranXChainPoints.get(i));
                chainPointsY.add(tranYChainPoints.get(i));
            }

            group = firstGroup + 1;
        }  else if (firstGroup > secondGroup) {
            for (int i = (tranXChainPoints.size() - 1); i >= 0; i--){
                chainPointsX.add(tranXChainPoints.get(i));
                chainPointsY.add(tranYChainPoints.get(i));
            }
            group = secondGroup + 1;
        }

        Log.e("tag", "--------Following Chain---------------");
        for(int i = 0; i < chainPointsX.size(); i++) {
            Log.e("tag", "chainPointsX = " + chainPointsX.get(i) + "  chainPointsY = " + chainPointsY.get(i));
        }


        nameThatChain(group);

    }

}
