// Region.java

package jmri.jmrix.rps;

import javax.vecmath.*;
import java.awt.geom.GeneralPath;
import java.awt.Shape;

/**
 * Represent a region in space for the RPS system.
 *<P>
 * The region is specfied by a <em>right-handed</em>
 * set of points.
 * <p>
 * Regions are immutable once created.
 * <p>
 * This initial implementation of a Region is inherently 2-dimensional,
 * deferring use of the 3rd (Z) dimension to a later implementation.
 * It uses a Java2D GeneralPath to handle the inside/outside calculations.
 *
 * @author	Bob Jacobsen  Copyright (C) 2007, 2008
 * @version	$Revision: 1.3 $
 */
public class Region {
    
    public Region(Point3d[] points) {
        super();

        initPath(points);
        
        // old init
        if (points.length<3) log.error("Not enough points to define region");
        this.points = points;
    }
    
    GeneralPath path;
    
    /**
     * Provide Java2D access to the shape of this region.
     *<p>
     * This should provide a copy of the GeneralPath path, to keep the underlying
     * object immutable, but by returning a Shape type hopefully we 
     * achieve the same result with a little better performance. Please
     * don't assume you can cast and modify this.
     */
    public Shape getPath() {
        return path;
    }
    
    void initPath(Point3d[] points) {
        if (points.length < 3) 
            log.error("Region needs at least three points to have non-zero area");
        
        path = new GeneralPath();
        path.moveTo((float)points[0].x, (float)points[0].y);
        for (int i = 1; i<points.length; i++) {
            path.lineTo((float)points[i].x, (float)points[i].y);
        }
        path.lineTo((float)points[0].x, (float)points[0].y);
    }
    
    /**
     * Ctor from a string like "(0,0,0);(1,0,0);(1,1,0);(0,1,0)"
     */
    public Region(String s) {
        String[] pStrings = s.split(";");
        points = new Point3d[pStrings.length];
        
        // load each point
        for (int i=0; i<points.length; i++) {
            // remove leading ( and trailing )
            String coords = pStrings[i].substring(1,pStrings[i].length()-1);
            String[] coord = coords.split(",");
            if (coord.length!=3) log.error("need to have three coordinates in "+pStrings[i]);
            double x = Double.valueOf(coord[0]).doubleValue();
            double y = Double.valueOf(coord[1]).doubleValue();
            double z = Double.valueOf(coord[2]).doubleValue();
            points[i] = new Point3d(x,y,z);
        }
        initPath(points);
    }

    public String toString() {
        String retval = "";
        for (int i=0; i<points.length; i++) {
            retval += "("+points[i].x+","+points[i].y+","+points[i].z+")";
            if (i!=points.length-1) retval+=";";
        }
        return retval;
    }
    
    public boolean isInside(Point3d p) {
        return path.contains(p.x, p.y);
    }
    
    public boolean equals(Object ro) {
        try {
            Region r = (Region)ro;
            if (points.length != r.points.length) return false;
            for (int i = 0; i<points.length; i++)
                if (!points[i].epsilonEquals(r.points[i], 0.001)) {
                    return false;
                }
            return true;
        } catch (Exception e) { return false; }
    }
    
    Point3d[] points;
    
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Region.class.getName());
}

/* @(#)Region.java */
