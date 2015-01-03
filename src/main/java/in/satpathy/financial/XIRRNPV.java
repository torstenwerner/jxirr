/*
 *  XIRRNPV.java
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

import in.satpathy.math.GoalSeekFunction;
import in.satpathy.math.GoalSeekStatus;

public class XIRRNPV implements GoalSeekFunction {

    public static final double DAYS_OF_YEAR = 365.0;

    public XIRRNPV() {
    }

    public GoalSeekStatus f(double rate, Object userData) {
        XIRR p;
        double[] values;
        double[] dates;
        double sum;

        p = (XIRR) userData;
        values = p.values;
        dates = p.dates;
        sum = 0;
        for (int i = 0; i < dates.length; i++) {
            double d = dates[i] - dates[0];
            if (d < 0) {
                return new GoalSeekStatus(GoalSeekStatus.GOAL_SEEK_ERROR, null);
            }
            sum += values[i] / Math.pow(rate, d / DAYS_OF_YEAR); //pow1p( rate, d / 365.0 ) ;
        }

        return new GoalSeekStatus(GoalSeekStatus.GOAL_SEEK_OK, sum);
    }

}