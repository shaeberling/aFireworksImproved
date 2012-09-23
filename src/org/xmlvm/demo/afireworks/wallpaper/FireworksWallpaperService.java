/* Copyright (c) 2002-2012 by XMLVM.org
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

package org.xmlvm.demo.afireworks.wallpaper;

import org.xmlvm.demo.afireworks.FireworksRenderer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class FireworksWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }


    private class WallpaperEngine extends Engine {
        private final int         touchMod   = 3;
        private int               touchCount = 0;
        private FireworksRenderer renderer;
        private SensorManager     sensorManager;


        @Override
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            renderer = new FireworksRenderer(getResources(), surfaceHolder);
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            // Register the accelerometer listener.
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Do nothing.
                }

                @Override
                public void onSensorChanged(SensorEvent event) {
                    renderer.onSensorData(event.values[0], event.values[1]);

                }
            }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                renderer.onResume();
            } else {
                renderer.onPause();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            renderer.onSizeChanged(width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchCount = 0;
            }
            if (touchCount == 0) {
                for (int i = 0; i < event.getPointerCount(); ++i) {
                    renderer.onTouchEvent((int) event.getX(i), (int) event.getY(i), i);
                }
            }
            touchCount = (touchCount + 1) % touchMod;

        }

    }

}
