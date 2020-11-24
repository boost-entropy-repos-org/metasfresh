package de.metas.rest_api.bpartner.impl.bpartnercomposite;

import java.util.function.Predicate;

import de.metas.organization.IOrgDAO;
import de.metas.organization.OrgId;
import de.metas.organization.OrgQuery;
import de.metas.rest_api.exception.MissingResourceException;
import de.metas.util.Services;
import org.adempiere.exceptions.AdempiereException;

import de.metas.bpartner.composite.BPartnerContact;
import de.metas.bpartner.composite.BPartnerLocation;
import de.metas.rest_api.common.MetasfreshId;
import de.metas.rest_api.utils.IdentifierString;
import de.metas.rest_api.utils.IdentifierString.Type;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.compiere.util.Env;

import javax.annotation.Nullable;

import static de.metas.util.Check.isNotBlank;

/*
 * #%L
 * de.metas.business.rest-api-impl
 * %%
 * Copyright (C) 2020 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@UtilityClass
public class BPartnerCompositeRestUtils
{
	public static Predicate<BPartnerLocation> createLocationFilterFor(@NonNull final IdentifierString locationIdentifier)
	{
		return l -> matches(locationIdentifier, l);
	}

	public static boolean matches(
			@NonNull final IdentifierString locationIdentifier,
			@NonNull final BPartnerLocation location)
	{
		switch (locationIdentifier.getType())
		{
			case EXTERNAL_ID:
				return locationIdentifier.asExternalId().equals(location.getExternalId());
			case GLN:
				return locationIdentifier.asGLN().equals(location.getGln());
			case METASFRESH_ID:
				return locationIdentifier.asMetasfreshId().equals(MetasfreshId.of(location.getId()));
			case VALUE:
				throw new AdempiereException("IdentifierStrings with type=" + Type.VALUE + " are not supported for locations")
						.appendParametersToMessage()
						.setParameter("locationIdentifier", locationIdentifier);
			default:
				throw new AdempiereException("Unexpected type; locationIdentifier=" + locationIdentifier);
		}
	}

	public static Predicate<BPartnerContact> createContactFilterFor(@NonNull final IdentifierString contactIdentifier)
	{
		return c -> matches(contactIdentifier, c);
	}

	private boolean matches(
			@NonNull final IdentifierString contactIdentifier,
			@NonNull final BPartnerContact contact)
	{
		switch (contactIdentifier.getType())
		{
			case EXTERNAL_ID:
				return contactIdentifier.asExternalId().equals(contact.getExternalId());
			case GLN:
				throw new AdempiereException("IdentifierStrings with type=" + Type.GLN + " are not supported for contacts")
						.appendParametersToMessage()
						.setParameter("contactIdentifier", contactIdentifier);
			case METASFRESH_ID:
				return contactIdentifier.asMetasfreshId().equals(MetasfreshId.of(contact.getId()));
			case VALUE:
				return contactIdentifier.asValue().equals(contact.getValue());
			default:
				throw new AdempiereException("Unexpected type; contactIdentifier=" + contactIdentifier);
		}
	}

	public static OrgId retrieveOrgIdOrDefault(@Nullable final String orgCode)
	{
		final OrgId orgId;
		if (isNotBlank(orgCode))
		{
			orgId = Services.get(IOrgDAO.class)
					.retrieveOrgIdBy(OrgQuery.ofValue(orgCode))
					.orElseThrow(() -> MissingResourceException.builder()
							.resourceName("organisation")
							.resourceIdentifier(orgCode).build());
		}
		else
		{
			orgId = Env.getOrgId();
		}
		return orgId;
	}
}
