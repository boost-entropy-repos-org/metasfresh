package org.adempiere.appdict;

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


import org.adempiere.appdict.model.I_AD_JavaClass;
import org.adempiere.util.ISingletonService;

public interface IJavaClassBL extends ISingletonService
{
	/**
	 * Creates a new instance of the given class definition
	 * 
	 * @param javaClassDef
	 * @return
	 */
	<T> T newInstance(I_AD_JavaClass javaClassDef);
}
