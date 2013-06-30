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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class FireworksRenderer {
    private final Environment   environment = new Environment();
    private final Fireworks     fireworks;
    private final SurfaceHolder surfaceHolder;
    private Thread              renderThread;


    public FireworksRenderer(Resources resources, SurfaceHolder surfaceHolder) {
        this.fireworks = new Fireworks(new StarResources(resources), environment);
        this.surfaceHolder = surfaceHolder;
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
        fireworks.touchExplode(x, y, pointerId);
    }

    public void onResume() {
        renderThread = new Thread() {
            long start;
            long duration;
            long pause;


            public void run() {
                while (!isInterrupted()) {
                    start = System.currentTimeMillis();
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        fireworks.render(canvas);
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
        renderThread.start();
        fireworks.onResume();
    }

    public void onPause() {
        renderThread.interrupt();
        renderThread = null;
        fireworks.onPause();
    }
}
