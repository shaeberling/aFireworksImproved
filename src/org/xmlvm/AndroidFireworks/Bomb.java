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

/**
 * A bomb is a logical group of sparks. A bomb defines where a group of stars
 * start to animate upon an "explosion".
 */
public class Bomb {
    private Spark   sparks[]      = new Spark[Const.SPARKS_PER_BOMB];
    private boolean allOutOfSight = false;


    public Bomb(StarResources resources, Environment environment) {
        for (int i = 0; i < sparks.length; i++)
            sparks[i] = new Spark(resources, environment);
    }

    /**
     * Returns the requested Spark.
     */
    public Spark getSpark(int n) {
        return sparks[n];
    }

    /**
     * Render call sparks of this bomb on the given canvas.
     */
    public void render(Canvas canvas) {
        for (Spark spark : sparks) {
            spark.render(canvas);
        }
    }

    /**
     * Resets all client {@link Spark}s contained in this Bomb to the given
     * location.
     */
    public void scheduleForReset(int x, int y, int pointerId) {
        allOutOfSight = false;
        for (Spark spark : sparks) {
            spark.scheduleForReset(x, y, pointerId);
        }
    }

    /**
     * Returns whether all sparks are out of sight.
     */
    public boolean isAllOutOfSight() {
        // No need to recalculate if the bomb hasn't been reset.
        if (allOutOfSight) {
            return true;
        }

        int count = 0;
        for (Spark spark : sparks) {
            if (spark.isOutOfSight()) {
                count++;
            }
        }
        return count == Const.SPARKS_PER_BOMB;
    }
}
