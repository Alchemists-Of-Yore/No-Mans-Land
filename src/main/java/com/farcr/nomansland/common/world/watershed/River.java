package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.utility.MathUtilities;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class River {
    public static final River NONE = new River(null, new RiverBoundingBox(0, 0,0,0,0,0,0));
    private final RiverSegment[] segments;
    private final RiverBoundingBox bvhRoot;

    private River(RiverSegment[] segments, RiverBoundingBox bvhRoot) {
        this.segments = segments;
        this.bvhRoot = bvhRoot;
    }

    public static River generate(RandomSource random, int watershedX, int watershedZ, int sourceX, int sourceZ, int sourceHeight, int drainX, int drainZ, int drainHeight, int iterations) {
        // start out with a list of river points, generate river shape
        List<RiverPoint> points = subdivideRecursive(random, watershedX, watershedZ, new RiverPoint(sourceX, sourceZ, 0), new RiverPoint(drainX, drainZ, 1), iterations);

        RiverSegment[] riverSegments = new RiverSegment[points.size() - 1];
        for (int i = 0; i < points.size() - 1; i++) {
            RiverPoint pointA = points.get(i + 0);
            RiverPoint pointB = points.get(i + 1);
            riverSegments[i] = new RiverSegment(
                    pointA.x, pointA.z, Mth.lerp(pointA.gradient, sourceHeight, drainHeight),
                    pointB.x, pointB.z, Mth.lerp(pointB.gradient, sourceHeight, drainHeight)
            );
        }

        // generate river bvh
        RiverBoundingBox bvhRoot = RiverBoundingBox.fromSegments(riverSegments, 0, riverSegments.length);
        bvhRoot.expand(30, 50, 30); // expand by a little bit so that rivers can have a radius
        return new River(riverSegments, bvhRoot);
    }

    private static List<RiverPoint> subdivideRecursive(RandomSource random, int watershedX, int watershedZ, RiverPoint start, RiverPoint end, int iterations) {
        if (iterations == 0) {
            List<RiverPoint> segment = new ArrayList<>(2);
            segment.add(start);
            segment.add(end);
            return segment;
        }

        double distance = Math.abs(Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.z - end.z)*(start.z - end.z)));
        double maxOffset = distance < 16 ? 0 : distance * 0.2;
        int midpointPosX = (int) (Mth.lerp(0.5, start.x, end.x) + random.nextInt(0, (int) maxOffset + 1)),
            midpointPosZ = (int) (Mth.lerp(0.5, start.z, end.z) + random.nextInt(0, (int) maxOffset + 1));
        // clamp it to stay within the current watershed.
        int margin = 30;
        midpointPosX = Math.clamp(midpointPosX - (watershedX * Watershed.WATERSHED_SIZE), margin, Watershed.WATERSHED_SIZE - margin) + watershedX * Watershed.WATERSHED_SIZE;
        midpointPosZ = Math.clamp(midpointPosZ - (watershedZ * Watershed.WATERSHED_SIZE), margin, Watershed.WATERSHED_SIZE - margin) + watershedZ * Watershed.WATERSHED_SIZE;

        RiverPoint mid = new RiverPoint(
                midpointPosX, midpointPosZ,
                Mth.lerp(0.5, start.gradient, end.gradient)
        );

        List<RiverPoint> left = subdivideRecursive(random, watershedX, watershedZ, start, mid, iterations - 1);
        List<RiverPoint> right = subdivideRecursive(random, watershedX, watershedZ, mid, end, iterations - 1);

        left.remove(left.size() - 1); // remove mid from left half
        left.addAll(right);
        return left;
    }

    public RiverSpaceCoordinates getRiverSpaceCoordinates(int x, int y, int z) {
        MutableRiverSpaceCoords best = new MutableRiverSpaceCoords(1000, 0);
        sampleDistanceFromRiverRecursive(this.bvhRoot, x, y, z, best);
        return best.toImmutable();
    }

    private void sampleDistanceFromRiverRecursive(RiverBoundingBox box, int x, int y, int z, MutableRiverSpaceCoords best) {
        if (!box.containsHorizontal(x, z)) return; // don't take y into consideration
        if (box.riverSegmentIndex >= 0) {
            RiverSegment segment = this.segments[box.riverSegmentIndex];

            int horizontalDistance = (int) lineSegmentDistance(x, z, segment.startX, segment.startZ, segment.endX, segment.endZ);

            if (horizontalDistance < best.horizontalDistance) {
                best.horizontalDistance = horizontalDistance;

                double gradient = (x - segment.endX()) * (segment.startX() - segment.endX()) +
                        (z - segment.endZ()) * (segment.startZ() - segment.endZ());
                gradient /= (segment.startX() - segment.endX()) * (segment.startX() - segment.endX()) +
                        (segment.startZ() - segment.endZ()) * (segment.startZ() - segment.endZ());
                double surfaceHeight = Mth.clampedMap(gradient, 0, 1, segment.endHeight(), segment.startHeight());
                best.surfaceHeight = surfaceHeight;//Math.floor(surfaceHeight / 12.0) * 12;
            }
        } else {
            sampleDistanceFromRiverRecursive(box.childA, x, y, z, best);
            sampleDistanceFromRiverRecursive(box.childB, x, y, z, best);
        }
    }

    public static int getWaterSurfaceHeight(double riverHeight, double terraceGradientSize) {
        return (int) MathUtilities.terrace(riverHeight, 12, terraceGradientSize);
    }

    private static double lineSegmentDistance(double px, double py, double ax, double ay, double bx, double by) {
        double bax = bx - ax, bay = by - ay;
        double h = ((px - ax) * bax + (py - ay) * bay) / (bax * bax + bay * bay);
        if (h < 0) h = 0;
        else if (h > 1) h = 1;
        double dx = (px - ax) - bax * h;
        double dy = (py - ay) - bay * h;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public record RiverSpaceCoordinates(int horizontalDistance, double riverHeight) {}
    private record RiverSegment(int startX, int startZ, double startHeight, int endX, int endZ, double endHeight) {}
    private record RiverPoint(int x, int z, double gradient) {
//        private static RiverPoint subdivide(RandomSource random, RiverPoint start, RiverPoint end) {
//            double maxOffset = Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.z - end.z)*(start.z - end.z));
//            double offsetAngle = random.nextDouble() * Math.PI * 2;
//            double offsetDistance = Math.sqrt(random.nextDouble()) * maxOffset * 0.5;
//            return new RiverPoint(
//                    (int) (Mth.lerp(0.5, start.x, end.x)),
//                    (int) (Mth.lerp(0.5, start.z, end.z)),
//                    Mth.lerp(0.5, start.gradient, end.gradient)
//            );
//        }
    }
    private static class MutableRiverSpaceCoords {
        int horizontalDistance;
        double surfaceHeight;
        MutableRiverSpaceCoords(int horiz, double surfaceHeight) { this.horizontalDistance = horiz; this.surfaceHeight = surfaceHeight; }
        private RiverSpaceCoordinates toImmutable() { return new RiverSpaceCoordinates(horizontalDistance, (int) surfaceHeight); }
    }
    private static class RiverBoundingBox {
        int riverSegmentIndex; // -1 for non-leaf nodes
        int x1, y1, z1, x2, y2, z2;
        RiverBoundingBox childA, childB;

        RiverBoundingBox(int riverSegmentIndex, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.riverSegmentIndex = riverSegmentIndex;
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.x2 = x2; this.y2 = y2; this.z2 = z2;
        }

        RiverBoundingBox expand(int x, int y, int z) {
            this.x1 -= x; this.y1 -= y; this.z1 -= z;
            this.x2 += x; this.y2 += y; this.z2 += z;
            if (this.childA != null) this.childA.expand(x, y, z);
            if (this.childB != null) this.childB.expand(x, y, z);
            return this;
        }

        boolean contains(int x, int y, int z) {
            return x >= x1 && x <= x2 &&
                    y >= y1 && y <= y2 &&
                    z >= z1 && z <= z2;
        }

        boolean containsHorizontal(int x, int z) {
            return x >= x1 && x <= x2 &&
                    z >= z1 && z <= z2;
        }

        static RiverBoundingBox fromSegments(RiverSegment[] segments, int start, int end) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

            for (int i = start; i < end; i++) {
                RiverSegment seg = segments[i];
                minX = Math.min(minX, Math.min(seg.startX, seg.endX));
                minY = Math.min(minY, Math.min(Mth.floor(seg.startHeight), Mth.floor(seg.endHeight)));
                minZ = Math.min(minZ, Math.min(seg.startZ, seg.endZ));
                maxX = Math.max(maxX, Math.max(seg.startX, seg.endX));
                maxY = Math.max(maxY, Math.max(Mth.ceil(seg.startHeight), Mth.ceil(seg.endHeight)));
                maxZ = Math.max(maxZ, Math.max(seg.startZ, seg.endZ));
            }

            RiverBoundingBox box = new RiverBoundingBox(-1, minX, minY, minZ, maxX, maxY, maxZ);

            if (end - start == 1) {
                // Leaf node
                box.riverSegmentIndex = start;
            } else {
                int mid = (start + end) / 2;
                box.childA = fromSegments(segments, start, mid);
                box.childB = fromSegments(segments, mid, end);
            }

            return box;
        }
    }
}
