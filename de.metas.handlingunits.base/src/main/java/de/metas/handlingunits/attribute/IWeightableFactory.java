package de.metas.handlingunits.attribute;

/*
 * #%L
 * de.metas.handlingunits.base
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


import org.adempiere.mm.attributes.api.IAttributeSet;
import org.adempiere.util.IMultitonService;

/**
 * Service used to wrap various objects and create {@link IWeightable} interfaces for them.
 *
 * @author tsa
 *
 */
public interface IWeightableFactory extends IMultitonService
{
	/**
	 * Creates the {@link IWeightable} wrapper for given {@link IAttributeSet}.
	 *
	 * If {@link IAttributeSet} is not suitable to be weightable then <code>null</code> will be returned.
	 *
	 * @param attributeSet
	 * @return
	 */
	IWeightable createWeightableOrNull(IAttributeSet attributeSet);

}
