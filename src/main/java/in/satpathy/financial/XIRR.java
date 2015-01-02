/*
 *  XIRR.java
 *  Copyright (C) 2005 Gautam Satpathy
 *  gautam@satpathy.in
 *  www.satpathy.in
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package in.satpathy.financial;

import in.satpathy.math.GoalSeek;
import in.satpathy.math.GoalSeekData;
import in.satpathy.math.GoalSeekStatus;

public class XIRR {
    /*
     *  Excel stores dates as sequential serial numbers so they can be used
	 *  in calculations. By default, January 1, 1900 is serial number 1, and
	 *  January 1, 2008 is serial number 39448 because it is 39,448 days
	 *  after January 1, 1900.
	 */

    public static double xirr(XIRRData xirrData) {
        GoalSeekData data;
        GoalSeekStatus status;
        double result;
        double rate0;

        data = new GoalSeekData();
        GoalSeek.goal_seek_initialize(data);
        data.xmin = -1;
        data.xmax = Math.min(1000, data.xmax);
        rate0 = xirrData.guess;

        status = GoalSeek.goalSeekNewton(
                new XIRRNPV(), null, data, xirrData, rate0);

        if (status.seekStatus == GoalSeekStatus.GOAL_SEEK_OK) {
            result = (Double) status.returnData;
        } else {
            result = Double.NaN;
        }

        System.out.println("XIRR Result - " + result);
        return !(Double.isNaN(result)) ? (result - 1.0) : result;
    }
}
