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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * The fireworks controller class.
 */
public class Fireworks {
    private Bomb[]      bombs;
    private int         touchCount    = 0;
    private Environment environment;
    private boolean     userActive    = false;
    private Paint       blackPaint    = new Paint(android.R.color.black);
    private Thread      updateThread;

    /** For randomizing pointer colors after reset. */
    private int         pointerOffset = 0;


    public Fireworks(StarResources resources, Environment environment) {
        this.environment = environment;
        bombs = new Bomb[Const.BOMB_COUNT];
        // Initialize normal bombs.
        for (int i = 0; i < bombs.length; ++i) {
            bombs[i] = new Bomb(resources, environment);
            bombs[i].scheduleForReset((int) (Math.random() * (environment.windowWidth - 60)) + 30,
                    (int) (Math.random() * (environment.windowHeight - 60)) + 30, -1);
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

                    for (int i = 0; i < bombs.length; ++i) {
                        update(bombs[i], !userActive);
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

    /**
     * Performs the update for the next frame.
     */
    public void render(Canvas canvas) {
        canvas.drawPaint(blackPaint);

        if (allBombsOutOfSight()) {
            userActive = false;
            pointerOffset = (int) (Math.random() * 10);
            Log.d("DEBUG", "New Pointer Offset: " + pointerOffset);
        }
        for (int i = 0; i < bombs.length; ++i) {
            render(canvas, bombs[i]);
        }
    }

    public void update(Bomb bomb, boolean resetOnFinish) {
        boolean bombOutOfSight = bomb.allOutOfSight();
        if (bombOutOfSight && resetOnFinish) {
            bomb.scheduleForReset((int) (Math.random() * environment.windowWidth),
                    (int) (Math.random() * environment.windowHeight), -1);
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

    public void render(Canvas canvas, Bomb bomb) {
        for (int i = 0; i < Const.SPARKS_PER_BOMB; ++i) {
            Spark spark = bomb.getSpark(i);
            if (!spark.isOutOfSight()) {
                spark.render(canvas);
            }
        }
    }

    /**
     * Will make a touch-bomb explode at the given position.
     */
    public void touchExplode(int x, int y, int pointerId) {
        userActive = true;
        bombs[touchCount].scheduleForReset(x, y, pointerId + pointerOffset);
        touchCount = (touchCount + 1) % bombs.length;
    }

    /**
     * Returns whether all bombs are out of sight.
     */
    private boolean allBombsOutOfSight() {
        for (int i = 0; i < bombs.length; ++i) {
            if (!bombs[i].allOutOfSight()) {
                return false;
            }
        }
        return true;
    }
}