/*
 *  GoalSeekStatus.java
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
package in.satpathy.math;

import static java.lang.String.format;

public class GoalSeekResult {

    public static enum Status {
        OK, ERROR
    }

    private final Status status;
    private final Double value;

    public GoalSeekResult(Status status, Double value) {
        this.status = status;
        this.value = value;
    }

    public Status getStatus() {
        return status;
    }

    public Double getValue() {
        return value;
    }

    public String toString() {
        return format("status = %s, value = %g", status, value);
    }
}