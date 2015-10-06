package org.adempiere.ad.callout.api.impl;

/*
 * #%L
 * ADempiere ERP - Base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.util.List;
import java.util.logging.Level;

import org.adempiere.ad.callout.api.ICalloutFactory;
import org.adempiere.ad.callout.api.ICalloutField;
import org.adempiere.ad.callout.api.ICalloutInstance;
import org.adempiere.ad.callout.spi.ICalloutProvider;
import org.adempiere.ad.callout.spi.impl.CompositeCalloutProvider;
import org.adempiere.ad.callout.spi.impl.DefaultCalloutProvider;
import org.compiere.util.CLogger;

public class CalloutFactory implements ICalloutFactory
{
	private static final transient CLogger logger = CLogger.getCLogger(CalloutFactory.class);
	private final CompositeCalloutProvider providers = new CompositeCalloutProvider();

	public CalloutFactory()
	{
		// Register standard providers
		providers.addCalloutProvider(new DefaultCalloutProvider());

		// NOTE: IProgramaticCalloutProvider will be registered just in time
		// providers.addCalloutProvider(Services.get(IProgramaticCalloutProvider.class));
	}

	@Override
	public void registerCalloutProvider(final ICalloutProvider provider)
	{
		final boolean added = providers.addCalloutProvider(provider);
		if (added)
		{
			logger.log(Level.CONFIG, "Registered provider: {0}", provider);
		}
	}

	@Override
	public List<ICalloutProvider> getCalloutProviders()
	{
		return providers.getProviders();
	}

	@Override
	public List<ICalloutInstance> getCallouts(final ICalloutField field)
	{
		return providers.getCallouts(field);
	}
}
