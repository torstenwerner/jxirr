/*
 *  GoalSeek.java
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

import java.util.function.Function;

import static in.satpathy.math.GoalSeekResult.Status.ERROR;
import static in.satpathy.math.GoalSeekResult.Status.OK;

/**
 * A generic root finder.
 */
public class GoalSeek {
    private static final boolean DEBUG_GOAL_SEEK = false;

    public static boolean update_data(double x, double y, GoalSeekData data) {
        if (y > 0) {
            if (data.havexpos) {
                if (data.havexneg) {
                    /*
                     *  When we have pos and neg, prefer the new point only
					 *  if it makes the pos-neg x-internal smaller.
					 */
                    if (Math.abs(x - data.xneg) < Math.abs(data.xpos - data.xneg)) {
                        data.xpos = x;
                        data.ypos = y;
                    }
                } else if (y < data.ypos) {
                    /* We have pos only and our neg y is closer to zero.  */
                    data.xpos = x;
                    data.ypos = y;
                }
            } else {
                data.xpos = x;
                data.ypos = y;
                data.havexpos = true;
            }
            return false;
        } else if (y < 0) {
            if (data.havexneg) {
                if (data.havexpos) {
                    /*
                     * When we have pos and neg, prefer the new point only
					 * if it makes the pos-neg x-internal smaller.
					 */
                    if (Math.abs(x - data.xpos) < Math.abs(data.xpos - data.xneg)) {
                        data.xneg = x;
                        data.yneg = y;
                    }
                } else if (-y < -data.yneg) {
                    /* We have neg only and our neg y is closer to zero.  */
                    data.xneg = x;
                    data.yneg = y;
                }

            } else {
                data.xneg = x;
                data.yneg = y;
                data.havexneg = true;
            }
            return false;
        } else {
            /* Lucky guess...  */
            data.have_root = true;
            data.root = x;
            return true;
        }
    }


    /*
     *  Calculate a reasonable approximation to the derivative of a function
     *  in a single point.
     */
    public static GoalSeekResult fake_df(Function<Double, GoalSeekResult> f, double x, double xstep, GoalSeekData data) {
        double xl;
        double xr;
        double yl;
        double yr;
        double dfx;
        GoalSeekResult status;

        if (DEBUG_GOAL_SEEK) {
            log("fake_df (x = " + x + ", xstep = " + xstep + ")");
        }

        xl = x - xstep;
        if (xl < data.xmin)
            xl = x;

        xr = x + xstep;
        if (xr > data.xmax)
            xr = x;

        if (xl == xr) {
            if (DEBUG_GOAL_SEEK) {
                log("==> xl == xr");
            }
            return new GoalSeekResult(ERROR, null);
        }

        status = f.apply(xl);
        if (status.getStatus() != OK) {
            if (DEBUG_GOAL_SEEK) {
                log("==> failure at xl\n");
            }
            return status;
        }
        yl = status.getValue();
        if (DEBUG_GOAL_SEEK) {
            log("==> xl = " + xl + " ; yl =" + yl);
        }

        status = f.apply(xr);
        if (status.getStatus() != OK) {
            if (DEBUG_GOAL_SEEK) {
                log("==> failure at xr");
            }
            return status;
        }
        yr = status.getValue();
        if (DEBUG_GOAL_SEEK) {
            log("==> xr = " + xr + " ; yr = " + yr);
        }

        dfx = (yr - yl) / (xr - xl);
        if (DEBUG_GOAL_SEEK) {
            log("==> " + dfx);
        }

        return Double.isInfinite(dfx) ?
                new GoalSeekResult(ERROR, null) :
                new GoalSeekResult(OK, dfx);
    }

    /**
     * Seek a goal (root) using Newton's iterative method.
     * <p>
     * The supplied function must (should) be continuously differentiable in the supplied interval.  If NULL is used for
     * `df', this function will estimate the derivative.
     * <p>
     * This method will find a root rapidly provided the initial guess, x0, is sufficiently close to the root.  (The
     * number of significant digits (asymptotically) goes like i^2 unless the root is a multiple root in which case it
     * is only like c*i.)
     */
    public static GoalSeekResult goalSeekNewton(Function<Double, GoalSeekResult> f, Function<Double, GoalSeekResult> df,
                                                GoalSeekData data, double x0) {
        int iterations;
        double precision = data.precision / 2;

        if (data.have_root) {
            return new GoalSeekResult(OK, data.root);
        }

        if (DEBUG_GOAL_SEEK) {
            log("goalSeekNewton");
        }

        for (iterations = 0; iterations < 20; iterations++) {
            double x1;
            double y0;
            double df0;
            double stepsize;
            GoalSeekResult status;
            if (DEBUG_GOAL_SEEK) {
                log("goalSeekNewton - x0 = " + x0 + ", (i = " + iterations + " )");
            }
            //  Check whether we have left the valid interval.
            if (x0 < data.xmin || x0 > data.xmax) {
                return new GoalSeekResult(ERROR, null);
            }
            status = f.apply(x0);
            if (status.getStatus() != OK) {
                return status;
            }

            y0 = status.getValue();
            if (DEBUG_GOAL_SEEK) {
                log("   y0 = " + y0);
            }
            if (update_data(x0, y0, data)) {
                return new GoalSeekResult(OK, data.root);
            }

            if (df != null) {
                status = df.apply(x0);
            } else {
                double xstep;
                if (Math.abs(x0) < 1e-10) {
                    if (data.havexneg && data.havexpos)
                        xstep = Math.abs(data.xpos - data.xneg) / 1e6;
                    else
                        xstep = (data.xmax - data.xmin) / 1e6;
                } else {
                    xstep = Math.abs(x0) / 1e6;
                }
                status = fake_df(f, x0, xstep, data);
            }
            if (status.getStatus() != OK) {
                return status;
            }

            df0 = status.getValue();
            //  If we hit a flat spot, we are in trouble.
            if (df0 == 0) {
                return new GoalSeekResult(ERROR, null);
            }

			/*
			 * Overshoot slightly to prevent us from staying on
			 * just one side of the root.
			 */
            x1 = x0 - 1.000001 * y0 / df0;
            stepsize = Math.abs(x1 - x0) / (Math.abs(x0) + Math.abs(x1));
            if (DEBUG_GOAL_SEEK) {
                log("   df0 = " + df0);
                log("   ss = " + stepsize);
            }

            x0 = x1;

            if (stepsize < precision) {
                data.root = x0;
                data.have_root = true;
                return new GoalSeekResult(OK, data.root);
            }
        }

        return new GoalSeekResult(ERROR, null);
    }

    /**
     * Log a message to the console.
     */
    private static void log(String message) {
        System.out.println(message);
    }

}