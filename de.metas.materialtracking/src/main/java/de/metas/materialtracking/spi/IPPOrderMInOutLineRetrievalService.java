package de.metas.materialtracking.spi;

import java.util.List;

import org.adempiere.util.ISingletonService;
import org.eevolution.model.I_PP_Cost_Collector;

import de.metas.materialtracking.model.I_M_InOutLine;

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

/**
 * As of now, this service needs to be implemented in <code>de.metas.handlingunits</code>.
 * 
 * @author metas-dev <dev@metas-fresh.com>
 *
 */
public interface IPPOrderMInOutLineRetrievalService extends ISingletonService
{
	/**
	 * For the given cost collector, provide the receipt lines for the material that was issued.<br>
	 * Returns only lines whose inOut is completed or closed.
	 * 
	 * @param issueCostCollector
	 * @return
	 */
	List<I_M_InOutLine> provideIssuedInOutLines(I_PP_Cost_Collector issueCostCollector);
}
