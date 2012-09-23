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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

/**
 * The main class/activity of the Fireworks application.
 */
public class AndroidFireworks extends Activity {
    /**
     * UI Text for inviting people to visit XMLVM.org.
     */
    public static final String VISIT_XMLVM       = "Visit Project XMLVM.org";
    /**
     * UI Text for inviting people to watch the XMLVM Google TechTalk.
     */
    public static final String WATCH_YOUTUBE     = "Watch Google TechTalk";
    /**
     * The URL to XMLVM.org
     */
    public static final String XMLVM_URL         = "http://www.xmlvm.org";
    /**
     * The URL to the XMLVM YouTube video.
     */
    public static final String YOUTUBE_XMLVM_URL = "http://www.youtube.com/watch?v=s8nMpi5-P-I";

    private SurfaceView        surfaceView;
    private SurfaceHolder      surfaceHolder;
    private Fireworks          fireworks;
    private Thread             renderThread;
    private final Environment  environment       = new Environment();


    @Override
    public void onContentChanged() {
        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        environment.windowWidth = d.getWidth();
        environment.windowHeight = d.getHeight();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(VISIT_XMLVM).setIcon(R.drawable.xmlvm);
        menu.add(WATCH_YOUTUBE).setIcon(R.drawable.youtube);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(VISIT_XMLVM)) {
            viewUri(Uri.parse(XMLVM_URL));
            return true;
        } else if (item.getTitle().equals(WATCH_YOUTUBE)) {
            viewUri(Uri.parse(YOUTUBE_XMLVM_URL));
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();

        // No title bar.
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Switch to fullscreen view, getting rid of the status bar as well.
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Activate hardware acceleration if possible.
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        // Register the accelerometer listener.
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing.
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                environment.rotX = event.values[0];
                environment.rotY = event.values[1];

            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        setContentView(surfaceView);

        // Register the touch listener.
        surfaceView.setOnTouchListener(new OnTouchListener() {
            private final int touchMod   = 3;
            private int       touchCount = 0;


            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchCount = 0;
                }
                if (touchCount == 0) {
                    for (int i = 0; i < event.getPointerCount(); ++i) {
                        fireworks.touchExplode((int) event.getX(i), (int) event.getY(i));
                    }
                }
                touchCount = (touchCount + 1) % touchMod;
                return true;
            }
        });
        fireworks = new Fireworks(getResources(), environment);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onPause() {
        super.onPause();
        renderThread.interrupt();
        renderThread = null;
        fireworks.onPause();
    }

    /**
     * Returns the active Environment.
     */
    public Environment getEnvironment() {
        return environment;
    }

    private void viewUri(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }


    /**
     * A simple class for keepting basic environmental data.
     */
    public static class Environment {
        public float rotX         = 0;
        public float rotY         = 0;
        public int   windowHeight = 10;
        public int   windowWidth  = 10;
    }
}
