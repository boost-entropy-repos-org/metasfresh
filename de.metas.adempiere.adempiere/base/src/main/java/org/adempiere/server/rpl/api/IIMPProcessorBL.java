package org.adempiere.server.rpl.api;

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


import java.util.Iterator;
import java.util.Properties;

import org.adempiere.server.rpl.IImportProcessor;
import org.adempiere.util.ILoggable;
import org.adempiere.util.ISingletonService;
import org.compiere.model.AdempiereProcessor;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_EXP_FormatLine;
import org.compiere.model.I_IMP_Processor;
import org.compiere.model.I_IMP_ProcessorLog;
import org.compiere.model.I_IMP_ProcessorParameter;

public interface IIMPProcessorBL extends ISingletonService
{
	I_IMP_ProcessorLog createLog(I_IMP_Processor impProcessor, String summary, String text, String reference, Throwable error);

	/**
	 * Gets XML message as String
	 * 
	 * @param pLog
	 * @return xml message or null
	 */
	String getXmlMessage(I_IMP_ProcessorLog pLog);

	org.w3c.dom.Document getXmlDocument(I_IMP_ProcessorLog plog);

	/**
	 * Mark error log as resolved (e.g. to be called after it was resubmitted successfully).
	 * 
	 * @param plog
	 */
	void markResolved(I_IMP_ProcessorLog plog);

	/**
	 * Resubmit (to be reimported) given logs.
	 * 
	 * @param logs
	 */
	void resubmit(Iterator<I_IMP_ProcessorLog> logs, boolean failfast, ILoggable loggable);

	IImportProcessor getIImportProcessor(I_IMP_Processor impProcessor);

	/**
	 * Create/Update Parameter
	 * 
	 * @param impProcessor
	 * @param key parameter key
	 * @param name parameter name (human readable)
	 * @param desc parameter description
	 * @param help parameter help
	 * @param value parameter value
	 * @return created/updated parameter
	 */
	I_IMP_ProcessorParameter createParameter(I_IMP_Processor impProcessor, String key, String name, String desc, String help, String value);

	I_IMP_ProcessorParameter createParameter(I_IMP_Processor impProcessor, String key, String value);

	AdempiereProcessor asAdempiereProcessor(I_IMP_Processor impProcessor);

	I_IMP_Processor getIMP_Processor(AdempiereProcessor adempiereProcessor);

	IImportHelper createImportHelper(Properties initialCtx);

	void setImportHelperClass(Class<? extends IImportHelper> importHelperClass);

	/**
	 * Returns the reference of the given column or (if an overriding reference is set there) from the given line.
	 * 
	 * @param column
	 * @param formatLine
	 * @return AD_Reference of column and formatLine
	 */
	int getAD_Reference_ID(I_AD_Column column, I_EXP_FormatLine formatLine);

	/**
	 * Returns the table and column that the given embedded or referencing format line points to. Throws an exception if the given line's type is neither <code>ReferencedEXPFormat</code> nor
	 * <code>EmbeddedEXPFormat</code>.
	 * 
	 * @param formatLine
	 * @return
	 */
	ITableAndColumn getTargetTableAndColumn(I_EXP_FormatLine formatLine);

	/**
	 * Simple interface to return the result of {@link IIMPProcessorBL#getTargetTableAndColumn(I_EXP_FormatLine)}
	 * 
	 * 
	 */
	interface ITableAndColumn
	{
		String getTableName();

		String getColumnName();
	}
}
