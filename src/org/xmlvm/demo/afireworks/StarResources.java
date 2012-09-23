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

package org.xmlvm.demo.afireworks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class StarResources {
    private final Bitmap[] starBitmaps = new Bitmap[4];

    public StarResources(Resources resources) {
        starBitmaps[0] = BitmapFactory.decodeResource(resources, R.drawable.star1);
        starBitmaps[1] = BitmapFactory.decodeResource(resources, R.drawable.star2);
        starBitmaps[2] = BitmapFactory.decodeResource(resources, R.drawable.star3);
        starBitmaps[3] = BitmapFactory.decodeResource(resources, R.drawable.star4);
    }

    public Bitmap getCachedStarBitmap(int num) {
        return starBitmaps[num % starBitmaps.length];
    }
}
