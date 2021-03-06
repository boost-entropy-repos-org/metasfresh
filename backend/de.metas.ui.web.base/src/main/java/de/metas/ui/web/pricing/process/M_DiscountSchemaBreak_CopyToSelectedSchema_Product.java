package de.metas.ui.web.pricing.process;

import java.util.HashSet;
import java.util.Set;

import org.adempiere.ad.dao.ICompositeQueryFilter;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.model.PlainContextAware;
import org.compiere.model.I_M_DiscountSchema;
import org.compiere.model.I_M_DiscountSchemaBreak;
import org.compiere.model.I_M_DiscountSchemaBreak_V;
import org.compiere.model.I_M_Product;

import com.google.common.collect.ImmutableSet;

import de.metas.pricing.conditions.CopyDiscountSchemaBreaksRequest;
import de.metas.pricing.conditions.CopyDiscountSchemaBreaksRequest.Direction;
import de.metas.pricing.conditions.PricingConditionsId;
import de.metas.pricing.conditions.service.IPricingConditionsRepository;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.Param;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.product.ProductId;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2019 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class M_DiscountSchemaBreak_CopyToSelectedSchema_Product extends JavaProcess implements IProcessPrecondition
{

	private final IPricingConditionsRepository pricingConditionsRepo = Services.get(IPricingConditionsRepository.class);

	final String PARAM_M_Product_ID = I_M_Product.COLUMNNAME_M_Product_ID;

	@Param(parameterName = I_M_DiscountSchema.COLUMNNAME_M_DiscountSchema_ID, mandatory = true)
	private int p_PricingConditionsId;

	@Param(parameterName = PARAM_M_Product_ID, mandatory = true)
	private int p_ProductId;

	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(@NonNull IProcessPreconditionsContext context)
	{

		if (context.isNoSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}
		
		

		final PlainContextAware localCtx =  PlainContextAware.newWithThreadInheritedTrx(getCtx());
		
		final Set<ProductId> breaks = context.getSelectedIncludedRecords()
				.stream()
				.map(recordRef -> recordRef.getModel(localCtx, I_M_DiscountSchemaBreak_V.class))
				.map(discBreak -> ProductId.ofRepoId(discBreak.getM_Product_ID()))
				.collect(ImmutableSet.toImmutableSet());

		if (breaks.size() > 1)
		{
			return ProcessPreconditionsResolution.reject();
		}

		return ProcessPreconditionsResolution.accept();
	}	
	
	@Override
	protected String doIt()
	{
		
		
		final PlainContextAware localCtx = PlainContextAware.newWithThreadInheritedTrx(getCtx());
		
		final Set<I_M_DiscountSchemaBreak_V> breaks = getProcessInfo().getSelectedIncludedRecords()
				.stream()
				.map(recordRef -> recordRef.getModel(localCtx, I_M_DiscountSchemaBreak_V.class))
				.collect(ImmutableSet.toImmutableSet());

		final Set<ProductId> products = new HashSet<ProductId>();
		final Set<PricingConditionsId> pricingConditions = new HashSet<PricingConditionsId>();

		breaks.forEach(record -> {

			products.add(ProductId.ofRepoId(record.getM_Product_ID()));

			//
			pricingConditions.add(PricingConditionsId.ofRepoId(record.getM_DiscountSchema_ID()));

		});
		
		final ICompositeQueryFilter<I_M_DiscountSchemaBreak> queryFilter = Services.get(IQueryBL.class)
				.createCompositeQueryFilter(I_M_DiscountSchemaBreak.class)
				.setJoinAnd()
				.addInArrayFilter(I_M_DiscountSchemaBreak.COLUMNNAME_M_DiscountSchema_ID, pricingConditions)
				.addInArrayFilter(I_M_DiscountSchemaBreak.COLUMNNAME_M_Product_ID, products);
		
		final boolean allowCopyToSameSchema = true;

		final CopyDiscountSchemaBreaksRequest request = CopyDiscountSchemaBreaksRequest.builder()
				.filter(queryFilter)
				.pricingConditionsId(PricingConditionsId.ofRepoId(p_PricingConditionsId))
				.productId(ProductId.ofRepoId(p_ProductId))
				.allowCopyToSameSchema(allowCopyToSameSchema)
				.direction(Direction.TargetSource)
				.build();
		
		pricingConditionsRepo.copyDiscountSchemaBreaksWithProductId(request);

		return MSG_OK;
	}

}
