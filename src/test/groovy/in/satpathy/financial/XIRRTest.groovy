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

import static java.lang.Math.abs
import static java.util.Calendar.*

class XIRRTest extends Specification {
    static {
        Double.metaClass.isCloseTo = {
            double target, double epsilon = Math.sqrt(Double.MIN_VALUE) ->
                if (epsilon <= 0) {
                    throw new RuntimeException("epsilon must be positive")
                }
                double value = (double) delegate
                def maxDifference = epsilon * (abs(value) + abs(target)) * 0.5
                if (maxDifference == 0) {
                    maxDifference = epsilon
                }
                return abs(value - target) < maxDifference
        }
    }

    def "a simple test"() {
        setup:
        double[] values = [-6000, 2134, 1422, 1933, 1422]
        GregorianCalendar[] dates = [
                new GregorianCalendar(1999, JANUARY, 15),
                new GregorianCalendar(1999, APRIL, 4),
                new GregorianCalendar(1999, MAY, 9),
                new GregorianCalendar(2000, MARCH, 12),
                new GregorianCalendar(2000, MAY, 1)
        ]
        XIRRData data = new XIRRData(values, dates)

        when:
        double xirrValue = XIRR.xirr(data)

        then:
        xirrValue.isCloseTo(0.2248376916245216)
    }

    def "datevalue"() {
        expect:
        XIRRData.getExcelDateValue(new GregorianCalendar(2015, 0, 2)) == 42006
    }

    def "very small epsilon"() {
        expect:
        Double.MIN_VALUE * 0.5 == 0.0
    }
}