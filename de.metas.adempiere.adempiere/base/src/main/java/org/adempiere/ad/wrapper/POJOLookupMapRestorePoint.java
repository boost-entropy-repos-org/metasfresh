package org.adempiere.ad.wrapper;

/*
 * #%L
 * ADempiere ERP - Base
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-memory database restore point.
 * 
 * It persist the database state, allowing you to {@link #restore()} at any time.
 * 
 * @author tsa
 *
 */
public class POJOLookupMapRestorePoint
{
	private final POJOLookupMap db;
	private final Map<String, Map<Integer, Object>> cachedObjects;
	private final Map<Integer, Set<Integer>> selectionId2selection;

	POJOLookupMapRestorePoint(final POJOLookupMap db)
	{
		super();
		this.db = db;
		this.cachedObjects = copyCachedObjects(db.cachedObjects);
		this.selectionId2selection = copySelection(db.selectionId2selection);
	}

	private static final Map<String, Map<Integer, Object>> copyCachedObjects(Map<String, Map<Integer, Object>> cachedObjects)
	{
		final Map<String, Map<Integer, Object>> cachedObjectsCopy = new HashMap<>(cachedObjects);
		for (Map.Entry<String, Map<Integer, Object>> e : cachedObjectsCopy.entrySet())
		{
			final Map<Integer, Object> value = e.getValue();
			e.setValue(value == null ? null : new HashMap<>(value));
		}
		return cachedObjectsCopy;
	}

	private static final Map<Integer, Set<Integer>> copySelection(Map<Integer, Set<Integer>> selectionId2selection)
	{
		final Map<Integer, Set<Integer>> selectionId2selectionCopy = new HashMap<>(selectionId2selection);
		for (Map.Entry<Integer, Set<Integer>> e : selectionId2selectionCopy.entrySet())
		{
			final Set<Integer> value = e.getValue();
			e.setValue(value == null ? null : new HashSet<>(value));
		}
		return selectionId2selectionCopy;
	}

	/**
	 * Restore the database as it was when this restore point was created.
	 */
	public void restore()
	{
		this.db.cachedObjects = copyCachedObjects(this.cachedObjects);
		this.db.selectionId2selection = copySelection(this.selectionId2selection);
	}
}
