/**
 * 
 */
package de.metas.contracts.order;

import java.util.HashSet;
import java.util.Set;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.proxy.Cached;
import org.springframework.stereotype.Service;

import de.metas.contracts.model.I_C_Flatrate_Term;
import de.metas.contracts.subscription.model.I_C_Order;
import de.metas.order.IOrderDAO;
import de.metas.order.OrderId;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2018 metas GmbH
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

/**
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@Service
public class ContractOrderService
{
	/**
	 * retrieves the linked order through column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>
	 * @param orderId
	 * @return
	 */
	@Cached(cacheName = I_C_Order.Table_Name + "#by#OrderId")
	public OrderId retrieveLinkedFollowUpContractOrder(@NonNull final OrderId orderId)
	{
		int oroginalOrderId = Services.get(IQueryBL.class).createQueryBuilder(I_C_Order.class)
				.addOnlyActiveRecordsFilter()
				.addOnlyContextClient()
				.addEqualsFilter(I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID, orderId)
				.create()
				.firstId();

		return OrderId.ofRepoIdOrNull(oroginalOrderId);
	}

	public Set<OrderId> retrieveAllContractOrderList(@NonNull final OrderId orderId)
	{
		final Set<OrderId> orderIds = new HashSet<>();
		final OrderId ancestorId = retrieveOriginalContractOrder(orderId);
		if (ancestorId != null)
		{
			orderIds.add(ancestorId);
			buildAllContractOrderList(ancestorId, orderIds);
		}
		else
		{	// add itself
			orderIds.add(orderId);
		}
			

		return orderIds;
	}

	/**
	 * retrieves original order through column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>,
	 * going recursively until the original one
	 * 
	 * @param orderId
	 * @return OrderId
	 */
	@Cached(cacheName = I_C_Order.Table_Name + "#by#OrderId")
	public OrderId retrieveOriginalContractOrder(@NonNull final OrderId orderId)
	{
		OrderId ancestor = retrieveLinkedFollowUpContractOrder(orderId);

		if (ancestor != null)
		{
			final OrderId nextAncestor = retrieveOriginalContractOrder(ancestor);
			if (nextAncestor != null)
			{
				ancestor = nextAncestor;
			}
		}

		return ancestor;
	}

	/**
	 * builds up a list of contract orders based on column <code>I_C_Order.COLUMNNAME_Ref_FollowupOrder_ID</code>
	 * 
	 * @param orderId
	 * @param contractOrderIds
	 * @return
	 */
	private void buildAllContractOrderList(@NonNull final OrderId orderId, @NonNull Set<OrderId> contractOrderIds)
	{
		final I_C_Order order = InterfaceWrapperHelper.load(orderId, I_C_Order.class);
		final OrderId nextAncestorId = OrderId.ofRepoIdOrNull(order.getRef_FollowupOrder_ID());
		if (nextAncestorId != null)
		{
			contractOrderIds.add(nextAncestorId);
			buildAllContractOrderList(nextAncestorId, contractOrderIds);
		}
	}
	
	public I_C_Flatrate_Term retrieveTopExtendedTerm(@NonNull final I_C_Flatrate_Term term)
	{
		I_C_Flatrate_Term nextTerm = term.getC_FlatrateTerm_Next();

		if (nextTerm != null)
		{
			nextTerm = retrieveTopExtendedTerm(nextTerm);
		}

		return nextTerm == null ? term : nextTerm;
	}
	
	public OrderId getContractOrderId(@NonNull final I_C_Flatrate_Term term)
	{
		if (term.getC_OrderLine_Term_ID() <= 0)
		{
			return null;
		}
		
		final IOrderDAO orderRepo = Services.get(IOrderDAO.class);
		final de.metas.interfaces.I_C_OrderLine ol = orderRepo.getOrderLineById(term.getC_OrderLine_Term_ID());
		if (ol == null)
		{
			return null;
		}

		return OrderId.ofRepoId(ol.getC_Order_ID());
	}
}
