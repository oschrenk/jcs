package com.vividsolutions.jcs.conflate.coverage.snap;

import com.vividsolutions.jts.geom.*;
import java.util.*;

/**
 * Note: this class is not thread-safe.
 */
public class CoordinateSnapper {

  public Coordinate closestPt(List closePts, Coordinate pt)
  {
    Coordinate closestPt = null;
    double minDistance = 0.0;

    for (Iterator i = closePts.iterator(); i.hasNext(); ) {
      Coordinate closePt = (Coordinate) i.next();
      double distance = closePt.distance(pt);
      if (closestPt == null || distance < minDistance) {
        closestPt = closePt;
        minDistance = distance;
      }
    }
    return closestPt;
  }

  private SlowPointIndex ptIndex;
  private Envelope queryEnv = new Envelope();

  public CoordinateSnapper(SlowPointIndex ptIndex)
  {
    this.ptIndex = ptIndex;
  }

  /**
   * Compute the closest point within the given distance, if any.
   * @param pt
   * @param distance
   * @return the reference point to snap to, if any
   *   the original point if there were no reference points within the given distance
   */
  public Coordinate snap(Coordinate pt, double distance)
  {
    queryEnv.init(pt.x - distance, pt.x + distance, pt.y - distance, pt.y + distance);
    List closePts = ptIndex.query(queryEnv);
    Coordinate closestPt = closestPt(closePts, pt);
    if (closestPt == null) return pt;
    return closestPt;
  }


}
