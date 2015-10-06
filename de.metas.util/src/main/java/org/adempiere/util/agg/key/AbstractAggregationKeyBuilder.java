package org.adempiere.util.agg.key;

/*
 * #%L
 * org.adempiere.util
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

import org.adempiere.util.Services;

/**
 * Base class for normal {@link IAggregationKeyBuilder}s
 *
 * @author al
 *
 * @param <T>
 */
public abstract class AbstractAggregationKeyBuilder<T> implements IAggregationKeyBuilder<T>
{
	protected final IAggregationKeyRegistry aggregationKeyRegistry = Services.get(IAggregationKeyRegistry.class);

	protected final String registrationKey;

	public AbstractAggregationKeyBuilder(final String registrationKey)
	{
		super();

		this.registrationKey = registrationKey;
	}

	@Override
	public final List<String> getDependsOnColumnNames()
	{
		return aggregationKeyRegistry.getDependsOnColumnNames(registrationKey);
	}
}
