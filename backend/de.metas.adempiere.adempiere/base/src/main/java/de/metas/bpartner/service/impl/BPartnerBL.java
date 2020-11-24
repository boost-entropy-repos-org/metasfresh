package de.metas.bpartner.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import de.metas.bpartner.BPGroupId;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.ShipmentAllocationBestBeforePolicy;
import de.metas.bpartner.service.BPartnerPrintFormatMap;
import de.metas.bpartner.service.IBPGroupDAO;
import de.metas.bpartner.service.IBPartnerAware;
import de.metas.bpartner.service.IBPartnerBL;
import de.metas.bpartner.service.IBPartnerBL.RetrieveContactRequest.ContactType;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.i18n.Language;
import de.metas.lang.SOTrx;
import de.metas.location.CountryId;
import de.metas.location.ILocationBL;
import de.metas.location.impl.AddressBuilder;
import de.metas.organization.OrgId;
import de.metas.payment.PaymentRule;
import de.metas.payment.paymentterm.PaymentTermId;
import de.metas.user.User;
import de.metas.user.UserId;
import de.metas.user.UserRepository;
import de.metas.util.Check;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.ISysConfigBL;
import org.compiere.model.I_AD_User;
import org.compiere.model.I_C_BP_Group;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_BPartner_QuickInput;
import org.compiere.util.Env;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

@Service
public class BPartnerBL implements IBPartnerBL
{
	/* package */static final String SYSCONFIG_C_BPartner_SOTrx_AllowConsolidateInOut_Override = "C_BPartner.SOTrx_AllowConsolidateInOut_Override";

	private final IBPartnerDAO bpartnersRepo;
	private final UserRepository userRepository;

	public BPartnerBL(@NonNull final UserRepository userRepository)
	{
		this.bpartnersRepo = Services.get(IBPartnerDAO.class);
		this.userRepository = userRepository;
	}

	@Override
	public I_C_BPartner getById(@NonNull final BPartnerId bpartnerId)
	{
		return bpartnersRepo.getById(bpartnerId);
	}

	@Override
	public String getBPartnerValue(final BPartnerId bpartnerId)
	{
		return toBPartnerDisplayName(bpartnerId, I_C_BPartner::getValue);
	}

	@Override
	public String getBPartnerName(final BPartnerId bpartnerId)
	{
		return toBPartnerDisplayName(bpartnerId, I_C_BPartner::getName);
	}

	@Override
	public String getBPartnerValueAndName(final BPartnerId bpartnerId)
	{
		return toBPartnerDisplayName(bpartnerId, bpartner -> bpartner.getValue() + "_" + bpartner.getName());
	}

	private String toBPartnerDisplayName(final BPartnerId bpartnerId, final Function<I_C_BPartner, String> toString)
	{
		if (bpartnerId == null)
		{
			return "?";
		}

		final I_C_BPartner bpartner = getById(bpartnerId);
		if (bpartner == null)
		{
			return "<" + bpartnerId + ">";
		}

		return toString.apply(bpartner);
	}

	@Override
	public String mkFullAddress(
			@NonNull final org.compiere.model.I_C_BPartner bPartner,
			final I_C_BPartner_Location location,
			final I_AD_User user,
			final String trxName)
	{
		final AddressBuilder addressBuilder = AddressBuilder.builder()
				.orgId(OrgId.ofRepoId(bPartner.getAD_Org_ID()))
				.adLanguage(bPartner.getAD_Language())
				.build();
		return addressBuilder.buildBPartnerFullAddressString(bPartner, location, user, trxName);
	}

	@Override
	public I_AD_User retrieveShipContact(final Properties ctx, final int bPartnerId, final String trxName)
	{
		final I_C_BPartner_Location loc = bpartnersRepo.retrieveShipToLocation(ctx, bPartnerId, trxName);

		final int bPartnerLocationId = loc == null ? -1 : loc.getC_BPartner_Location_ID();
		return retrieveUserForLoc(ctx, bPartnerId, bPartnerLocationId, trxName);
	}

