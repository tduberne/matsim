/* *********************************************************************** *
 * project: org.matsim.*
 * BiasErrorGraph.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.counts.algorithms.graphs;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.matsim.counts.CountSimComparison;

public class BiasErrorGraph extends CountsGraph {

	public BiasErrorGraph(final List<CountSimComparison> ccl, final int iteration, final String filename,
			final String chartTitle) {
		super(ccl, iteration, filename, chartTitle);
	}

	@Override
	public JFreeChart createChart(final int nbr) {
		DefaultCategoryDataset dataset0 = new DefaultCategoryDataset();
		DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();

		// init with same value. Shorter stat.?
		// Possibly not all links have values for all hours.
		double[] meanRelError = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		int[] nbrRelErr = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] meanAbsError = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		int[] nbrAbsErr = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] meanAbsBias = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		int[] nbrAbsBias = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		Iterator<CountSimComparison> l_it = this.ccl_.iterator();
		while (l_it.hasNext()) {
			CountSimComparison cc = l_it.next();
			int hour = cc.getHour() - 1;
			meanRelError[hour] += Math.abs(cc.calculateRelativeError());
			nbrRelErr[hour]++;

			meanAbsError[hour] += Math.abs(cc.getSimulationValue() - cc.getCountValue());
			nbrAbsErr[hour]++;

			meanAbsBias[hour] += cc.getSimulationValue() - cc.getCountValue();
			nbrAbsBias[hour]++;
		}// while

		for (int h = 0; h < 24; h++) {
			if (nbrRelErr[h] > 0) {
				meanRelError[h] /= nbrRelErr[h];
			} else {
				meanRelError[h] = 1000.0;
			}
			if (nbrAbsErr[h] > 0) {
				meanAbsError[h] /= nbrAbsErr[h];
			} else {
				meanAbsError[h] = 1.0;
			}
			if (nbrAbsBias[h] > 0) {
				meanAbsBias[h] /= nbrAbsBias[h];
			} else {
				meanAbsBias[h] = 1.0;
			}

			dataset0.addValue(meanRelError[h], "Mean rel error", Integer.toString(h + 1));
			dataset1.addValue(meanAbsError[h], "Mean abs error", Integer.toString(h + 1));
			dataset1.addValue(meanAbsBias[h], "Mean abs bias", Integer.toString(h + 1));
		}

		this.chart_ = ChartFactory.createLineChart("", "Hour", "Mean rel error [%]", dataset0, PlotOrientation.VERTICAL,
				true, // legend?
				true, // tooltips?
				false // URLs?
				);
		CategoryPlot plot = this.chart_.getCategoryPlot();
		plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDataset(1, dataset1);
		plot.mapDatasetToRangeAxis(1, 1);

		final LineAndShapeRenderer renderer = new LineAndShapeRenderer();
		renderer.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
		plot.setRenderer(0, renderer);

		final CategoryAxis axis1 = new CategoryAxis("Hour");
		axis1.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 7));
		plot.setDomainAxis(axis1);

		final ValueAxis axis2 = new NumberAxis("Mean abs {bias, error} [veh/h]");
		plot.setRangeAxis(1, axis2);

		final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
		renderer2.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
		renderer2.setSeriesToolTipGenerator(1, new StandardCategoryToolTipGenerator());
		renderer2.setSeriesPaint(0, Color.black);
		plot.setRenderer(1, renderer2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

		return this.chart_;
	}
}
