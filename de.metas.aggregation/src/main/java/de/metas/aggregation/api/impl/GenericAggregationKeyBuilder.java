package de.metas.aggregation.api.impl;

/*
 * #%L
 * de.metas.aggregation
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


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Check;
import org.adempiere.util.text.annotation.ToStringBuilder;
import org.adempiere.util.time.SimpleDateFormatThreadLocal;
import org.compiere.Adempiere;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Evaluatee;
import org.compiere.util.Util;

import com.google.common.collect.ImmutableList;

import de.metas.aggregation.api.AbstractAggregationKeyBuilder;
import de.metas.aggregation.api.IAggregation;
import de.metas.aggregation.api.IAggregationAttribute;
import de.metas.aggregation.api.IAggregationItem;
import de.metas.aggregation.api.IAggregationItem.Type;
import de.metas.aggregation.api.IAggregationKey;

/* package */final class GenericAggregationKeyBuilder<ModelType> extends AbstractAggregationKeyBuilder<ModelType>
{
	// services
	private static final transient CLogger logger = CLogger.getCLogger(GenericAggregationKeyBuilder.class);

	// Standard formatters to be used
	private static final transient SimpleDateFormatThreadLocal dateFormat = new SimpleDateFormatThreadLocal("yyyyMMdd");
	private static final transient SimpleDateFormatThreadLocal timeFormat = new SimpleDateFormatThreadLocal("HHmmss");
	private static final transient SimpleDateFormatThreadLocal dateTimeFormat = new SimpleDateFormatThreadLocal("yyyyMMdd-HHmmss.SSS");
	// NOTE: DecimalFormat is thread safe for now but unsure for future; more it's reported that it has performance issues
	// see: http://stackoverflow.com/questions/4387170/decimalformat-formatdouble-in-different-threads

	/** Debug? if enabled, the generated keys will also contain the source column name */
	private static final boolean debug = Adempiere.isUnitTestMode(); // only for JUnit tests; easy to debug them 

	//
	// Parameters
	private final IAggregation aggregation;
	@ToStringBuilder(skip = true)
	private final List<String> columnNames;

	public GenericAggregationKeyBuilder(final Class<ModelType> modelClass, final IAggregation aggregation)
	{
		super();

		Check.assumeNotNull(modelClass, "modelClass not null");
		Check.assumeNotNull(aggregation, "aggregation not null");

		final String modelTableName = InterfaceWrapperHelper.getTableName(modelClass);
		Check.assume(modelTableName.equals(aggregation.getTableName()), "Aggregation's model {0} shall match {1}", aggregation, modelClass);

		this.aggregation = aggregation;

		//
		// Collect column names
		final ImmutableList.Builder<String> columnNames = ImmutableList.builder();
		for (final IAggregationItem aggregationItem : aggregation.getItems())
		{
			final Type type = aggregationItem.getType();
			if (type == Type.ModelColumn)
			{
				columnNames.add(aggregationItem.getColumnName());
			}
		}
		this.columnNames = columnNames.build();
	}

	@Override
	public String getTableName()
	{
		return aggregation.getTableName();
	}

	@Override
	public List<String> getDependsOnColumnNames()
	{
		return columnNames;
	}

	@Override
	public IAggregationKey buildAggregationKey(final ModelType model)
	{
		final List<Object> keyValues = extractKeyValues(model);
		return new AggregationKey(Util.mkKey(keyValues.toArray()), aggregation.getC_Aggregation_ID());
	}

	private final List<Object> extractKeyValues(final ModelType model)
	{
		Check.assumeNotNull(model, "model not null");

		//
		// Assert model's table name is matching
		final String modelTableName = InterfaceWrapperHelper.getModelTableName(model);
		if (!aggregation.getTableName().equals(modelTableName))
		{
			throw new AdempiereException("Aggregation " + aggregation + " cannot be applied on " + model);
		}

		final List<Object> keyValues = new ArrayList<Object>();

		//
		// Collect C_Aggregation_ID to make sure we are not comparing things from different aggregation keys
		// because they could have a total different meaning (even if they are identical)
		keyValues.add(aggregation.getC_Aggregation_ID());

		//
		// Collect values for all items
		final Evaluatee evaluatee = InterfaceWrapperHelper.getEvaluatee(model);
		for (final IAggregationItem aggregationItem : aggregation.getItems())
		{
			// Check if we shall include this item in our aggregation
			if (!aggregationItem.isInclude(evaluatee))
			{
				continue;
			}

			// Collect values for current item
			final Type type = aggregationItem.getType();
			if (Type.ModelColumn == type)
			{
				addKeyValue_ColumnValue(keyValues, model, aggregationItem);
			}
			else if (Type.Attribute == type)
			{
				addKeyValue_Attribute(keyValues, model, aggregationItem);
			}
			else
			{
				final AdempiereException ex = new AdempiereException("@Unknown@ @Type@: " + type + " [IGNORED]"
						+ "\n Aggregation Item: " + aggregationItem);
				logger.log(Level.WARNING, ex.getLocalizedMessage(), ex);
				continue;
			}
		}

		return keyValues;
	}

	private void addKeyValue_ColumnValue(final List<Object> values, final ModelType model, final IAggregationItem aggregationItem)
	{
		final String columnName = aggregationItem.getColumnName();
		final Object value = InterfaceWrapperHelper.getValueOverrideOrValue(model, columnName);
		
		Object valueNormalized = normalizeValue(value, model, aggregationItem);
		if (debug)
		{
			valueNormalized = aggregationItem.getColumnName() + "=" + valueNormalized;
		}

		values.add(valueNormalized);
	}

	private void addKeyValue_Attribute(List<Object> values, ModelType model, IAggregationItem aggregationItem)
	{
		final IAggregationAttribute attribute = aggregationItem.getAttribute();
		final Evaluatee ctx = InterfaceWrapperHelper.getEvaluatee(model);
		
		Object value = attribute.evaluate(ctx);

		values.add(value);
	}

	private Object normalizeValue(final Object value, final ModelType model, final IAggregationItem aggregationItem)
	{
		final int displayType = aggregationItem.getDisplayType();

		if (DisplayType.isID(displayType))
		{
			final Integer valueInt = (Integer)value;
			if (valueInt == null)
			{
				return 0;
			}
			else if (valueInt <= 0)
			{
				return 0;
			}
			else
			{
				return valueInt;
			}
		}
		else if (displayType == DisplayType.Date)
		{
			return value == null ? null : dateFormat.format(value);
		}
		else if (displayType == DisplayType.Time)
		{
			return value == null ? null : timeFormat.format(value);
		}
		else if (displayType == DisplayType.DateTime)
		{
			return value == null ? null : dateTimeFormat.format(value);
		}
		else if (DisplayType.isText(displayType))
		{
			return value == null ? 0 : toHashcode(value.toString());
		}
		else if (displayType == DisplayType.YesNo)
		{
			return DisplayType.toBoolean(value, false);
		}
		else
		{
			return value;
		}
	}

	private static final int toHashcode(final String s)
	{
		if (Check.isEmpty(s, true))
		{
			return 0;
		}

		return s.hashCode();
	}
}
