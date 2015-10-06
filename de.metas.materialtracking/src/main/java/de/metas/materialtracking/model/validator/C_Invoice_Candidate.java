package de.metas.materialtracking.model.validator;

/*
 * #%L
 * de.metas.materialtracking
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


import org.adempiere.ad.dao.cache.impl.TableRecordCacheLocal;
import org.adempiere.ad.modelvalidator.annotations.Init;
import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_Table;
import org.compiere.model.ModelValidator;

import de.metas.inout.model.I_M_InOutLine;
import de.metas.invoicecandidate.api.IInvoiceCandidateListeners;
import de.metas.materialtracking.IMaterialTrackingBL;
import de.metas.materialtracking.MTLinkRequest;
import de.metas.materialtracking.model.I_C_Invoice_Candidate;
import de.metas.materialtracking.model.I_M_Material_Tracking;
import de.metas.materialtracking.qualityBasedInvoicing.IQualityInspectionHandlerDAO;
import de.metas.materialtracking.qualityBasedInvoicing.ic.spi.impl.MaterialTrackingInvoiceCandidateListener;

@Interceptor(I_C_Invoice_Candidate.class)
public class C_Invoice_Candidate
{
	@Init
	public void onInit()
	{
		final IInvoiceCandidateListeners invoiceCandidateListeners = Services.get(IInvoiceCandidateListeners.class);
		invoiceCandidateListeners.addListener(MaterialTrackingInvoiceCandidateListener.instance);
	}

	/**
	 * Checks if the given <code>ic</code> references a record that is already tracked. If that is the case, the ic's
	 * {@link de.metas.materialtracking.model.I_C_Invoice_Candidate#COLUMNNAME_M_Material_Tracking_ID M_Material_Tracking_ID} is set to the referenced object's tracking ID.
	 * 
	 * @param ic
	 */
	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW })
	public void referenceMaterialTracking(final I_C_Invoice_Candidate ic)
	{
		final Object referencedObject = getReferencedObjectOrNull(ic);
		Services.get(IQualityInspectionHandlerDAO.class).updateICFromMaterialTracking(ic, referencedObject);
	}

	private Object getReferencedObjectOrNull(final I_C_Invoice_Candidate ic)
	{
		final Object referencedObject;

		if (ic.getC_OrderLine_ID() > 0)
		{
			referencedObject = ic.getC_OrderLine();
		}
		else
		{
			final I_AD_Table table = ic.getAD_Table();
			if (table != null && I_M_InOutLine.Table_Name.equals(table.getTableName()))
			{
				referencedObject = TableRecordCacheLocal.getReferencedValue(ic, I_M_InOutLine.class);
			}
			else
			{
				referencedObject = null;
			}
		}
		return referencedObject;
	}

	/**
	 * Checks if the given <code>ic</code> has {@link de.metas.materialtracking.model.I_C_Invoice_Candidate#COLUMNNAME_M_Material_Tracking_ID M_Material_Tracking_ID} <code>>0</code>. If that is the
	 * case, it is also linked to the tracking using {@link IMaterialTrackingBL#linkModelToMaterialTracking(Object, I_M_Material_Tracking)}.
	 * 
	 * @param ic
	 */
	@ModelChange(timings = { ModelValidator.TYPE_AFTER_NEW })
	public void linkToMaterialTracking(final I_C_Invoice_Candidate ic)
	{
		final boolean createLink = true;
		updateLinkToTrackingIfNotNull(ic, createLink);
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_DELETE })
	public void unlinkFromMaterialTracking(final I_C_Invoice_Candidate ic)
	{
		final boolean createLink = false; // i.e. remove the link
		updateLinkToTrackingIfNotNull(ic, createLink);
	}
	
	private void updateLinkToTrackingIfNotNull(final I_C_Invoice_Candidate ic, final boolean createLink)
	{
		final de.metas.materialtracking.model.I_C_Invoice_Candidate icExt = InterfaceWrapperHelper.create(ic, de.metas.materialtracking.model.I_C_Invoice_Candidate.class);
		if (icExt.getM_Material_Tracking_ID() <= 0)
		{
			return;
		}

		final IMaterialTrackingBL materialTrackingBL = Services.get(IMaterialTrackingBL.class);
		if (createLink)
		{
			materialTrackingBL.linkModelToMaterialTracking(MTLinkRequest.builder()
					.setModel(ic)
					.setMaterialTracking(ic.getM_Material_Tracking())
					.build());
		}
		else
		{
			materialTrackingBL.unlinkModelFromMaterialTracking(ic);
		}
	}
}
