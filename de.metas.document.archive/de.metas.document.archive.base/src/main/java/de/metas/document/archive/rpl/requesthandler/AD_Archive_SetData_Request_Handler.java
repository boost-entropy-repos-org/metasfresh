package de.metas.document.archive.rpl.requesthandler;

/*
 * #%L
 * de.metas.document.archive.base
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


import org.adempiere.process.rpl.requesthandler.spi.impl.LoadConvertPORequestHandler;
import org.adempiere.util.collections.Converter;

import de.metas.document.archive.model.I_AD_Archive;

public class AD_Archive_SetData_Request_Handler extends LoadConvertPORequestHandler<I_AD_Archive, I_AD_Archive>
{

	public AD_Archive_SetData_Request_Handler(final Class<I_AD_Archive> requestModelClass, final Converter<I_AD_Archive, I_AD_Archive> converter)
	{
		super(I_AD_Archive.class, ArchiveSetDataHandlerConverter.instance);
	}
}
