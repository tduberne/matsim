/* *********************************************************************** *
 * project: org.matsim.*
 * VisumMatrixWriter.java
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

package org.matsim.visum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;
import org.matsim.utils.identifiers.IdI;

/**
 * @author mrieser
 *
 * @param <T> Element type of the matrix
 *
 * Writes a single matrix in a VISUM-compatible format to file.
 * Can be used e.g. for OD-matrices.
 */
public class VisumMatrixWriter<T> {

	Matrix<T> matrix;
	Set<IdI> ids;

	public VisumMatrixWriter(Matrix<T> matrix) {
		super();
		this.matrix = matrix;
		this.ids = new TreeSet<IdI>();
		this.ids.addAll(matrix.getFromLocations().keySet());
		this.ids.addAll(matrix.getToLocations().keySet());
	}

	/**
	 * @param ids Set of ids to use as row- and column-header in the matrix
	 *
	 * sets the row- and column-header used when writing the matrix. If the ids
	 * are not set explicitly, the ids in the matrix are used. <br />
	 * useful, if the matrix is sparse and not all possible rows and columns
	 * contain values, but should still be written out in the matrix containing
	 * only zeros.
	 */
	public void setIds(Set<IdI> ids) {
		this.ids = ids;
	}

	public void writeFile(String filename) {
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter(filename));

			out.write("$VN;Y5\n");
			out.write("*\n");
			out.write("*\tAnzahl Bezirke\n");
			out.write(this.ids.size() + "\n");

			out.write("*\tBezirksNummern\n");
			int cnt = 0;
			for (IdI value : this.ids) {
				cnt++;
				if (cnt > 1) {
					out.write("\t");
				}
				out.write(value.toString());
			}
			out.write("\n");

			for (IdI from : this.ids) {
				out.write("*\t" + from.toString() + "\n");
				cnt = 0;
				for (IdI to : this.ids) {
					cnt++;
					Entry<T> e = this.matrix.getEntry(from, to);
					if (cnt > 1) {
						out.write("\t");
					}
					if (e == null) {
						out.write("0");
					} else {
						out.write(e.getValue().toString());
					}
				}
				out.write("\n");
			}

			out.write("\n");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try { out.close(); } catch (IOException ignored) {}
			}
		}
	}
}
