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
import in.satpathy.math.GoalSeekResult;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import static in.satpathy.math.GoalSeekResult.Status.ERROR;
import static in.satpathy.math.GoalSeekResult.Status.OK;

/**
 * Data structure to hold XIRR data.
 */
public class Xirr {
    public static final GregorianCalendar EXCEL_DAY_ZERO = new GregorianCalendar(1899, 11, 30);
    public static final double DAYS_OF_YEAR = 365.0;
    public static final double DEFAULT_GUESS = 0.1;

    private final double guess;
    private final double[] values;
    private final int[] dates;

    public Xirr(double guess, double[] values, int[] dates) {
        Objects.requireNonNull(values);
        Objects.requireNonNull(dates);
        if (values.length != dates.length) {
            throw new RuntimeException("Both arrays must be of same size.");
        }
        this.guess = guess;
        this.values = values;
        this.dates = dates;
    }

    public Xirr(double guess, double[] values, Calendar calendarDates[]) {
        this(guess, values, getExcelDateArray(calendarDates));
    }

    public Xirr(double[] values, Calendar calendarDates[]) {
        this(DEFAULT_GUESS, values, calendarDates);
    }

    /**
     * converts an array of Calendar values into excel values
     */
    private static int[] getExcelDateArray(Calendar calendarDates[]) {
        Objects.requireNonNull(calendarDates);
        return Arrays.stream(calendarDates).mapToInt(Xirr::getExcelDateValue).toArray();
    }

    /**
     * Returns the same value as Excel's DataValue method.
     */
    private static int getExcelDateValue(Calendar date) {
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
    private static int getDaysBetween(Calendar d1, Calendar d2) {
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

    public double findRoot() {
        if (values.length < 2) {
            throw new RuntimeException("The arrays must contain at least 2 values.");
        }

        final GoalSeekData data = new GoalSeekData();
        data.xmin = -1;
        data.xmax = Math.min(1000, data.xmax);
        final GoalSeek goalSeek = new GoalSeek(data);

        final GoalSeekResult result = goalSeek.newton(this::residual, null, guess);
        if (result.getStatus() == OK) {
            return result.getValue() - 1.0;
        } else {
            return Double.NaN;
        }
    }

    private GoalSeekResult residual(double rate) {
        double sum = 0;
        for (int i = 0; i < dates.length; i++) {
            double d = dates[i] - dates[0];
            if (d < 0) {
                return new GoalSeekResult(ERROR, null);
            }
            sum += values[i] / Math.pow(rate, d / DAYS_OF_YEAR); //pow1p( rate, d / 365.0 ) ;
        }

        return new GoalSeekResult(OK, sum);
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