package de.metas.materialtracking.qualityBasedInvoicing.ic.spi.impl;

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
import java.util.Properties;

import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Services;

import de.metas.invoicecandidate.api.IInvoiceCandDAO;
import de.metas.invoicecandidate.model.I_C_Invoice_Candidate;
import de.metas.invoicecandidate.spi.AbstractInvoiceCandidateHandler;
import de.metas.invoicecandidate.spi.IInvoiceCandidateHandler;
import de.metas.materialtracking.model.I_PP_Order;

/**
 * Creates invoice candidates for PP_Orders which have DocType with BaseType <code>MOP</code> and a special subtype to indicate it's a quality order
 *
 * @author ts
 *
 */
public class QualityInspectionHandler extends AbstractInvoiceCandidateHandler
{
	private final PPOrder2InvoiceCandidatesProducer createInvoiceCandidatesProducer()
	{
		final PPOrder2InvoiceCandidatesProducer invoiceCandidatesProducer = new PPOrder2InvoiceCandidatesProducer();
		invoiceCandidatesProducer.setC_ILCandHandler(getHandlerRecord());
		return invoiceCandidatesProducer;
	}

	@Override
	public List<I_C_Invoice_Candidate> createMissingCandidates(final Properties ctx, final int limit, final String trxName)
	{
		final PPOrder2InvoiceCandidatesProducer invoiceCandidatesProducer = createInvoiceCandidatesProducer();
		final List<de.metas.materialtracking.model.I_C_Invoice_Candidate> invoiceCandidates = invoiceCandidatesProducer.createMissingInvoiceCandidates(ctx, limit, trxName);
		return InterfaceWrapperHelper.createList(invoiceCandidates, I_C_Invoice_Candidate.class);
	}

	@Override
	public List<I_C_Invoice_Candidate> createCandidatesFor(final Object model)
	{
		final I_PP_Order ppOrder = InterfaceWrapperHelper.create(model, I_PP_Order.class);

		final PPOrder2InvoiceCandidatesProducer invoiceCandidatesProducer = createInvoiceCandidatesProducer();
		final List<de.metas.materialtracking.model.I_C_Invoice_Candidate> invoiceCandidates = invoiceCandidatesProducer.createInvoiceCandidates(ppOrder);
		return InterfaceWrapperHelper.createList(invoiceCandidates, I_C_Invoice_Candidate.class);
	}

	@Override
	public void invalidateCandidatesFor(final Object model)
	{
		final IInvoiceCandDAO invoiceCandDAO = Services.get(IInvoiceCandDAO.class);

		final List<I_C_Invoice_Candidate> referencingICs = invoiceCandDAO.retrieveReferencing(model);

		for (final I_C_Invoice_Candidate ic : referencingICs)
		{
			invoiceCandDAO.invalidateCand(ic);
		}
	}

	@Override
	public String getSourceTable()
	{
		return org.eevolution.model.I_PP_Order.Table_Name;
	}

	@Override
	public boolean isUserInChargeUserEditable()
	{
		return true;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void setOrderedData(final I_C_Invoice_Candidate ic)
	{
		// nothing to do; the value won't change
	}

	/**
	 * <ul>
	 * <li>QtyDelivered := QtyOrdered
	 * <li>DeliveryDate := DateOrdered
	 * <li>M_InOut_ID: untouched
	 * </ul>
	 * 
	 * @see IInvoiceCandidateHandler#setDeliveredData(I_C_Invoice_Candidate)
	 */
	@Override
	public void setDeliveredData(final I_C_Invoice_Candidate ic)
	{
		ic.setQtyDelivered(ic.getQtyOrdered());
		ic.setDeliveryDate(ic.getDateOrdered());
	}

	@Override
	public void setPriceActual(final I_C_Invoice_Candidate ic)
	{
		// nothing to do; the value won't change
	}

	@Override
	public void setBPartnerData(final I_C_Invoice_Candidate ic)
	{
		// nothing to do; the value won't change
	}

	@Override
	public void setC_UOM_ID(final I_C_Invoice_Candidate ic)
	{
		// nothing to do; the value won't change
	}

	@Override
	public void setPriceEntered(final I_C_Invoice_Candidate ic)
	{
		ic.setPriceEntered(ic.getPriceActual());
	}

}
