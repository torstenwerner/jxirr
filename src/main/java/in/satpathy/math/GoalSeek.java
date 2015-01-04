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

    private final GoalSeekData data;

    public GoalSeek(GoalSeekData data) {
        this.data = data;
    }

    /*
         *  Calculate a reasonable approximation to the derivative of a function
         *  in a single point.
         */
    private GoalSeekResult fake_df(Function<Double, GoalSeekResult> f, double x, double xstep) {
        double xl;
        double xr;
        double yl;
        double yr;
        double dfx;
        GoalSeekResult result;

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

        result = f.apply(xl);
        if (result.getStatus() != OK) {
            if (DEBUG_GOAL_SEEK) {
                log("==> failure at xl\n");
            }
            return result;
        }
        yl = result.getValue();
        if (DEBUG_GOAL_SEEK) {
            log("==> xl = " + xl + " ; yl =" + yl);
        }

        result = f.apply(xr);
        if (result.getStatus() != OK) {
            if (DEBUG_GOAL_SEEK) {
                log("==> failure at xr");
            }
            return result;
        }
        yr = result.getValue();
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
    public GoalSeekResult newton(Function<Double, GoalSeekResult> f, Function<Double, GoalSeekResult> df,
                                 double x0) {
        int iterations;
        double precision = data.precision / 2;

        if (data.have_root) {
            return new GoalSeekResult(OK, data.root);
        }

        if (DEBUG_GOAL_SEEK) {
            log("newton");
        }

        for (iterations = 0; iterations < 20; iterations++) {
            double x1;
            double y0;
            double df0;
            double stepsize;
            GoalSeekResult result;
            if (DEBUG_GOAL_SEEK) {
                log("newton - x0 = " + x0 + ", (i = " + iterations + " )");
            }
            //  Check whether we have left the valid interval.
            if (x0 < data.xmin || x0 > data.xmax) {
                return new GoalSeekResult(ERROR, null);
            }
            result = f.apply(x0);
            if (result.getStatus() != OK) {
                return result;
            }

            y0 = result.getValue();
            if (DEBUG_GOAL_SEEK) {
                log("   y0 = " + y0);
            }
            if (data.update(x0, y0)) {
                return new GoalSeekResult(OK, data.root);
            }

            if (df != null) {
                result = df.apply(x0);
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
                result = fake_df(f, x0, xstep);
            }
            if (result.getStatus() != OK) {
                return result;
            }

            df0 = result.getValue();
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