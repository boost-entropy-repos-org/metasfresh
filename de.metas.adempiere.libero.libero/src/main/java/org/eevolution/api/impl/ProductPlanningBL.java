package org.eevolution.api.impl;

/*
 * #%L
 * de.metas.adempiere.libero.libero
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


import java.util.Properties;

import org.adempiere.ad.persistence.ModelDynAttributeAccessor;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Check;
import org.eevolution.api.IProductPlanningBL;
import org.eevolution.model.I_PP_Product_Planning;

public class ProductPlanningBL implements IProductPlanningBL
{
	// private final transient CLogger logger = CLogger.getCLogger(getClass());

	private static final ModelDynAttributeAccessor<I_PP_Product_Planning, Integer> DYNATTR_Base_Product_Planning_ID = new ModelDynAttributeAccessor<>("Base_Product_Planning_ID", Integer.class);

	@Override
	public I_PP_Product_Planning createPlainProductPlanning(final Properties ctx)
	{
		final I_PP_Product_Planning productPlanningNew = InterfaceWrapperHelper.create(ctx, I_PP_Product_Planning.class, ITrx.TRXNAME_None);
		InterfaceWrapperHelper.setSaveDeleteDisabled(productPlanningNew, true);
		return productPlanningNew;
	}

	@Override
	public I_PP_Product_Planning createPlainProductPlanning(final I_PP_Product_Planning productPlanning)
	{
		Check.assumeNotNull(productPlanning, "productPlanning not null");

		final Properties ctx = InterfaceWrapperHelper.getCtx(productPlanning);
		final I_PP_Product_Planning productPlanningNew = createPlainProductPlanning(ctx);
		InterfaceWrapperHelper.setSaveDeleteDisabled(productPlanningNew, true);
		DYNATTR_Base_Product_Planning_ID.setValue(productPlanningNew, productPlanning.getPP_Product_Planning_ID());

		// honorIsCalculated=false=copy everything
		InterfaceWrapperHelper.copyValues(productPlanning, productPlanningNew, false);

		return productPlanningNew;
	}

	@Override
	public int getBase_Product_Planning_ID(final I_PP_Product_Planning productPlanning)
	{
		if (productPlanning == null)
		{
			return -1;
		}

		// If this product planning is in database, return it's ID
		Integer productPlanningId = productPlanning.getPP_Product_Planning_ID();
		if (productPlanningId > 0)
		{
			return productPlanningId;
		}

		// If this product planning is a plain one, created based on another product planning,
		// get the ID of that one and return it
		productPlanningId = DYNATTR_Base_Product_Planning_ID.getValue(productPlanning);
		if (productPlanningId != null && productPlanningId > 0)
		{
			return productPlanningId;
		}

		//
		// Fallback: we don't know which is the actual/base product planning ID
		return -1;
	}
}
