/*
 *  TestXIRR.java
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
package in.satpathy.financial

import spock.lang.Specification

class XIRRTest extends Specification {
    def setupSpec() {
        Double.metaClass.isCloseTo = {
            double target, double epsilon -> (delegate - target).abs() < epsilon * (delegate.abs() + Math.abs(target))
        }
        Double.metaClass.isCloseTo = {
            double target -> delegate.isCloseTo(target, Math.sqrt(Double.MIN_VALUE))
        }
    }

    def "a simple test"() {
        setup:
        double[] values = new double[5];
        double[] dates = new double[5];
        values[0] = -6000;
        values[1] = 2134;
        values[2] = 1422;
        values[3] = 1933;
        values[4] = 1422;
        dates[0] = XIRRData.getExcelDateValue(new GregorianCalendar(1999, 0, 15));
        dates[1] = XIRRData.getExcelDateValue(new GregorianCalendar(1999, 3, 4));
        dates[2] = XIRRData.getExcelDateValue(new GregorianCalendar(1999, 4, 9));
        dates[3] = XIRRData.getExcelDateValue(new GregorianCalendar(2000, 2, 12));
        dates[4] = XIRRData.getExcelDateValue(new GregorianCalendar(2000, 4, 1));
        XIRRData data = new XIRRData(5, 0.3, values, dates);

        when:
        double xirrValue = XIRR.xirr(data);

        then:
        xirrValue.isCloseTo(0.22483769162452205)
    }
}