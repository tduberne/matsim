package org.matsim.core.mobsim.qsim.pt;

import com.google.inject.Inject;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

public class TransitEngineModule extends AbstractQSimModule {
	public final static String TRANSIT_ENGINE_NAME = "TransitEngine";

	@Override
	protected void configureQSim() {
		bind(TransitQSimEngine.class).asEagerSingleton();
		addNamedComponent(TransitQSimEngine.class, TRANSIT_ENGINE_NAME);

		if ( this.getConfig().transit().isUseTransit() && this.getConfig().transit().isUsingTransitInMobsim() ) {
			bind( TransitStopHandlerFactory.class ).to( ComplexTransitStopHandlerFactory.class ) ;
		} else {
			// Explicit bindings are required, so although it may not be used, we need provide something.
			bind( TransitStopHandlerFactory.class ).to( SimpleTransitStopHandlerFactory.class );
		}

	}
}
