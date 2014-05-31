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

package org.xmlvm.AndroidFireworks;

import org.xmlvm.AndroidFireworks.AndroidFireworks.Environment;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * The fireworks controller class.
 */
public class FireworksRenderer {
    private final Bomb[]        bombs         = new Bomb[Const.BOMB_COUNT];
    private int                 touchCount    = 0;
    private Environment         environment   = new Environment();
    private boolean             userActive    = false;
    private Paint               blackPaint    = new Paint(android.R.color.black);
    private Thread              updateThread;

    public float                rotX          = 0;
    public float                rotY          = 0;
    public int                  windowHeight  = 10;
    public int                  windowWidth   = 10;

    /** For randomizing pointer colors after reset. */
    private int                 pointerOffset = 0;

    private final SurfaceHolder surfaceHolder;


    public FireworksRenderer(StarResources resources, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;

        // Initialize normal bombs.
        for (int i = 0; i < bombs.length; ++i) {
            bombs[i] = new Bomb(resources, environment);
            bombs[i].scheduleForReset((int) (Math.random() * environment.windowWidth),
                    (int) (Math.random() * environment.windowHeight), -1);
        }
    }

    public void onResume() {
        updateThread = new Thread() {
            long start;
            long duration;
            long pause;


            public void run() {
                while (!isInterrupted()) {
                    start = System.currentTimeMillis();

                    for (Bomb bomb : bombs) {
                        update(bomb, !userActive);
                    }

                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        render(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }

                    duration = System.currentTimeMillis() - start;
                    pause = Const.UPDATE_DELAY - duration;
                    pause = Math.max(pause, 0);
                    try {
                        sleep(pause);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };

        updateThread.start();
    }

    public void onPause() {
        updateThread.interrupt();
    }

    public void onSizeChanged(int width, int height) {
        environment.windowWidth = width;
        environment.windowHeight = height;
    }

    public void onSensorData(float rotX, float rotY) {
        environment.rotX = rotX;
        environment.rotY = rotY;
    }

    public void onTouchEvent(int x, int y, int pointerId) {
        this.touchExplode(x, y, pointerId);
    }

    /**
     * Performs the update for the next frame.
     */
    public void render(Canvas canvas) {
        canvas.drawPaint(blackPaint);

        if (isAllBombsOutOfSight()) {
            userActive = false;
            pointerOffset = (int) (Math.random() * 10);
            Log.d("DEBUG", "New Pointer Offset: " + pointerOffset);
        }
        for (Bomb bomb : bombs) {
            bomb.render(canvas);
        }
    }

    public void update(Bomb bomb, boolean resetOnFinish) {
        boolean bombOutOfSight = bomb.isAllOutOfSight();
        if (bombOutOfSight && resetOnFinish) {
            bomb.scheduleForReset((int) (Math.random() * environment.windowWidth)
                    + Const.IMAGE_SIZE, (int) (Math.random() * environment.windowHeight)
                    + Const.IMAGE_SIZE, -1);
            bombOutOfSight = false;
        }
        if (!bombOutOfSight) {
            for (int i = 0; i < Const.SPARKS_PER_BOMB; ++i) {
                // Update the position
                Spark spark = bomb.getSpark(i);
                spark.nextStep();
            }
        }
    }

    /**
     * Will make a touch-bomb explode at the given position.
     */
    private void touchExplode(int x, int y, int pointerId) {
        userActive = true;
        bombs[touchCount].scheduleForReset(x, y, pointerId + pointerOffset);
        touchCount = (touchCount + 1) % bombs.length;
    }

    /**
     * Returns whether all bombs are out of sight.
     */
    private boolean isAllBombsOutOfSight() {
        for (Bomb bomb : bombs) {
            if (!bomb.isAllOutOfSight()) {
                return false;
            }
        }
        return true;
    }
}