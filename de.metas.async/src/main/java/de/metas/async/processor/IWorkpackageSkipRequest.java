package de.metas.async.processor;

/*
 * #%L
 * de.metas.async
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
 * Exceptions which implement this interface will provide Skipping infos to workpackage processor. Based on these informations the system will decide if the workpackage needs to be skipped, for how
 * long and which was the reason.
 * 
 * @author tsa
 * 
 */
public interface IWorkpackageSkipRequest
{
	
//	ts: currently not used 
//	/**
//	 * If this value is returned by {@link #getSkipTimeoutMillis()} then default timeout (configured in processor) will be used instead
//	 */
//	int SKIPTIMEOUT_Auto = -100;

	/**
	 * 
	 * @return true if execution's workpackage needs to be skipped this time
	 */
	boolean isSkip();

	/**
	 * 
	 * @return skip reason (i.e. error summary message)
	 */
	String getSkipReason();

	/**
	 * 
	 * @return after how many millis system can check again the current workpackage. If negative number then system will use default timeout.
	 */
	int getSkipTimeoutMillis();

	/**
	 * 
	 * @return optional exception for skipping reason
	 */
	Exception getException();
}
