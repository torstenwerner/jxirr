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

public class GoalSeekStatus {

    public static final int GOAL_SEEK_OK = 0;
    public static final int GOAL_SEEK_ERROR = 1;

    private final int seekStatus;
    private final Double returnData;

    public GoalSeekStatus(int seekStatus, Double returnData) {
        this.seekStatus = seekStatus;
        this.returnData = returnData;
    }

    public int getSeekStatus() {
        return seekStatus;
    }

    public Double getReturnData() {
        return returnData;
    }

    public String toString() {
        return "Status - " + seekStatus + ", Return Data - " + returnData;
    }

}