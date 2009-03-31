/* *********************************************************************** *
 * project: org.matsim.*
 * Variance.java
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

/**
 *
 */
package playground.yu.analysis;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.yu.utils.CollectionSum;

/**
 * @author yu
 * 
 */
public class ScoreVariance extends AbstractPersonAlgorithm implements
		PlanAlgorithm {
	private final List<Double> scores = new ArrayList<Double>();
	private BufferedWriter writer;
	private final String outputFilename;

	public ScoreVariance(final String outputFilename) {
		this.outputFilename = outputFilename;
		try {
			this.writer = IOUtils.getBufferedWriter(outputFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run(final Person person) {
		run(person.getSelectedPlan());
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		// final String netFilename = "../data/ivtch/input/network.xml";
		final String netFilename = "../data/schweiz/input/ch.xml";
		final String plansFilename = "../data/schweiz/input/Run410.output.plans.xml.gz";
		final String outputFilename = "../data/schweiz/variance/Run410variance.txt";

		Gbl.startMeasurement();
		Gbl.createConfig(null);

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);

		Population population = new PopulationImpl();

		ScoreVariance sv = new ScoreVariance(outputFilename);

		System.out.println("-->reading plansfile: " + plansFilename);
		new MatsimPopulationReader(population, network).readFile(plansFilename);

		sv.run(population);
		sv.writeVariance();

		System.out.println("--> Done!");
		Gbl.printElapsedTime();
		System.exit(0);
	}

	public void run(final Plan plan) {
		this.scores.add(plan.getScore());
	}

	public void writeVariance() {
		int size = this.scores.size();
		if (size == 0) {
			try {
				this.writer
						.write(this.outputFilename
								+ "\nERROR: there is not data for calculating Variance!");
				this.writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(
					"ERROR: there is not data for calculating Variance!");
		}
		double[] scoreArray = new double[size];
		for (int i = 0; i < size; i++)
			scoreArray[i] = this.scores.get(i);
		double var = getVariance(scoreArray);
		try {
			this.writer.write(this.outputFilename + "\ncount = "
					+ scoreArray.length + "\navg. = " + getAverage(scoreArray)
					+ "\nScoreVariance = " + var + "\nStandard deviation = "
					+ getStandardDeviation(var));
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double getStandardDeviation(final double variance) {
		return Math.sqrt(variance);
	}

	public static double getStandardDeviation(final double[] inputData) {
		return Math.sqrt(getVariance(inputData));
	}

	public static double getVariance(final double[] inputData) {
		double average = getAverage(inputData);
		if (average == -1) {
			System.err.println("avg. = " + -1);
			System.exit(0);
		}
		return getSquareSum(inputData) / getCount(inputData) - average
				* average;
	}

	public static int getCount(final double[] inputData) {
		return inputData == null ? -1 : inputData.length;
	}

	public static double getAverage(final double[] inputData) {
		return inputData == null || inputData.length == 0 ? -1 : CollectionSum
				.getSum(inputData)
				/ inputData.length;
	}

	public static double getSquareSum(final double[] inputData) {
		if (inputData == null || inputData.length == 0)
			return -1;
		double sqrsum = 0.0;
		for (double element : inputData)
			sqrsum += element * element;
		return sqrsum;
	}
}
