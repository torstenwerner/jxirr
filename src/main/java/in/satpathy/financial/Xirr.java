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

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static in.satpathy.math.GoalSeekResult.Status.OK;

/**
 * Data structure to hold XIRR data.
 */
public class Xirr {
    public static final Calendar EXCEL_DAY_ZERO = new GregorianCalendar(1899, Calendar.DECEMBER, 30);
    public static final LocalDate EXCEL_DAY_ZERO_ = LocalDate.of(1899, Month.DECEMBER, 30);
    public static final double DAYS_OF_YEAR = 365.0;
    public static final double DEFAULT_GUESS = 0.1;

    private final double initialRate;
    private final double[] values;
    private final int[] dates;

    public Xirr(double guess, double[] values, int[] dates) {
        this.values = Objects.requireNonNull(values);
        this.dates = Objects.requireNonNull(dates);
        if (values.length != dates.length) {
            throw new RuntimeException("Both arrays must be of same size.");
        }
        this.initialRate = guess + 1.0;
    }

    public Xirr(double guess, double[] values, Calendar calendarDates[]) {
        this(guess, values, getExcelDateArray(calendarDates));
    }

    public Xirr(double[] values, Calendar calendarDates[]) {
        this(DEFAULT_GUESS, values, calendarDates);
    }

    public Xirr(double guess, double[] values, LocalDate calendarDates[]) {
        this(guess, values, getExcelDateArray(calendarDates));
    }

    public Xirr(double[] values, LocalDate calendarDates[]) {
        this(DEFAULT_GUESS, values, calendarDates);
    }

    /**
     * converts an array of Calendar values into int values similar to Excel's DateValue method
     */
    private static int[] getExcelDateArray(Calendar calendarDates[]) {
        Objects.requireNonNull(calendarDates);
        final ToIntFunction<Calendar> excelDateValue = date -> getDaysBetween(EXCEL_DAY_ZERO, date);
        return Arrays.stream(calendarDates).mapToInt(excelDateValue).toArray();
    }

    /**
     * converts an array of Calendar values into int values similar to Excel's DateValue method
     */
    private static int[] getExcelDateArray(LocalDate calendarDates[]) {
        Objects.requireNonNull(calendarDates);
        final ToIntFunction<LocalDate> excelDateValue = date -> getDaysBetween(EXCEL_DAY_ZERO_, date);
        return Arrays.stream(calendarDates).mapToInt(excelDateValue).toArray();
    }

    /**
     * Calculates the number of days between two calendar days in a manner which is independent of the Calendar type
     * used.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return The number of days between the two dates.  Zero is returned if the dates are the same, one if the dates
     * are adjacent, etc.  The order of the dates does matter. If Calendar types of d1 and d2 are different, the result
     * may not be accurate.
     */
    private static int getDaysBetween(Calendar d1, Calendar d2) {
        if (d1.after(d2)) {
            return -getDaysBetween(d2, d1);
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

    private static int getDaysBetween(LocalDate first, LocalDate second) {
        if (first.isAfter(second)) {
            return -getDaysBetween(second, first);
        }
        int daysDifference = second.getDayOfYear() - first.getDayOfYear();
        final int yearDifference = second.getYear() - first.getYear();
        final int daysOfYears = Stream.iterate(first, date -> date.plusYears(1)).limit(yearDifference)
                .mapToInt(LocalDate::lengthOfYear).sum();
        return daysDifference + daysOfYears;
    }

    /*
     *  Excel stores dates as sequential serial numbers so they can be used
	 *  in calculations. By default, January 1, 1900 is serial number 1, and
	 *  January 1, 2008 is serial number 39448 because it is 39,448 days
	 *  after January 1, 1900.
	 */

    public double findRoot() {
        checkInput();
        final GoalSeekData data = new GoalSeekData();
        data.xmin = -1;
        data.xmax = Math.min(1000, data.xmax);
        final GoalSeek goalSeek = new GoalSeek(data);

        final GoalSeekResult result = goalSeek.newton(this::residual, null, initialRate);
        if (result.getStatus() == OK) {
            return result.getValue() - 1.0;
        } else {
            return Double.NaN;
        }
    }

    private void checkInput() {
        if (values.length < 2) {
            throw new RuntimeException("The arrays must contain at least 2 values.");
        }
        for (int date : dates) {
            if (date < dates[0]) {
                // TODO: why?
                throw new RuntimeException("The dates array must be sorted properly.");
            }
        }
    }

    private GoalSeekResult residual(double rate) {
        final double sum = IntStream.range(0, dates.length).mapToDouble(residualAtIndex(rate)).sum();
        return new GoalSeekResult(OK, sum);
    }

    private IntToDoubleFunction residualAtIndex(double rate) {
        return index -> values[index] / Math.pow(rate, (dates[index] - dates[0]) / DAYS_OF_YEAR);
    }

    /**
     * Expensive method. Don't call in loops etc.
     */
    public String toString() {
        return "XIRRData - n = "
                + values.length
                + ", Guess = "
                + initialRate
                + ", Values = "
                + Arrays.stream(values).mapToObj(Double::toString).collect(Collectors.joining(","))
                + ", Dates = "
                + Arrays.stream(dates).mapToObj(Integer::toString).collect(Collectors.joining(","));
    }

}