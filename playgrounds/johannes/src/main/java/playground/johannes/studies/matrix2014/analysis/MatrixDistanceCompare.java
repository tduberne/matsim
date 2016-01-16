/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.johannes.studies.matrix2014.analysis;

import com.vividsolutions.jts.geom.Point;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.stats.StatsWriter;
import playground.johannes.synpop.analysis.AnalyzerTask;
import playground.johannes.synpop.analysis.FileIOContext;
import playground.johannes.synpop.analysis.StatsContainer;
import playground.johannes.synpop.gis.Zone;
import playground.johannes.synpop.gis.ZoneCollection;
import playground.johannes.synpop.matrix.NumericMatrix;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixDistanceCompare implements AnalyzerTask<NumericMatrix> {

    private final NumericMatrix distanceMatrix;

    private final ZoneCollection zones;

    private DistanceCalculator distanceCalculator;

    private Discretizer discretizer;

    private final String dimension;

    private FileIOContext ioContext;

    private NumericMatrix refMatrix;

    public MatrixDistanceCompare(String dimension, ZoneCollection zones) {
        this.dimension = dimension;
        this.distanceMatrix = new NumericMatrix();
        this.zones = zones;

        discretizer = new LinearDiscretizer(50000);
        distanceCalculator = CartesianDistanceCalculator.getInstance();
    }

    public void setFileIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    public void setReferenceMatrix(NumericMatrix refMatrix) {
        this.refMatrix = refMatrix;
    }

    @Override
    public void analyze(NumericMatrix simMatrix, List<StatsContainer> containers) {
        TDoubleDoubleHashMap simHist = histogram(simMatrix);
        TDoubleDoubleHashMap refHist = histogram(refMatrix);

        Set<Double> distances = new HashSet();
        for(double d : simHist.keys()) distances.add(d);
        for(double d : refHist.keys()) distances.add(d);

        TDoubleDoubleHashMap diffHist = new TDoubleDoubleHashMap();
        for(Double d : distances) {
            double simVal = simHist.get(d);
            double refVal = refHist.get(d);

            if(refVal == 0 && simVal == 0) {
                diffHist.put(d, 0);
            } else if(refVal > 0) {
                diffHist.put(d, (simVal - refVal)/ refVal);
            }
        }

        containers.add(new StatsContainer(dimension, diffHist.values()));

        if(ioContext != null) {
            try {
                Histogram.normalize(simHist);
                Histogram.normalize(refHist);
                Histogram.normalize(diffHist);

                StatsWriter.writeHistogram(simHist, "distance", "count", String.format("%s/%s.sim.txt", ioContext.getPath
                        (), dimension));
                StatsWriter.writeHistogram(refHist, "distance", "count", String.format("%s/%s.ref.txt", ioContext
                        .getPath(), dimension));
                StatsWriter.writeHistogram(diffHist, "distance", "count", String.format("%s/%s.diff.txt", ioContext
                        .getPath(), dimension));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private TDoubleDoubleHashMap histogram(NumericMatrix m) {
        TDoubleArrayList values = new TDoubleArrayList();
        TDoubleArrayList weights = new TDoubleArrayList();

        double sum = 0;
        Set<String> keys = m.keys();
        for(String i : keys) {
            for(String j : keys) {
                Double vol = m.get(i, j);
                if(vol != null && vol > 0) {
                    double d = getDistance(i, j);
                    values.add(d);
                    weights.add(vol);
                    sum += vol;
                }
            }
        }
        System.out.println("Total=" + sum);
        return Histogram.createHistogram(values.toArray(), weights.toArray(), discretizer, true);
    }

    private double getDistance(String i, String j) {
        Double d = distanceMatrix.get(i, j);

        if(d == null) {
            Zone z_i = zones.get(i);
            Zone z_j = zones.get(j);

            if(z_i != null && z_j != null) {
                Point p_i = z_i.getGeometry().getCentroid();
                Point p_j = z_j.getGeometry().getCentroid();

                d = distanceCalculator.distance(p_i, p_j);
                distanceMatrix.set(i, j, d);
            } else {
                d = 0.0;
                if(z_i == null) System.err.println(String.format("Zone %s not found.", i));
                if(z_j == null) System.err.println(String.format("Zone %s not found.", j));
            }
        }

        return d;
    }
}