	@Override
	public I_AD_User retrieveShipContact(final org.compiere.model.I_C_BPartner bpartner)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(bpartner);
		final int bPartnerId = bpartner.getC_BPartner_ID();
		final String trxName = InterfaceWrapperHelper.getTrxName(bpartner);
		final org.compiere.model.I_AD_User userPO = retrieveShipContact(ctx, bPartnerId, trxName);
		return InterfaceWrapperHelper.create(userPO, I_AD_User.class);
	}

	@Override
	public I_AD_User createDraftContact(final org.compiere.model.I_C_BPartner bpartner)
	{
		I_AD_User contact = InterfaceWrapperHelper.newInstance(I_AD_User.class, bpartner);
		contact.setC_BPartner_ID(bpartner.getC_BPartner_ID());
		contact.setName(bpartner.getName());
		return contact;
	}

	@Override
	public User retrieveContactOrNull(@NonNull final RetrieveContactRequest request)
	{
		final List<I_AD_User> contactRecords = bpartnersRepo.retrieveContacts(
				Env.getCtx(),
				request.getBpartnerId().getRepoId(),
				ITrx.TRXNAME_None);

		final boolean ifNotFoundReturnNull = RetrieveContactRequest.IfNotFound.RETURN_NULL.equals(request.getIfNotFound());
		final boolean onlyActiveContacts = request.isOnlyActive();

		// we will collect the candidates for our return value into these variables
		final Set<User> contactsAtLocation = new TreeSet<>(request.getComparator());
		final Set<User> contactsAtOtherLocations = new TreeSet<>(request.getComparator());
		User defaultContactOfType = null;
		User defaultContact = null;

		for (final I_AD_User contactRecord : contactRecords)
		{
			if (onlyActiveContacts && !contactRecord.isActive())
			{
				continue;
			}

			final User contact = userRepository.ofRecord(contactRecord);
			if (!request.getFilter().test(contact))
			{
				continue;
			}

			final boolean contactHasMatchingBPartnerLocation = request.getBPartnerLocationId() != null
					&& contactRecord.getC_BPartner_Location_ID() == request.getBPartnerLocationId().getRepoId();
			if (contactHasMatchingBPartnerLocation)
			{
				contactsAtLocation.add(contact);
			}
			else
			{
				contactsAtOtherLocations.add(contact);
			}

			if (contactRecord.isDefaultContact())
			{
				defaultContact = contact;
			}
			if (recordMatchesType(contactRecord, request.getContactType()))
			{
				defaultContactOfType = contact;

				if (ifNotFoundReturnNull)
				{
					return defaultContactOfType;
				}
			}
		}

		if (ifNotFoundReturnNull)
		{
			// no user of the given type was found
			return null;
		}

		if (!contactsAtLocation.isEmpty())
		{
			return findBestMatch(contactsAtLocation, defaultContactOfType, defaultContact);
		}
		else if (!contactsAtOtherLocations.isEmpty())
		{
			return findBestMatch(contactsAtOtherLocations, defaultContactOfType, defaultContact);
		}
		return null;
	}

	private boolean recordMatchesType(@NonNull final I_AD_User contactRecord, @Nullable final ContactType contactType)
	{
		if (contactType == null)
		{
			return true;
		}
		switch (contactType)
		{
			case BILL_TO_DEFAULT:
				return contactRecord.isBillToContact_Default();
			case SALES_DEFAULT:
				return contactRecord.isSalesContact_Default();
			case SHIP_TO_DEFAULT:
				return contactRecord.isShipToContact_Default();
			case SUBJECT_MATTER:
				return contactRecord.isSubjectMatterContact();
			default:
				throw new AdempiereException("Unsupporded contactType=" + contactType);
		}
	}

	private User findBestMatch(
			@NonNull final Set<User> contacts,
			@Nullable final User defaultContactOfType,
			@Nullable final User defaultContact)
	{
		Check.assumeNotEmpty(contacts, "Parameter contacts needs to be non-empty");

		if (defaultContactOfType != null && contacts.contains(defaultContactOfType))
		{
			return defaultContactOfType;
		}
		else if (defaultContact != null && contacts.contains(defaultContact))
		{
			return defaultContact;
		}

		return contacts.iterator().next();
	}

	@Override
	public I_AD_User retrieveUserForLoc(final org.compiere.model.I_C_BPartner_Location loc)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(loc);
		final int bPartnerId = loc.getC_BPartner_ID();
		final int bPartnerLocationId = loc.getC_BPartner_Location_ID();
		final String trxName = InterfaceWrapperHelper.getTrxName(loc);

		return retrieveUserForLoc(ctx, bPartnerId, bPartnerLocationId, trxName);
	}

	private I_AD_User retrieveUserForLoc(final Properties ctx, final int bPartnerId, final int bPartnerLocationId, final String trxName)
	{
		final List<I_AD_User> users = bpartnersRepo.retrieveContacts(ctx, bPartnerId, trxName);

		if (bPartnerLocationId > 0)
		{
			for (final I_AD_User user : users)
			{
				if (user.getC_BPartner_Location_ID() == bPartnerLocationId)
				{
					return user;
				}
			}
		}

		return getDefaultBPContact(users);
	}

	/**
	 * Selects the default contact from a list of BPartner users. Returns first user with IsDefaultContact=Y found or first contact.
	 *
	 * @param users
	 * @return default user/contact.
	 */
	private I_AD_User getDefaultBPContact(final List<I_AD_User> users)
	{
		if (users == null || users.isEmpty())
		{
			return null;
		}

		for (final I_AD_User user : users)
		{
			if (user.isDefaultContact())
			{
				return user;
			}
		}
		return users.get(0);
	}

	@Override
	public void updateAllAddresses(@NonNull final I_C_BPartner bpartner)
	{
		final List<I_C_BPartner_Location> bpLocations = bpartnersRepo.retrieveBPartnerLocations(bpartner);
		for (final I_C_BPartner_Location bpLocation : bpLocations)
		{
			updateAddressNoSave(bpLocation, bpartner);
			bpartnersRepo.save(bpLocation);
		}
	}

	@Override
	public void setAddress(@NonNull final I_C_BPartner_Location bpLocation)
	{
		final I_C_BPartner bpartner = bpartnersRepo.getById(bpLocation.getC_BPartner_ID());
		updateAddressNoSave(bpLocation, bpartner);
	}

	private void updateAddressNoSave(final I_C_BPartner_Location bpLocation, final I_C_BPartner bpartner)
	{
		final ILocationBL locationBL = Services.get(ILocationBL.class);

		final String address = locationBL.mkAddress(
				bpLocation.getC_Location(),
				bpartner,
				"",  // bPartnerBlock
				"" // userBlock
		);

		bpLocation.setAddress(address);
	}

	@Override
	public boolean isAllowConsolidateInOutEffective(@NonNull final BPartnerId bpartnerId, @NonNull final SOTrx soTrx)
	{
		I_C_BPartner bpartner = getById(bpartnerId);
		return isAllowConsolidateInOutEffective(bpartner, soTrx);
	}

	@Override
	public boolean isAllowConsolidateInOutEffective(
			@NonNull final org.compiere.model.I_C_BPartner partner,
			@NonNull final SOTrx soTrx)
	{
		final I_C_BPartner partnerToUse = InterfaceWrapperHelper.create(partner, de.metas.interfaces.I_C_BPartner.class);
		final boolean partnerAllowConsolidateInOut = partnerToUse.isAllowConsolidateInOut();
		if (partnerAllowConsolidateInOut)
		{
			return true;
		}

		//
		// 07973: Attempt to override SO shipment consolidation if configured
		if (soTrx.isSales())
		{
			final boolean allowConsolidateInOutOverrideDefault = false; // default=false (preserve existing logic)
			final boolean allowConsolidateInOutOverride = Services.get(ISysConfigBL.class).getBooleanValue(
					SYSCONFIG_C_BPartner_SOTrx_AllowConsolidateInOut_Override,
					allowConsolidateInOutOverrideDefault);
			return allowConsolidateInOutOverride;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Optional<Language> getLanguage(@NonNull final BPartnerId bpartnerId)
	{
		final I_C_BPartner bpartner = bpartnersRepo.getById(bpartnerId);
		return bpartner != null
				? getLanguage(bpartner)
				: Optional.empty();
	}

	@Override
	public Optional<BPartnerId> getBPartnerIdForModel(@Nullable final Object model)
	{
		final IBPartnerAware bpartnerAware = InterfaceWrapperHelper.asColumnReferenceAwareOrNull(model, IBPartnerAware.class);
		return bpartnerAware != null
				? BPartnerId.optionalOfRepoId(bpartnerAware.getC_BPartner_ID())
				: Optional.empty();
	}

	@Override
	public Optional<Language> getLanguageForModel(@Nullable final Object model)
	{
		// 09527 get the most suitable language:
		return getBPartnerIdForModel(model)
				.map(bpartnersRepo::getById)
				.flatMap(this::getLanguage);
	}

	@Override
	public Optional<Language> getLanguage(@NonNull final I_C_BPartner bpartner)
	{
		final String adLanguage = bpartner.getAD_Language();
		return Check.isNotBlank(adLanguage)
				? Optional.ofNullable(Language.getLanguage(adLanguage))
				: Optional.empty();
	}

	@Override
	public I_C_BPartner createFromTemplate(final I_C_BPartner_QuickInput template)
	{
		Check.assumeNotNull(template, "Parameter template is not null");
		Check.assume(!template.isProcessed(), "{} not already processed", template);
		Check.assume(template.getC_Location_ID() > 0, "{} > 0", template); // just to make sure&explicit

		Services.get(ITrxManager.class).assertThreadInheritedTrxExists();

		//
		// BPartner
		final I_C_BPartner bpartner = InterfaceWrapperHelper.create(Env.getCtx(), I_C_BPartner.class, ITrx.TRXNAME_ThreadInherited);
		bpartner.setAD_Org_ID(template.getAD_Org_ID());
		// bpartner.setValue(Value);
		bpartner.setName(extractName(template));
		bpartner.setName2(template.getName2());
		bpartner.setIsCompany(template.isCompany());
		bpartner.setCompanyName(template.getCompanyname());
		bpartner.setC_BP_Group_ID(template.getC_BP_Group_ID());
		bpartner.setAD_Language(template.getAD_Language());
		// Customer
		bpartner.setIsCustomer(template.isCustomer());
		bpartner.setC_PaymentTerm_ID(template.getC_PaymentTerm_ID());
		bpartner.setM_PricingSystem_ID(template.getM_PricingSystem_ID());
		// Vendor
		bpartner.setIsVendor(template.isVendor());
		bpartner.setPO_PaymentTerm_ID(template.getPO_PaymentTerm_ID());
		bpartner.setPO_PricingSystem_ID(template.getPO_PricingSystem_ID());
		//
		bpartnersRepo.save(bpartner);

		template.setC_BPartner(bpartner);

		//
		// BPartner location
		final I_C_BPartner_Location bpLocation = InterfaceWrapperHelper.newInstance(I_C_BPartner_Location.class, bpartner);
		bpLocation.setC_BPartner_ID(bpartner.getC_BPartner_ID());
		bpLocation.setC_Location_ID(template.getC_Location_ID());
		bpLocation.setIsBillTo(true);
		bpLocation.setIsBillToDefault(true);
		bpLocation.setIsShipTo(true);
		bpLocation.setIsShipToDefault(true);
		bpartnersRepo.save(bpLocation);

		template.setC_BPartner_Location(bpLocation);

		final boolean isContactInfoProvided = !Check.isEmpty(template.getFirstname()) || !Check.isEmpty(template.getLastname());

		if (isContactInfoProvided)
		{
			//
			// BPartner contact
			final I_AD_User bpContact = InterfaceWrapperHelper.newInstance(I_AD_User.class, bpartner);
			bpContact.setC_BPartner(bpartner);
			bpContact.setC_Greeting(template.getC_Greeting());
			bpContact.setFirstname(template.getFirstname());
			bpContact.setLastname(template.getLastname());
			bpContact.setPhone(template.getPhone());
			bpContact.setEMail(template.getEMail());
			bpContact.setIsNewsletter(template.isNewsletter());
			bpContact.setC_BPartner_Location(bpLocation);
			if (template.isCustomer())
			{
				bpContact.setIsSalesContact(true);
				bpContact.setIsSalesContact_Default(true);
			}
			if (template.isVendor())
			{
				bpContact.setIsPurchaseContact(true);
				bpContact.setIsPurchaseContact_Default(true);
			}
			bpartnersRepo.save(bpContact);

			template.setAD_User(bpContact);
		}

		template.setProcessed(true);
		save(template);

		return bpartner;
	}

	private void save(final I_C_BPartner_QuickInput template)
	{
		InterfaceWrapperHelper.saveRecord(template);
	}

	private final String extractName(final I_C_BPartner_QuickInput template)
	{
		if (template.isCompany())
		{
			return template.getCompanyname();
		}
		else
		{
			final String firstname = Strings.emptyToNull(template.getFirstname());
			final String lastname = Strings.emptyToNull(template.getLastname());
			return Joiner.on(" ")
					.skipNulls()
					.join(firstname, lastname);
		}
	}

	@Override
	public int getDiscountSchemaId(@NonNull final BPartnerId bpartnerId, final SOTrx soTrx)
	{
		final I_C_BPartner bpartner = getById(bpartnerId);

		return getDiscountSchemaId(bpartner, soTrx);
	}

	@Override
	public int getDiscountSchemaId(@NonNull final I_C_BPartner bpartner, final SOTrx soTrx)
	{
		{
			final int discountSchemaId;
			if (soTrx.isSales())
			{
				discountSchemaId = bpartner.getM_DiscountSchema_ID();
			}
			else
			{
				discountSchemaId = bpartner.getPO_DiscountSchema_ID();
			}
			if (discountSchemaId > 0)
			{
				return discountSchemaId; // we are done
			}
		}

		// didn't get the schema yet; now we try to get the discount schema from the C_BPartner's C_BP_Group
		final I_C_BP_Group bpGroup = bpartner.getC_BP_Group();
		if (bpGroup != null && bpGroup.getC_BP_Group_ID() > 0)
		{
			final int groupDiscountSchemaId;
			if (soTrx.isSales())
			{
				groupDiscountSchemaId = bpGroup.getM_DiscountSchema_ID();
			}
			else
			{
				groupDiscountSchemaId = bpGroup.getPO_DiscountSchema_ID();
			}
			if (groupDiscountSchemaId > 0)
			{
				return groupDiscountSchemaId; // we are done
			}
		}

		return -1;
	}

	@Override
	public String getAddressStringByBPartnerLocationId(final BPartnerLocationId bpartnerLocationId)
	{
		if (bpartnerLocationId == null)
		{
			return "?";
		}

		final I_C_BPartner_Location bpLocation = bpartnersRepo.getBPartnerLocationById(bpartnerLocationId);
		return bpLocation != null ? bpLocation.getAddress() : "<" + bpartnerLocationId.getRepoId() + ">";
	}

	@Override
	public UserId getSalesRepIdOrNull(final BPartnerId bpartnerId)
	{
		final I_C_BPartner bpartnerRecord = getById(bpartnerId);
		if (InterfaceWrapperHelper.isNull(bpartnerRecord, I_C_BPartner.COLUMNNAME_SalesRep_ID))
		{
			return null;
		}

		final int salesRepRecordId = bpartnerRecord.getSalesRep_ID();
		return UserId.ofRepoIdOrNull(salesRepRecordId);
	}

	@Override
	public BPartnerId getBPartnerSalesRepId(final BPartnerId bPartnerId)
	{
		final int salesRepRecordId = getById(bPartnerId).getC_BPartner_SalesRep_ID();

		return BPartnerId.ofRepoIdOrNull(salesRepRecordId);
	}

	@Override
	public UserId setSalesRepId(
			@NonNull final BPartnerId bpartnerId,
			@Nullable final UserId salesRepId)
	{
		final I_C_BPartner bpartnerRecord = bpartnersRepo.getById(bpartnerId);

		final UserId oldSalesRepId = UserId.ofRepoIdOrNull(bpartnerRecord.getSalesRep_ID());
		bpartnerRecord.setSalesRep_ID(UserId.toRepoId(salesRepId));
		bpartnersRepo.save(bpartnerRecord);

		return oldSalesRepId;
	}

	@Override
	public CountryId getBPartnerLocationCountryId(@NonNull final BPartnerLocationId bpLocationId)
	{
		return bpartnersRepo.retrieveBPartnerLocationCountryId(bpLocationId);
	}

	@Override
	public int getFreightCostIdByBPartnerId(@NonNull final BPartnerId bpartnerId)
	{
		final I_C_BPartner bpartner = bpartnersRepo.getById(bpartnerId);
		int freightCostId = bpartner.getM_FreightCost_ID();
		if (freightCostId > 0)
		{
			return freightCostId;
		}

		final IBPGroupDAO bpGroupsRepo = Services.get(IBPGroupDAO.class);
		final I_C_BP_Group bpGroup = bpGroupsRepo.getByBPartnerId(bpartnerId);
		freightCostId = bpGroup.getM_FreightCost_ID();
		return freightCostId;
	}

	@Override
	public ShipmentAllocationBestBeforePolicy getBestBeforePolicy(@NonNull final BPartnerId bpartnerId)
	{
		final I_C_BPartner bpartner = getById(bpartnerId);
		final ShipmentAllocationBestBeforePolicy bestBeforePolicy = ShipmentAllocationBestBeforePolicy.ofNullableCode(bpartner.getShipmentAllocation_BestBefore_Policy());
		return bestBeforePolicy != null ? bestBeforePolicy : ShipmentAllocationBestBeforePolicy.Expiring_First;
	}

	@Override
	public Optional<PaymentTermId> getPaymentTermIdForBPartner(@NonNull final BPartnerId bpartnerId, @NonNull final SOTrx soTrx)
	{
		final I_C_BPartner bpartner = bpartnersRepo.getById(bpartnerId);

		return soTrx.isSales()
				? PaymentTermId.optionalOfRepoId(bpartner.getC_PaymentTerm_ID())
				: PaymentTermId.optionalOfRepoId(bpartner.getPO_PaymentTerm_ID());
	}

	@Override
	public Optional<PaymentRule> getPaymentRuleForBPartner(@NonNull final BPartnerId bpartnerId)
	{
		final I_C_BPartner bpartner = bpartnersRepo.getById(bpartnerId);
		final Optional<PaymentRule> bpartnerPaymentRule = PaymentRule.optionalOfCode(bpartner.getPaymentRule());
		if (bpartnerPaymentRule.isPresent())
		{
			return bpartnerPaymentRule;
		}
		//
		// No payment rule in BP. Fallback to group.
		final BPGroupId bpGroupId = BPGroupId.ofRepoId(bpartner.getC_BP_Group_ID());
		final IBPGroupDAO bpGroupDAO = Services.get(IBPGroupDAO.class);
		final I_C_BP_Group bpGroup = bpGroupDAO.getById(bpGroupId);
		return PaymentRule.optionalOfCode(bpGroup.getPaymentRule());
	}

	@Override
	public BPartnerPrintFormatMap getPrintFormats(final @NonNull BPartnerId bpartnerId)
	{
		return bpartnersRepo.getPrintFormats(bpartnerId);
	}
}
