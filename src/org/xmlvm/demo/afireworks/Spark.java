/* Copyright (c) 2002-2011 by XMLVM.org
 *
 * Project Info:  http://www.xmlvm.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

package org.xmlvm.demo.afireworks;

import org.xmlvm.demo.afireworks.AndroidFireworks.Environment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * A sparks of the big fireworks. A {@link Bomb} contains many Sparks.
 */
public class Spark {
    private final Bitmap image;
    private float        x;
    private float        y;
    private float        vx;
    private float        vy;
    private boolean      outOfSight;
    private Environment  environment;
    private Paint        paint = new Paint();
    private long         lastRenderTime;
    private long         renderTimeDiff;
    private float        stretchFactor;
    private int          counter;


    public Spark(Resources resources, Environment environment) {
        this.environment = environment;
        this.image = BitmapFactory.decodeResource(resources, getStarId());
    }

    void reset(int x, int y) {
        this.x = x;
        this.y = y;
        vx = (float) (Math.random() * Const.MAX2V) - (Const.MAX2V / 2);
        vy = (float) (Math.random() * Const.MAX2V) - (Const.MAX2V / 2);
        outOfSight = false;
        lastRenderTime = 0;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isOutOfSight() {
        return outOfSight;
    }

    public void setOutOfSight(boolean outOfSight) {
        this.outOfSight = outOfSight;
    }

    public void nextStep() {
        if (outOfSight) {
            return;
        }
        if (x < -2 * Const.IMAGE_SIZE || x > environment.windowWidth || y < -2 * Const.IMAGE_SIZE
                || y > environment.windowHeight) {
            // This spark is out of reach
            outOfSight = true;
            return;
        }

        if (lastRenderTime != 0) {
            renderTimeDiff = System.currentTimeMillis() - lastRenderTime;
            stretchFactor = renderTimeDiff / (float) Const.UPDATE_DELAY;
        } else {
            stretchFactor = 1;
        }
        lastRenderTime = System.currentTimeMillis();

//        if (counter++ == 50) {
//            counter = 0;
//            Log.d("DEBUG", "Stretch Factor: " + stretchFactor);
//        }

        // Gravity
        vx += (Const.DV * (-environment.rotX / 10f) * stretchFactor);
        vy += (Const.DV * (environment.rotY / 10f) * stretchFactor);
        x += (Const.T * vx * stretchFactor);
        y += (Const.T * vy * stretchFactor);
    }

    public void render(Canvas canvas) {
        canvas.drawBitmap(image, x, y, paint);
    }

    private static int getStarId() {
        int starId = R.drawable.star4;
        double rand = (Math.random() * 4);
        if (rand < 1) {
            starId = R.drawable.star1;
        } else if (rand < 2) {
            starId = R.drawable.star2;
        } else if (rand < 3) {
            starId = R.drawable.star3;
        }
        return starId;
    }
}
