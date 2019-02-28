package de.metas.order.impl;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.translate;

/*
 * #%L
 * de.metas.swat.base
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.service.IOrgDAO;
import org.adempiere.service.ISysConfigBL;
import org.adempiere.service.OrgId;
import org.adempiere.uom.UomId;
import org.adempiere.uom.api.IUOMConversionBL;
import org.adempiere.uom.api.UOMConversionContext;
import org.compiere.model.I_AD_Org;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_Tax;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.MTax;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.eevolution.api.IProductBOMBL;
import org.slf4j.Logger;

import de.metas.adempiere.model.I_M_Product;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.BPartnerLocationId;
import de.metas.bpartner.service.IBPartnerDAO;
import de.metas.currency.CurrencyPrecision;
import de.metas.document.IDocTypeBL;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.i18n.IMsgBL;
import de.metas.interfaces.I_C_OrderLine;
import de.metas.logging.LogManager;
import de.metas.money.CurrencyId;
import de.metas.money.Money;
import de.metas.order.IOrderBL;
import de.metas.order.IOrderDAO;
import de.metas.order.IOrderLineBL;
import de.metas.order.OrderAndLineId;
import de.metas.order.OrderLinePriceUpdateRequest;
import de.metas.order.PriceAndDiscount;
import de.metas.pricing.IPricingResult;
import de.metas.pricing.limit.PriceLimitRuleResult;
import de.metas.pricing.service.IPriceListBL;
import de.metas.pricing.service.IPriceListDAO;
import de.metas.product.IProductBL;
import de.metas.product.IProductDAO;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.shipping.ShipperId;
import de.metas.tax.api.ITaxBL;
import de.metas.tax.api.TaxCategoryId;
import de.metas.util.Check;
import de.metas.util.GuavaCollectors;
import de.metas.util.Services;
import lombok.NonNull;

public class OrderLineBL implements IOrderLineBL
{
	private static final Logger logger = LogManager.getLogger(OrderLineBL.class);

	public static final String SYSCONFIG_CountryAttribute = "de.metas.swat.CountryAttribute";
	private static final String MSG_COUNTER_DOC_MISSING_MAPPED_PRODUCT = "de.metas.order.CounterDocMissingMappedProduct";

	private static final String SYSCONFIG_SetBOMDescription = "de.metas.order.sales.line.SetBOMDescription";

	private I_C_UOM getUOM(final org.compiere.model.I_C_OrderLine orderLine)
	{
		final I_C_UOM uom = orderLine.getC_UOM();
		if (uom != null)
		{
			return uom;
		}

		// fallback to stocking UOM
		return getStockingUOM(orderLine);
	}

	private I_C_UOM getStockingUOM(final org.compiere.model.I_C_OrderLine orderLine)
	{
		final IProductBL productBL = Services.get(IProductBL.class);
		return productBL.getStockingUOM(orderLine.getM_Product_ID());
	}

	@Override
	public Quantity getQtyEntered(final org.compiere.model.I_C_OrderLine orderLine)
	{
		final BigDecimal qty = orderLine.getQtyEntered();
		final I_C_UOM uom = getUOM(orderLine);
		return Quantity.of(qty, uom);
	}

	@Override
	public Quantity getQtyOrdered(@NonNull final OrderAndLineId orderAndLineId)
	{
		final IOrderDAO ordersRepo = Services.get(IOrderDAO.class);
		final I_C_OrderLine orderLine = ordersRepo.getOrderLineById(orderAndLineId);
		Check.assumeNotNull(orderLine, "orderLine is not null");

		final BigDecimal qtyOrdered = orderLine.getQtyOrdered();
		final I_C_UOM uom = getStockingUOM(orderLine);

		return Quantity.of(qtyOrdered, uom);
	}

	@Override
	public Quantity getQtyToDeliver(@NonNull final OrderAndLineId orderAndLineId)
	{
		final IOrderDAO ordersRepo = Services.get(IOrderDAO.class);
		final I_C_OrderLine orderLine = ordersRepo.getOrderLineById(orderAndLineId);
		return getQtyToDeliver(orderLine);
	}

	private Quantity getQtyToDeliver(@NonNull final I_C_OrderLine orderLine)
	{
		final BigDecimal qtyOrdered = orderLine.getQtyOrdered();
		final BigDecimal qtyDelivered = orderLine.getQtyDelivered();
		BigDecimal qtyToDeliver = qtyOrdered.subtract(qtyDelivered);

		final I_C_UOM uom = getStockingUOM(orderLine);

		return Quantity.of(qtyToDeliver, uom);
	}

	@Override
	public Map<OrderAndLineId, Quantity> getQtyToDeliver(@NonNull final Collection<OrderAndLineId> orderAndLineIds)
	{
		final IOrderDAO ordersRepo = Services.get(IOrderDAO.class);
		return ordersRepo.getOrderLinesByIds(orderAndLineIds)
				.entrySet()
				.stream()
				.map(GuavaCollectors.mapValue(this::getQtyToDeliver))
				.collect(GuavaCollectors.toImmutableMap());
	}

	@Override
	public void setTaxAmtInfo(final I_C_OrderLine ol)
	{
		final int taxId = ol.getC_Tax_ID();
		if (taxId <= 0)
		{
			ol.setTaxAmtInfo(BigDecimal.ZERO);
			return;
		}

		final boolean taxIncluded = isTaxIncluded(ol);
		final BigDecimal lineAmout = ol.getLineNetAmt();
		final int taxPrecision = getPrecision(ol);

		final I_C_Tax tax = MTax.get(Env.getCtx(), taxId);

		final ITaxBL taxBL = Services.get(ITaxBL.class);
		final BigDecimal taxAmtInfo = taxBL.calculateTax(tax, lineAmout, taxIncluded, taxPrecision);
		ol.setTaxAmtInfo(taxAmtInfo);
	}

	@Override
	public void setShipper(final I_C_OrderLine ol)
	{
		final org.compiere.model.I_C_Order order = ol.getC_Order();

		final int orderShipperId = order.getM_Shipper_ID();
		if (orderShipperId > 0)
		{
			logger.debug("Setting M_Shipper_ID={} from {}", orderShipperId, order);
			ol.setM_Shipper_ID(orderShipperId);
		}
		else
		{
			logger.debug("Looking for M_Shipper_ID via ship-to-bpartner of {}", order);

			final BPartnerId bpartnerId = BPartnerId.ofRepoIdOrNull(order.getC_BPartner_ID());
			if (bpartnerId == null)
			{
				logger.warn("{} has no ship-to-bpartner", order);
				return;
			}

			final ShipperId shipperId = Services.get(IBPartnerDAO.class).getShipperId(bpartnerId);
			if (shipperId == null)
			{
				// task 07034: nothing to do
				return;
			}

			logger.debug("Setting M_Shipper_ID={} from ship-to-bpartner", shipperId);
			ol.setM_Shipper_ID(shipperId.getRepoId());
		}
	}

	@Override
	public BigDecimal calculatePriceEnteredFromPriceActualAndDiscount(final BigDecimal priceActual, final BigDecimal discount, final int precision)
	{
		return PriceAndDiscount.calculatePriceEnteredFromPriceActualAndDiscount(priceActual, discount, precision);
	}

	@Override
	public TaxCategoryId getTaxCategoryId(final org.compiere.model.I_C_OrderLine orderLine)
	{
		// In case we have a charge, use the tax category from charge
		if (orderLine.getC_Charge_ID() > 0)
		{
			return TaxCategoryId.ofRepoId(orderLine.getC_Charge().getC_TaxCategory_ID());
		}

		final OrderLinePriceUpdateRequest request = OrderLinePriceUpdateRequest.ofOrderLine(orderLine);

		return OrderLinePriceCalculator.builder()
				.request(request)
				.orderLineBL(this)
				.build()
				.computeTaxCategoryId();
	}

	@Override
	public I_C_OrderLine createOrderLine(@NonNull final org.compiere.model.I_C_Order order)
	{
		final I_C_OrderLine ol = newInstance(I_C_OrderLine.class);
		ol.setC_Order(order);
		setOrder(ol, order);

		if (order.isSOTrx() && order.isDropShip())
		{
			final int bpartnerId = order.getDropShip_BPartner_ID() > 0 ? order.getDropShip_BPartner_ID() : order.getC_BPartner_ID();
			ol.setC_BPartner_ID(bpartnerId);

			final BPartnerLocationId deliveryLocationId = Services.get(IOrderBL.class).getShipToLocationId(order);
			ol.setC_BPartner_Location_ID(BPartnerLocationId.toRepoId(deliveryLocationId));

			final int contactId = order.getDropShip_User_ID() > 0 ? order.getDropShip_User_ID() : order.getAD_User_ID();
			ol.setAD_User_ID(contactId);
		}

		return ol;
	}

	@Override
	public void setOrder(final org.compiere.model.I_C_OrderLine ol, final org.compiere.model.I_C_Order order)
	{
		ol.setAD_Org_ID(order.getAD_Org_ID());
		final boolean isDropShip = order.isDropShip();
		final int C_BPartner_ID = isDropShip && order.getDropShip_BPartner_ID() > 0 ? order.getDropShip_BPartner_ID() : order.getC_BPartner_ID();
		ol.setC_BPartner_ID(C_BPartner_ID);

		final int C_BPartner_Location_ID = isDropShip && order.getDropShip_Location_ID() > 0 ? order.getDropShip_Location_ID() : order.getC_BPartner_Location_ID();
		ol.setC_BPartner_Location_ID(C_BPartner_Location_ID);

		// metas: begin: copy AD_User_ID
		final de.metas.interfaces.I_C_OrderLine oline = InterfaceWrapperHelper.create(ol, de.metas.interfaces.I_C_OrderLine.class);
		final int AD_User_ID = isDropShip && order.getDropShip_User_ID() > 0 ? order.getDropShip_User_ID() : order.getAD_User_ID();
		oline.setAD_User_ID(AD_User_ID);
		// metas: end

		oline.setM_PriceList_Version_ID(0); // the current PLV might be add or'd with the new order's PL.

		ol.setM_Warehouse_ID(order.getM_Warehouse_ID());
		ol.setDateOrdered(order.getDateOrdered());
		ol.setDatePromised(order.getDatePromised());
		ol.setC_Currency_ID(order.getC_Currency_ID());

		// Don't set Activity, etc as they are overwrites
	}

	@Override
	public <T extends I_C_OrderLine> T createOrderLine(org.compiere.model.I_C_Order order, Class<T> orderLineClass)
	{
		final I_C_OrderLine orderLine = createOrderLine(order);
		return InterfaceWrapperHelper.create(orderLine, orderLineClass);
	}

	@Override
	public void updateLineNetAmt(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		updateLineNetAmt(orderLine, getQtyEntered(orderLine));
	}

	void updateLineNetAmt(
			@NonNull final org.compiere.model.I_C_OrderLine ol,
			@NonNull final Quantity qty)
	{
		final Quantity qtyInPriceUOM = convertToPriceUOM(qty, ol);

		final I_C_Order order = ol.getC_Order();
		final int priceListId = order.getM_PriceList_ID();
		final CurrencyPrecision precision = Services.get(IPriceListBL.class).getPricePrecision(priceListId);

		BigDecimal lineNetAmt = qtyInPriceUOM.getAsBigDecimal().multiply(ol.getPriceActual());
		lineNetAmt = precision.roundIfNeeded(lineNetAmt);

		logger.debug("Setting LineNetAmt={} to {}", lineNetAmt, ol);
		ol.setLineNetAmt(lineNetAmt);
	}

	@Override
	public IPricingResult computePrices(final OrderLinePriceUpdateRequest request)
	{
		return OrderLinePriceCalculator.builder()
				.request(request)
				.orderLineBL(this)
				.build()
				.computePrices();
	}

	@Override
	public void updatePrices(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		updatePrices(OrderLinePriceUpdateRequest.ofOrderLine(orderLine));
	}

	@Override
	public void updatePrices(@NonNull final OrderLinePriceUpdateRequest request)
	{
		OrderLinePriceCalculator.builder()
				.request(request)
				.orderLineBL(this)
				.build()
				.updateOrderLine();
	}

	@Override
	public void updateQtyReserved(@Nullable final I_C_OrderLine orderLine)
	{
		if (orderLine == null)
		{
			logger.debug("Given orderLine is NULL; returning");
			return; // not our business
		}

		// make two simple checks that work without loading additional stuff
		if (orderLine.getM_Product_ID() <= 0)
		{
			logger.debug("Given orderLine {} has M_Product_ID<=0; setting QtyReserved=0.", orderLine);
			orderLine.setQtyReserved(BigDecimal.ZERO);
			return;
		}
		if (orderLine.getQtyOrdered().signum() <= 0)
		{
			logger.debug("Given orderLine {} has QtyOrdered<=0; setting QtyReserved=0.", orderLine);
			orderLine.setQtyReserved(BigDecimal.ZERO);
			return;
		}

		final IDocTypeBL docTypeBL = Services.get(IDocTypeBL.class);
		final IDocumentBL docActionBL = Services.get(IDocumentBL.class);

		final I_C_Order order = orderLine.getC_Order();

		if (!docActionBL.isDocumentStatusOneOf(order,
				IDocument.STATUS_InProgress, IDocument.STATUS_Completed, IDocument.STATUS_Closed))
		{
			logger.debug("C_Order {} of given orderLine {} has DocStatus {}; setting QtyReserved=0.",
					new Object[] { order, orderLine, order.getDocStatus() });
			orderLine.setQtyReserved(BigDecimal.ZERO);
			return;
		}
		if (order.getC_DocType_ID() > 0 && docTypeBL.isProposal(order.getC_DocType()))
		{
			logger.debug("C_Order {} of given orderLine {} has C_DocType {} which is a proposal; setting QtyReserved=0.",
					new Object[] { order, orderLine, order.getC_DocType() });
			orderLine.setQtyReserved(BigDecimal.ZERO);
			return;
		}

		if (!orderLine.getM_Product().isStocked())
		{
			logger.debug("Given orderLine {} has M_Product {} which is not stocked; setting QtyReserved=0.",
					new Object[] { orderLine, orderLine.getM_Product() });
			orderLine.setQtyReserved(BigDecimal.ZERO);
			return;
		}

		final BigDecimal qtyReservedRaw = orderLine.getQtyOrdered().subtract(orderLine.getQtyDelivered());
		final BigDecimal qtyReserved = BigDecimal.ZERO.max(qtyReservedRaw); // not less than zero

		logger.debug("Given orderLine {} has QtyOrdered={} and QtyDelivered={}; setting QtyReserved={}.",
				new Object[] { orderLine, orderLine.getQtyOrdered(), orderLine.getQtyDelivered(), qtyReserved });
		orderLine.setQtyReserved(qtyReserved);
	}

	@Override
	public void updatePriceActual(final I_C_OrderLine orderLine, final CurrencyPrecision precision)
	{
		final BigDecimal priceActual = PriceAndDiscount.of(orderLine, precision)
				.updatePriceActual()
				.getPriceActual();
		orderLine.setPriceActual(priceActual);
	}

	@Override
	public void setProductId(
			@NonNull final org.compiere.model.I_C_OrderLine orderLine,
			@NonNull final ProductId productId,
			boolean setUOM)
	{
		orderLine.setM_Product_ID(productId.getRepoId());
		if (setUOM)
		{
			final UomId uomId = Services.get(IProductBL.class).getStockingUOMId(productId);
			orderLine.setC_UOM_ID(uomId.getRepoId());
		}
		
		orderLine.setM_AttributeSetInstance_ID(AttributeSetInstanceId.NONE.getRepoId());
	}	// setM_Product_ID

	@Override
	public I_M_PriceList_Version getPriceListVersion(I_C_OrderLine orderLine)
	{
		final Properties ctx = InterfaceWrapperHelper.getCtx(orderLine);
		final String trxName = InterfaceWrapperHelper.getTrxName(orderLine);

		if (orderLine.getM_PriceList_Version_ID() > 0)
		{
			return InterfaceWrapperHelper.create(ctx, orderLine.getM_PriceList_Version_ID(), I_M_PriceList_Version.class, trxName);
		}
		else
		{
			// If the line doesn't have a pricelist version, take the one from order.
			final I_C_Order order = orderLine.getC_Order();

			final Boolean processedPLVFiltering = null; // task 09533: the user doesn't know about PLV's processed flag, so we can't filter by it
			return Services.get(IPriceListDAO.class).retrievePriceListVersionOrNull(
					order.getM_PriceList(),
					getPriceDate(orderLine, order),
					processedPLVFiltering);
		}
	}

	/**
	 * task 07080
	 *
	 * @param orderLine
	 * @param order
	 * @return
	 */
	static LocalDate getPriceDate(
			final org.compiere.model.I_C_OrderLine orderLine,
			final org.compiere.model.I_C_Order order)
	{
		LocalDate date = TimeUtil.asLocalDate(orderLine.getDatePromised());
		// if null, then get date promised from order
		if (date == null)
		{
			date = TimeUtil.asLocalDate(order.getDatePromised());
		}
		// still null, then get date ordered from order line
		if (date == null)
		{
			date = TimeUtil.asLocalDate(orderLine.getDateOrdered());
		}
		// still null, then get date ordered from order
		if (date == null)
		{
			date = TimeUtil.asLocalDate(order.getDateOrdered());
		}
		return date;
	}

	@Override
	public BigDecimal convertQtyEnteredToPriceUOM(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		final Quantity qtyEntered = getQtyEntered(orderLine);
		return convertToPriceUOM(qtyEntered, orderLine).getAsBigDecimal();
	}

	@Override
	public BigDecimal convertQtyEnteredToInternalUOM(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
		final Properties ctx = InterfaceWrapperHelper.getCtx(orderLine);

		final BigDecimal qtyEntered = orderLine.getQtyEntered();

		ProductId productId = ProductId.ofRepoIdOrNull(orderLine.getM_Product_ID());
		final I_C_UOM uom = orderLine.getC_UOM();
		if (productId == null || uom == null)
		{
			return qtyEntered;
		}

		final BigDecimal qtyOrdered = uomConversionBL.convertToProductUOM(ctx, productId, uom, qtyEntered);
		return qtyOrdered;
	}

	Quantity convertToPriceUOM(@NonNull final Quantity qtyEntered, @NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		final I_C_OrderLine orderLineExt = InterfaceWrapperHelper.create(orderLine, I_C_OrderLine.class);
		final I_C_UOM priceUOM = orderLineExt.getPrice_UOM();
		if (priceUOM == null || priceUOM.getC_UOM_ID() <= 0)
		{
			return qtyEntered;
		}

		final int qtyUOMId = qtyEntered.getUOMId();
		if (qtyUOMId == priceUOM.getC_UOM_ID())
		{
			return qtyEntered;
		}

		final IUOMConversionBL uomConversionBL = Services.get(IUOMConversionBL.class);
		final UOMConversionContext conversionCtx = UOMConversionContext.of(orderLine.getM_Product_ID());
		return uomConversionBL.convertQuantityTo(qtyEntered, conversionCtx, priceUOM);
	}

	@Override
	public boolean isTaxIncluded(final org.compiere.model.I_C_OrderLine orderLine)
	{
		Check.assumeNotNull(orderLine, "orderLine not null");

		final I_C_Tax tax = orderLine.getC_Tax();

		final org.compiere.model.I_C_Order order = orderLine.getC_Order();
		return Services.get(IOrderBL.class).isTaxIncluded(order, tax);
	}

	@Override
	public int getPrecision(final org.compiere.model.I_C_OrderLine orderLine)
	{
		final org.compiere.model.I_C_Order order = orderLine.getC_Order();
		return Services.get(IOrderBL.class).getPrecision(order);
	}

	@Override
	public boolean isAllowedCounterLineCopy(final org.compiere.model.I_C_OrderLine fromLine)
	{
		final de.metas.interfaces.I_C_OrderLine ol = InterfaceWrapperHelper.create(fromLine, de.metas.interfaces.I_C_OrderLine.class);

		if (ol.isPackagingMaterial())
		{
			// DO not copy the line if it's packing material. The packaging lines will be created later
			return false;
		}

		return true;
	}

	@Override
	public void copyOrderLineCounter(final org.compiere.model.I_C_OrderLine line, final org.compiere.model.I_C_OrderLine fromLine)
	{
		final de.metas.interfaces.I_C_OrderLine ol = InterfaceWrapperHelper.create(fromLine, de.metas.interfaces.I_C_OrderLine.class);

		if (ol.isPackagingMaterial())
		{
			// do nothing! the packaging lines will be created later
			return;
		}

		// link the line with the one from the counter document
		line.setRef_OrderLine_ID(fromLine.getC_OrderLine_ID());

		final ProductId lineProductId = ProductId.ofRepoIdOrNull(line.getM_Product_ID());
		if (lineProductId != null)      // task 09700
		{
			final IProductDAO productsRepo = Services.get(IProductDAO.class);
			final IOrgDAO orgsRepo = Services.get(IOrgDAO.class);
			final IMsgBL msgBL = Services.get(IMsgBL.class);

			final I_AD_Org lineOrg = orgsRepo.getById(line.getAD_Org_ID());
			final I_M_Product lineProduct = productsRepo.getById(lineProductId, I_M_Product.class);

			final OrgId productOrgId = OrgId.ofRepoIdOrAny(lineProduct.getAD_Org_ID());
			if (!productOrgId.isAny())
			{
				// task 09700 the product from the original order is org specific, so we need to substitute it with the product from the counter-org.
				final ProductId counterProductId = productsRepo.retrieveMappedProductIdOrNull(lineProductId, productOrgId);
				if (counterProductId == null)
				{
					final I_AD_Org productOrg = orgsRepo.getById(productOrgId);
					final String msg = msgBL.getMsg(InterfaceWrapperHelper.getCtx(line),
							MSG_COUNTER_DOC_MISSING_MAPPED_PRODUCT,
							new Object[] { lineProduct.getValue(), productOrg.getName(), lineOrg.getName() });
					throw new AdempiereException(msg);
				}
				line.setM_Product_ID(counterProductId.getRepoId());
			}
		}
	}

	@Override
	public Money getCostPrice(final org.compiere.model.I_C_OrderLine orderLine)
	{
		final CurrencyId currencyId = CurrencyId.ofRepoId(orderLine.getC_Currency_ID());

		final BigDecimal poCostPrice = orderLine.getPriceCost();
		if (poCostPrice != null && poCostPrice.signum() != 0)
		{
			return Money.of(poCostPrice, currencyId);
		}

		BigDecimal priceActual = orderLine.getPriceActual();
		if (!isTaxIncluded(orderLine))
		{
			return Money.of(priceActual, currencyId);
		}

		final int taxId = orderLine.getC_Tax_ID();
		if (taxId <= 0)
		{
			// shall not happen
			return Money.of(priceActual, currencyId);
		}

		final MTax tax = MTax.get(Env.getCtx(), taxId);
		if (tax.isZeroTax())
		{
			return Money.of(priceActual, currencyId);
		}

		final int stdPrecision = getPrecision(orderLine);
		final BigDecimal taxAmt = Services.get(ITaxBL.class).calculateTax(tax, priceActual, true/* taxIncluded */, stdPrecision);
		final BigDecimal priceActualWithoutTax = priceActual.subtract(taxAmt);
		return Money.of(priceActualWithoutTax, currencyId);
	}

	@Override
	public int getC_PaymentTerm_ID(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		int paymentTermOverrideId = orderLine.getC_PaymentTerm_Override_ID();
		return paymentTermOverrideId > 0 ? paymentTermOverrideId : orderLine.getC_Order().getC_PaymentTerm_ID();
	}

	@Override
	public PriceLimitRuleResult computePriceLimit(@NonNull final org.compiere.model.I_C_OrderLine orderLine)
	{
		return OrderLinePriceCalculator.builder()
				.request(OrderLinePriceUpdateRequest.ofOrderLine(orderLine))
				.orderLineBL(this)
				.build()
				.computePriceLimit();
	}

	/**
	 *
	 * @task https://github.com/metasfresh/metasfresh/issues/4535
	 */
	@Override
	public void updateProductDescriptionFromProductBOMIfConfigured(final org.compiere.model.I_C_OrderLine orderLine)
	{
		if (!isSetBOMDescription())
		{
			return;
		}

		final ProductId productId = ProductId.ofRepoIdOrNull(orderLine.getM_Product_ID());
		if (productId == null)
		{
			return;
		}

		final IProductBOMBL productBOMBL = Services.get(IProductBOMBL.class);
		final String description = productBOMBL.getBOMDescriptionForProductId(productId);
		if (Check.isEmpty(description, true))
		{
			return;
		}

		orderLine.setProductDescription(description);
	}

	private boolean isSetBOMDescription()
	{
		return Services.get(ISysConfigBL.class).getBooleanValue(SYSCONFIG_SetBOMDescription, false);
	}

	@Override
	public void updateProductDocumentNote(final I_C_OrderLine orderLine)
	{
		final org.compiere.model.I_M_Product product = orderLine.getM_Product();

		if (product == null)
		{
			orderLine.setM_Product_DocumentNote(null);
		}

		final I_C_Order order = orderLine.getC_Order();

		final org.compiere.model.I_C_BPartner partner = order.getC_BPartner();

		final String adLanguage = partner.getAD_Language();

		final org.compiere.model.I_M_Product translatedProduct = translate(product, org.compiere.model.I_M_Product.class, adLanguage);

		orderLine.setM_Product_DocumentNote(translatedProduct.getDocumentNote());
	}
}
