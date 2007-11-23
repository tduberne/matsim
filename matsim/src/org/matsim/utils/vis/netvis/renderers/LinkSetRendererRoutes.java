/* *********************************************************************** *
 * project: org.matsim.*
 * LinkSetRendererRoutes.java
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

package org.matsim.utils.vis.netvis.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.matsim.basic.v01.Id;
import org.matsim.config.Config;
import org.matsim.gbl.Gbl;
import org.matsim.network.Link;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.network.NetworkLayerBuilder;
import org.matsim.plans.Leg;
import org.matsim.plans.MatsimPlansReader;
import org.matsim.plans.Person;
import org.matsim.plans.Plan;
import org.matsim.plans.Plans;
import org.matsim.plans.PlansReaderI;
import org.matsim.plans.Route;
import org.matsim.plans.algorithms.PersonAlgorithm;
import org.matsim.plans.algorithms.PlanAlgorithmI;
import org.matsim.utils.identifiers.IdI;
import org.matsim.utils.vis.netvis.VisConfig;
import org.matsim.utils.vis.netvis.gui.ControlToolbar;
import org.matsim.utils.vis.netvis.gui.NetJComponent;
import org.matsim.utils.vis.netvis.visNet.DisplayLink;
import org.matsim.utils.vis.netvis.visNet.DisplayNet;
import org.matsim.world.MatsimWorldReader;

public class LinkSetRendererRoutes extends RendererA {

	public static final int MODE_COLOR = 1;
	public static final int MODE_WIDTH = 2;

	private static final int MAX_PER_LINK = 200;

	ControlToolbar toolbar = null;

	private final TreeMap<IdI, Integer> linkValues = new TreeMap<IdI, Integer>();

	private final ValueColorizer colorizer = new ValueColorizer(new double[] { 0.0, MAX_PER_LINK }, new Color[] {
			Color.WHITE, Color.RED });

	private final DisplayNet network;

	private double linkWidth;
	private final int mode;

	public LinkSetRendererRoutes(final VisConfig visConfig, final DisplayNet network) {
		super(visConfig);
		this.network = network;
		this.mode = MODE_WIDTH;

		this.linkWidth = DisplayLink.LANE_WIDTH * visConfig.getLinkWidthFactor();
		chooseDataFile();
	}

	LinkSetRendererRoutes(final VisConfig visConfig, final DisplayNet network, final int mode) {
		super(visConfig);
		this.network = network;
		this.mode = mode;

		this.linkWidth = DisplayLink.LANE_WIDTH * visConfig.getLinkWidthFactor();
		chooseDataFile();
	}

	public void chooseDataFile() {
		String filename = "";

		JFrame tmpFrame = new JFrame("");

		FileDialog dialog = new FileDialog(tmpFrame, "Choose a config file", FileDialog.LOAD);
		dialog.setVisible(true);

		while (dialog.getFile() != null)	{
			filename = dialog.getDirectory() + "/" + dialog.getFile();

			try {
				cacheRoutes(filename);
				break;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "The file does not contain any rows with counts or volumes.");
				dialog.setVisible(true);
			}
		}
		tmpFrame.dispose();
	}

	private void cacheRoutes(final String filename) {

		Config config = Gbl.createConfig(new String[] {filename});

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(Gbl.getWorld());
		worldReader.readFile(config.world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading the network...");
		NetworkLayerBuilder.setNetworkLayerType(NetworkLayerBuilder.NETWORK_DEFAULT);
		NetworkLayer network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(config.network().getInputFile());
		System.out.println("  done.");

		System.out.println("  setting up plans objects...");
		Plans plans = new Plans(Plans.USE_STREAMING);
		PlansReaderI plansReader = new MatsimPlansReader(plans);
		plans.addAlgorithm(new RouteLinkMarker(this.linkValues));
		plansReader.readFile(config.plans().getInputFile());
		plans.printPlansCount();
		System.out.println("  done.");
	}

    @Override
	public void setTargetComponent(final NetJComponent comp) {
		super.setTargetComponent(comp);
	}

	// -------------------- RENDERING --------------------
    @Override
	protected synchronized void myRendering(final Graphics2D display, final AffineTransform boxTransform) {
		this.linkWidth = DisplayLink.LANE_WIDTH* getVisConfig().getLinkWidthFactor();

		NetJComponent comp = getNetJComponent();

		AffineTransform originalTransform = display.getTransform();

		double scale = 1.0;
		if (this.toolbar != null) {
			scale = this.toolbar.getScale();
		}

		double strokeWidth = 0.1 * this.linkWidth;
		if (strokeWidth < (40.0 / scale)) strokeWidth = 40.0 / scale; // min pixel-width on screen
		if (strokeWidth > (100.0 / scale)) strokeWidth = 100.0 / scale;  // max pixel-width on screen
		display.setStroke(new BasicStroke((float)strokeWidth));

		for (Iterator it = this.network.getLinks().iterator(); it.hasNext();) {
			DisplayLink link = (DisplayLink)it.next();

			if (!comp.checkLineInClip(link.getStartEasting(), link.getStartNorthing(),
					link.getEndEasting(), link.getEndNorthing())) {
				continue;
			}

			AffineTransform linkTransform = new AffineTransform(originalTransform);
			linkTransform.concatenate(boxTransform);
			linkTransform.concatenate(link.getLinear2PlaneTransform());

			/*
			 * (1) RENDER LINK
			 */

			display.setTransform(linkTransform);

			int cellLength_m = (int)Math.round(link.getLength_m() / link.getDisplayValueCount());
			int cellWidth_m = (int)Math.round(this.linkWidth * link.getLanes());
			int cellStart_m = 0;

			for (int i = 0; i < link.getDisplayValueCount(); i++) {

				Integer value = this.linkValues.get(new Id(link.getId().toString()));
				if (value != null) {
					if (this.mode == MODE_COLOR) {
						display.setColor(this.colorizer.getColor(value));
						display.fillRect(cellStart_m, -cellWidth_m, cellLength_m, cellWidth_m);
					} else {
						display.setColor(Color.RED);
						display.fillRect(cellStart_m, (int)Math.round(-value * 10.0 / MAX_PER_LINK * this.linkWidth), cellLength_m, (int)Math.round(value *10.0 / MAX_PER_LINK * this.linkWidth));
					}
//					display.setColor(Color.BLACK);
//					display.drawRect(cellStart_m, -cellWidth_m, cellLength_m, cellWidth_m);
				} else {
					display.setColor(Color.DARK_GRAY);
					display.drawRect(cellStart_m, -cellWidth_m, cellLength_m, cellWidth_m);
				}

				cellStart_m += cellLength_m;
			}
		}

		display.setTransform(originalTransform);

	}

	public void setControlToolbar(final ControlToolbar toolbar) {
		this.toolbar = toolbar;
	}

	public static class RouteLinkMarker extends  PersonAlgorithm implements PlanAlgorithmI {

		private final TreeMap<IdI, Integer> linkValues;

		public RouteLinkMarker(final TreeMap<IdI, Integer> linkValues) {
			this.linkValues = linkValues;
		}

		@Override
		public void run(final Person person) {
			run(person.getSelectedPlan());
		}

		public void run(final Plan plan) {
			List<?> actslegs = plan.getActsLegs();
			for (int i = 1, max = actslegs.size(); i < max; i+=2) {
				Leg leg = (Leg)actslegs.get(i);
				run(leg.getRoute());
			}
		}

		public void run(final Route route) {
			if (route == null) return;

			Link[] links = route.getLinkRoute();
			for (Link link : links) {
				IdI id = link.getId();
				Integer counter = this.linkValues.get(id);
				if (counter == null) counter = 0;
				counter++;
				this.linkValues.put(id, counter);
			}
		}

	}

}
