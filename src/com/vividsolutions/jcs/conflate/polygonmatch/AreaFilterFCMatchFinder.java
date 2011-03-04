

/*
 * The Java Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jcs.conflate.polygonmatch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;

/**
 * Speeds up processing by ignoring target and candidate features with areas
 * greater than a specified maximum or less than a specified minimum.
 */
public class AreaFilterFCMatchFinder implements FCMatchFinder {

  private FCMatchFinder matchFinder;
  private double minArea;
  private double maxArea;

  public AreaFilterFCMatchFinder(double minArea, double maxArea, FCMatchFinder matchFinder) {
    Assert.isTrue(minArea < maxArea);
    this.minArea = minArea;
    this.maxArea = maxArea;
    this.matchFinder = matchFinder;
  }

  public Map match(IndexedFeatureCollection targetFC, IndexedFeatureCollection candidateFC,
                   TaskMonitor monitor) {
    monitor.allowCancellationRequests();
    Map filteredTargetToMatchesMap = matchFinder.match(
        filter(targetFC, "targets", monitor),
        filter(candidateFC, "candidates", monitor), monitor);
    Map targetToMatchesMap = blankTargetToMatchesMap(targetFC.getFeatures(), candidateFC.getFeatureSchema());
    targetToMatchesMap.putAll(filteredTargetToMatchesMap);
    return targetToMatchesMap;
  }

  private IndexedFeatureCollection filter(IndexedFeatureCollection fc,
      String name, TaskMonitor monitor) {
    monitor.report("Filtering " + name + " by area");
    int featuresProcessed = 0;
    int totalFeatures = fc.size();
    FeatureDataset filteredFC = new FeatureDataset(fc.getFeatureSchema());
    for (Iterator i = fc.iterator(); i.hasNext() && ! monitor.isCancelRequested(); ) {
      Feature feature = (Feature) i.next();
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");
      if (! satisfiesAreaCriterion(feature)) { continue; }
      filteredFC.add(feature);
    }
    return new IndexedFeatureCollection(filteredFC);
  }

  private boolean satisfiesAreaCriterion(Feature feature) {
    double area = feature.getGeometry().getArea();
    return minArea <= area && area <= maxArea;
  }

  private Map blankTargetToMatchesMap(List targets, FeatureSchema matchesSchema) {
    Map blankTargetToMatchesMap = new HashMap();
    for (Iterator i = targets.iterator(); i.hasNext(); ) {
      Feature target = (Feature) i.next();
      blankTargetToMatchesMap.put(target, new Matches(matchesSchema));
    }
    return blankTargetToMatchesMap;
  }

}
