package com.bhawak.osmnavigation.navigation;

public class DirectionForLine {
        // structure for point in cartesian plane.
        static class DirectionPoint
        {
            double x, y;
        };

        // constant integers for directions
        static int RIGHT = 1, LEFT = -1, ZERO = 0;

        static int directionOfPoint(DirectionPoint A,
                                    DirectionPoint B, DirectionPoint P)
        {
            // subtracting co-ordinates of point A
            // from B and P, to make A as origin
            B.x -= A.x;
            B.y -= A.y;
            P.x -= A.x;
            P.y -= A.y;

            // Determining cross Product
            double cross_product = B.x * P.y - B.y * P.x;

            // return RIGHT if cross product is positive
            if (cross_product > 0)
                return RIGHT;

            // return LEFT if cross product is negative
            if (cross_product < 0)
                return LEFT;

            // return ZERO if cross product is zero.
            return ZERO;
        }
}
