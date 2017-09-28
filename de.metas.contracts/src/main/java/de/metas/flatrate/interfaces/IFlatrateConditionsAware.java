package de.metas.flatrate.interfaces;

/*
 * #%L
 * de.metas.contracts
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

import de.metas.flatrate.model.I_C_Flatrate_Conditions;

public interface IFlatrateConditionsAware
{
	// @formatter:off
	String COLUMNNAME_C_Flatrate_Conditions_ID = "C_Flatrate_Conditions_ID";
	
	void setC_Flatrate_Conditions(I_C_Flatrate_Conditions C_Flatrate_Conditions);
	void setC_Flatrate_Conditions_ID(int C_Flatrate_Conditions_ID);
	I_C_Flatrate_Conditions getC_Flatrate_Conditions();
	int getC_Flatrate_Conditions_ID();
	// @formatter:on
}
