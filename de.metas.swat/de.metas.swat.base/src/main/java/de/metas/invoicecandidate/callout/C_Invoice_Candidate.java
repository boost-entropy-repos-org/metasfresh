package de.metas.invoicecandidate.callout;

/*
 * #%L
 * de.metas.swat.base
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


import org.adempiere.ad.callout.annotations.Callout;
import org.adempiere.ad.callout.annotations.CalloutMethod;
import org.adempiere.ad.callout.api.ICalloutField;

import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;


@Callout(I_C_Invoice_Candidate.class)
public class C_Invoice_Candidate
{
	
	@CalloutMethod(columnNames = { I_C_Invoice_Candidate.COLUMNNAME_QualityDiscountPercent_Override })
	public void onQualityDiscountPercentOverride(final I_C_Invoice_Candidate ic, final ICalloutField field)
	{
		ic.setIsInDispute(false);
	}
}