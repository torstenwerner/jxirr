/*
 *  XIRRData.java
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Data structure to hold XIRR data.
 */
public class XIRR {
    public static final GregorianCalendar EXCEL_DAY_ZERO = new GregorianCalendar(1899, 11, 30);
    public static final double DEFAULT_GUESS = 0.1;

    public double guess;
    public double[] values;
    public double[] dates;

    public XIRR(double guess, double[] values, double[] dates) {
        Objects.requireNonNull(values);
        Objects.requireNonNull(dates);
        if (values.length != dates.length) {
            throw new RuntimeException("Both arrays must be of same size.");
        }
        if (values.length < 2) {
            throw new RuntimeException("The arrays must contain at least 2 values.");
        }
        this.guess = guess;
        this.values = values;
        this.dates = dates;
    }

    public XIRR(double guess, double[] values, GregorianCalendar calendarDates[]) {
        this(guess, values, getExcelDateArray(calendarDates));
    }

    public XIRR(double[] values, GregorianCalendar calendarDates[]) {
        this(DEFAULT_GUESS, values, calendarDates);
    }

    public static double[] getExcelDateArray(GregorianCalendar calendarDates[]) {
        Objects.requireNonNull(calendarDates);
        final double[] dates = new double[calendarDates.length];
        for (int i = 0; i < calendarDates.length; i++) {
            dates[i] = getExcelDateValue(calendarDates[i]);
        }
        return dates;
    }

    /**
     * Returns the same value as Excel's DataValue method.
     */
    public static int getExcelDateValue(Calendar date) {
        return getDaysBetween(EXCEL_DAY_ZERO, date);
    }

    /**
     * Calculates the number of days between two calendar days in a manner which is independent of the Calendar type
     * used.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return The number of days between the two dates.  Zero is returned if the dates are the same, one if the dates
     * are adjacent, etc.  The order of the dates does not matter, the value returned is always >= 0. If Calendar types
     * of d1 and d2 are different, the result may not be accurate.
     */
    public static int getDaysBetween(Calendar d1, Calendar d2) {
        if (d1.after(d2)) {
            // swap dates so that d1 is start and d2 is end
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }

        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        d1 = (Calendar) d1.clone();
        while (d1.get(Calendar.YEAR) != y2) {
            days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
            d1.add(Calendar.YEAR, 1);
        }
        return days;
    }

    /*
     *  Excel stores dates as sequential serial numbers so they can be used
	 *  in calculations. By default, January 1, 1900 is serial number 1, and
	 *  January 1, 2008 is serial number 39448 because it is 39,448 days
	 *  after January 1, 1900.
	 */

    public double xirr() {
        GoalSeekData data;
        GoalSeekStatus status;
        double result;
        double rate0;

        data = new GoalSeekData();
        GoalSeek.goal_seek_initialize(data);
        data.xmin = -1;
        data.xmax = Math.min(1000, data.xmax);
        rate0 = guess;

        status = GoalSeek.goalSeekNewton(new XIRRNPV(), null, data, this, rate0);

        if (status.seekStatus == GoalSeekStatus.GOAL_SEEK_OK) {
            result = (Double) status.returnData;
        } else {
            result = Double.NaN;
        }

        System.out.println("XIRR Result - " + result);
        return !(Double.isNaN(result)) ? (result - 1.0) : result;
    }

    /**
     * Expensive method. Don't call in loops etc.
     */
    public String toString() {
        String text;
        String valuesStr;
        String datesStr;

        text = "XIRRData - n = " + values.length + ", Guess = " + this.guess;
        valuesStr = ", Values = ";
        datesStr = ", Dates = ";
        for (int i = 0; i < this.values.length; i++) {
            valuesStr = valuesStr + this.values[i];
            if (i < this.values.length - 1) {
                valuesStr = valuesStr + ",";
            }
        }
        for (int i = 0; i < this.dates.length; i++) {
            datesStr = datesStr + this.dates[i];
            if (i < this.dates.length - 1) {
                datesStr = datesStr + ",";
            }
        }
        return text + valuesStr + datesStr;
    }

}