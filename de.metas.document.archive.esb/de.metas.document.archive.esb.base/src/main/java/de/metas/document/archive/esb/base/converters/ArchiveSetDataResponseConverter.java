package de.metas.document.archive.esb.base.converters;

/*
 * #%L
 * de.metas.document.archive.esb.base
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


import org.adempiere.util.collections.Converter;

import de.metas.document.archive.esb.api.ArchiveSetDataResponse;
import de.metas.document.archive.esb.base.jaxb.ADArchiveSetDataResponseType;

public class ArchiveSetDataResponseConverter implements Converter<ArchiveSetDataResponse, ADArchiveSetDataResponseType>
{
	public static final transient ArchiveSetDataResponseConverter instance = new ArchiveSetDataResponseConverter();

	@Override
	public ArchiveSetDataResponse convert(final ADArchiveSetDataResponseType responseXml)
	{
		final ArchiveSetDataResponse response = new ArchiveSetDataResponse();
		response.setAdArchiveId(responseXml.getADArchiveID() == null ? -1 : responseXml.getADArchiveID().intValue());
		return response;
	}

}
