package de.metas.banking.payment;

/*
 * #%L
 * de.metas.banking.base
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


import org.adempiere.model.IContextAware;

import de.metas.interfaces.I_C_BP_BankAccount;

/**
 * Interface used to retrieve information from an {@link IPaymentString}.
 *
 * @author al
 */
public interface IPaymentStringDataProvider
{
	IPaymentString getPaymentString();

	I_C_BP_BankAccount getC_BP_BankAccountOrNull();

	/**
	 * Create and save a new C_BP_BankAccount based on the data contained in this provider.
	 *
	 * @param contextProvider
	 * @param bpartnerId
	 * @return {@link I_C_BP_BankAccount} which was created
	 */
	I_C_BP_BankAccount createNewC_BP_BankAccount(IContextAware contextProvider, int bpartnerId);
}
