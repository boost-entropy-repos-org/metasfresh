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


import java.util.List;

import org.adempiere.ad.modelvalidator.annotations.DocValidate;
import org.adempiere.util.Services;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.ModelValidator;

import de.metas.materialtracking.IMaterialTrackingAttributeBL;
import de.metas.materialtracking.IMaterialTrackingBL;
import de.metas.materialtracking.MTLinkRequest;
import de.metas.materialtracking.model.I_M_Material_Tracking;

/**
 * Template for Material Track documents which have an ASI in their lines and that ASI contains Material Tracking Attribute.
 *
 * Following cases are covered:
 * <ul>
 * <li>on document Complete, take the Material Tracking from document line's ASI and link that document line to material tracking
 * <li>on document ReActivate/Void/Reverse, unlink the document line from material tracking
 * </ul>
 *
 * @author tsa
 *
 * @param <DocumentType>
 * @param <DocumentLineType>
 */
public abstract class MaterialTrackableDocumentByASIInterceptor<DocumentType, DocumentLineType>
{
	@DocValidate(timings = { ModelValidator.TIMING_AFTER_COMPLETE })
	public final void linkToMaterialTracking(final DocumentType document)
	{
		if (!isEligibleForMaterialTracking(document))
		{
			return;
		}

		final IMaterialTrackingBL materialTrackingBL = Services.get(IMaterialTrackingBL.class);

		final List<DocumentLineType> documentLines = retrieveDocumentLines(document);
		for (final DocumentLineType documentLine : documentLines)
		{
			final I_M_Material_Tracking materialTracking = getMaterialTrackingFromDocumentLineASI(documentLine);
			if (materialTracking == null)
			{
				materialTrackingBL.unlinkModelFromMaterialTracking(documentLine);
			}
			else
			{
				materialTrackingBL.linkModelToMaterialTracking(
						MTLinkRequest.builder()
								.setModel(documentLine)
								.setMaterialTracking(materialTracking)
								.build());
			}
		}
	}

	@DocValidate(timings = { ModelValidator.TIMING_BEFORE_REACTIVATE
			, ModelValidator.TIMING_BEFORE_VOID
			, ModelValidator.TIMING_BEFORE_REVERSECORRECT
			, ModelValidator.TIMING_BEFORE_REVERSEACCRUAL })
	public final void unlinkFromMaterialTracking(final DocumentType document)
	{
		if (!isEligibleForMaterialTracking(document))
		{
			return;
		}

		final IMaterialTrackingBL materialTrackingBL = Services.get(IMaterialTrackingBL.class);

		//
		// Unlink document header from any material tracking
		materialTrackingBL.unlinkModelFromMaterialTracking(document);

		//
		// Iterate lines and unlink them from any material tracking
		final List<DocumentLineType> documentLines = retrieveDocumentLines(document);
		for (final DocumentLineType documentLine : documentLines)
		{
			materialTrackingBL.unlinkModelFromMaterialTracking(documentLine);
		}
	}

	/**
	 *
	 * @param documentLine
	 * @return material tracking from document's ASI
	 */
	protected I_M_Material_Tracking getMaterialTrackingFromDocumentLineASI(final DocumentLineType documentLine)
	{
		final IMaterialTrackingAttributeBL materialTrackingAttributeBL = Services.get(IMaterialTrackingAttributeBL.class);

		final I_M_AttributeSetInstance asi = getM_AttributeSetInstance(documentLine);
		final I_M_Material_Tracking materialTracking = materialTrackingAttributeBL.getMaterialTracking(asi);
		return materialTracking;
	}

	/**
	 *
	 * @param document
	 * @return true if given document is eligible for material tracking
	 */
	protected abstract boolean isEligibleForMaterialTracking(final DocumentType document);

	/**
	 *
	 * @param document
	 * @return document lines
	 */
	protected abstract List<DocumentLineType> retrieveDocumentLines(final DocumentType document);

	/**
	 *
	 * @param documentLine
	 * @return ASI from document line
	 */
	protected abstract I_M_AttributeSetInstance getM_AttributeSetInstance(final DocumentLineType documentLine);
}
