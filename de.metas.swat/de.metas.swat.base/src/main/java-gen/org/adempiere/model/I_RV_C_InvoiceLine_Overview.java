/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.adempiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.MTable;
import org.compiere.util.KeyNamePair;

/** Generated Interface for RV_C_InvoiceLine_Overview
 *  @author Adempiere (generated) 
 *  @version Release 3.5.4a
 */
public interface I_RV_C_InvoiceLine_Overview 
{

    /** TableName=RV_C_InvoiceLine_Overview */
    public static final String Table_Name = "RV_C_InvoiceLine_Overview";

    /** AD_Table_ID=540262 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Mandant.
	  * Mandant fuer diese Installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_OrgTrx_ID */
    public static final String COLUMNNAME_AD_OrgTrx_ID = "AD_OrgTrx_ID";

	/** Set Buchende Organisation.
	  * Durchfuehrende oder ausloesende Organisation
	  */
	public void setAD_OrgTrx_ID (int AD_OrgTrx_ID);

	/** Get Buchende Organisation.
	  * Durchfuehrende oder ausloesende Organisation
	  */
	public int getAD_OrgTrx_ID();

	public org.compiere.model.I_AD_Org getAD_OrgTrx() throws RuntimeException;

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organisation.
	  * Organisatorische Einheit des Mandanten
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organisation.
	  * Organisatorische Einheit des Mandanten
	  */
	public int getAD_Org_ID();

    /** Column name C_Activity_ID */
    public static final String COLUMNNAME_C_Activity_ID = "C_Activity_ID";

	/** Set Activity.
	  * Business Activity
	  */
	public void setC_Activity_ID (int C_Activity_ID);

	/** Get Activity.
	  * Business Activity
	  */
	public int getC_Activity_ID();

	public org.compiere.model.I_C_Activity getC_Activity() throws RuntimeException;

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Geschaeftspartner.
	  * Bezeichnet einen Geschaeftspartner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Geschaeftspartner.
	  * Bezeichnet einen Geschaeftspartner
	  */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException;

    /** Column name C_Campaign_ID */
    public static final String COLUMNNAME_C_Campaign_ID = "C_Campaign_ID";

	/** Set Campaign.
	  * Marketing Campaign
	  */
	public void setC_Campaign_ID (int C_Campaign_ID);

	/** Get Campaign.
	  * Marketing Campaign
	  */
	public int getC_Campaign_ID();

	public org.compiere.model.I_C_Campaign getC_Campaign() throws RuntimeException;

    /** Column name C_Charge_ID */
    public static final String COLUMNNAME_C_Charge_ID = "C_Charge_ID";

	/** Set Kosten.
	  * Zusaetzliche Kosten
	  */
	public void setC_Charge_ID (int C_Charge_ID);

	/** Get Kosten.
	  * Zusaetzliche Kosten
	  */
	public int getC_Charge_ID();

	public org.compiere.model.I_C_Charge getC_Charge() throws RuntimeException;

    /** Column name C_InvoiceLine_ID */
    public static final String COLUMNNAME_C_InvoiceLine_ID = "C_InvoiceLine_ID";

	/** Set Rechnungsposition.
	  * Rechnungszeile
	  */
	public void setC_InvoiceLine_ID (int C_InvoiceLine_ID);

	/** Get Rechnungsposition.
	  * Rechnungszeile
	  */
	public int getC_InvoiceLine_ID();

	public org.compiere.model.I_C_InvoiceLine getC_InvoiceLine() throws RuntimeException;

    /** Column name C_Invoice_ID */
    public static final String COLUMNNAME_C_Invoice_ID = "C_Invoice_ID";

	/** Set Rechnung.
	  * Invoice Identifier
	  */
	public void setC_Invoice_ID (int C_Invoice_ID);

	/** Get Rechnung.
	  * Invoice Identifier
	  */
	public int getC_Invoice_ID();

	public org.compiere.model.I_C_Invoice getC_Invoice() throws RuntimeException;

    /** Column name C_OrderLine_ID */
    public static final String COLUMNNAME_C_OrderLine_ID = "C_OrderLine_ID";

	/** Set Auftragsposition.
	  * Auftragsposition
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID);

	/** Get Auftragsposition.
	  * Auftragsposition
	  */
	public int getC_OrderLine_ID();

	public org.compiere.model.I_C_OrderLine getC_OrderLine() throws RuntimeException;

    /** Column name C_ProjectPhase_ID */
    public static final String COLUMNNAME_C_ProjectPhase_ID = "C_ProjectPhase_ID";

	/** Set Projekt-Phase.
	  * Phase eines Projektes
	  */
	public void setC_ProjectPhase_ID (int C_ProjectPhase_ID);

	/** Get Projekt-Phase.
	  * Phase eines Projektes
	  */
	public int getC_ProjectPhase_ID();

	public org.compiere.model.I_C_ProjectPhase getC_ProjectPhase() throws RuntimeException;

    /** Column name C_ProjectTask_ID */
    public static final String COLUMNNAME_C_ProjectTask_ID = "C_ProjectTask_ID";

	/** Set Projekt-Aufgabe.
	  * Aktuelle Projekt-Aufgabe in einer Phase
	  */
	public void setC_ProjectTask_ID (int C_ProjectTask_ID);

	/** Get Projekt-Aufgabe.
	  * Aktuelle Projekt-Aufgabe in einer Phase
	  */
	public int getC_ProjectTask_ID();

	public org.compiere.model.I_C_ProjectTask getC_ProjectTask() throws RuntimeException;

    /** Column name C_Project_ID */
    public static final String COLUMNNAME_C_Project_ID = "C_Project_ID";

	/** Set Project.
	  * Financial Project
	  */
	public void setC_Project_ID (int C_Project_ID);

	/** Get Project.
	  * Financial Project
	  */
	public int getC_Project_ID();

	public org.compiere.model.I_C_Project getC_Project() throws RuntimeException;

    /** Column name C_UOM_ID */
    public static final String COLUMNNAME_C_UOM_ID = "C_UOM_ID";

	/** Set Masseinheit.
	  * Masseinheit
	  */
	public void setC_UOM_ID (int C_UOM_ID);

	/** Get Masseinheit.
	  * Masseinheit
	  */
	public int getC_UOM_ID();

	public org.compiere.model.I_C_UOM getC_UOM() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Erstellt.
	  * Datum, an dem dieser Eintrag erstellt wurde
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Erstellt durch.
	  * Nutzer, der diesen Eintrag erstellt hat
	  */
	public int getCreatedBy();

    /** Column name DateInvoiced */
    public static final String COLUMNNAME_DateInvoiced = "DateInvoiced";

	/** Set Rechnungsdatum.
	  * Datum auf der Rechnung
	  */
	public void setDateInvoiced (Timestamp DateInvoiced);

	/** Get Rechnungsdatum.
	  * Datum auf der Rechnung
	  */
	public Timestamp getDateInvoiced();

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Beschreibung.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Beschreibung.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name DocStatus */
    public static final String COLUMNNAME_DocStatus = "DocStatus";

	/** Set Document Status.
	  * The current status of the document
	  */
	public void setDocStatus (String DocStatus);

	/** Get Document Status.
	  * The current status of the document
	  */
	public String getDocStatus();

    /** Column name DocumentNo */
    public static final String COLUMNNAME_DocumentNo = "DocumentNo";

	/** Set Document No.
	  * Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo);

	/** Get Document No.
	  * Document sequence number of the document
	  */
	public String getDocumentNo();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Aktiv.
	  * Der Eintrag ist im System aktiv
	  */
	public void setIsActive (boolean IsActive);

	/** Get Aktiv.
	  * Der Eintrag ist im System aktiv
	  */
	public boolean isActive();

    /** Column name IsDescription */
    public static final String COLUMNNAME_IsDescription = "IsDescription";

	/** Set Description Only.
	  * if true, the line is just description and no transaction
	  */
	public void setIsDescription (boolean IsDescription);

	/** Get Description Only.
	  * if true, the line is just description and no transaction
	  */
	public boolean isDescription();

    /** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Zeile Nr..
	  * Einzelne Zeile in dem Dokument
	  */
	public void setLine (int Line);

	/** Get Zeile Nr..
	  * Einzelne Zeile in dem Dokument
	  */
	public int getLine();

    /** Column name M_AttributeSetInstance_ID */
    public static final String COLUMNNAME_M_AttributeSetInstance_ID = "M_AttributeSetInstance_ID";

	/** Set Auspraegung Merkmals-Satz.
	  * Instanz des Merkmals-Satzes zum Produkt
	  */
	public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID);

	/** Get Auspraegung Merkmals-Satz.
	  * Instanz des Merkmals-Satzes zum Produkt
	  */
	public int getM_AttributeSetInstance_ID();

	public I_M_AttributeSetInstance getM_AttributeSetInstance() throws RuntimeException;

    /** Column name M_InOutLine_ID */
    public static final String COLUMNNAME_M_InOutLine_ID = "M_InOutLine_ID";

	/** Set Versand-/Wareneingangsposition.
	  * Position auf Versand- oder Wareneingangsbeleg
	  */
	public void setM_InOutLine_ID (int M_InOutLine_ID);

	/** Get Versand-/Wareneingangsposition.
	  * Position auf Versand- oder Wareneingangsbeleg
	  */
	public int getM_InOutLine_ID();

	public org.compiere.model.I_M_InOutLine getM_InOutLine() throws RuntimeException;

    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/** Set Produkt.
	  * Produkt, Leistung, Artikel
	  */
	public void setM_Product_ID (int M_Product_ID);

	/** Get Produkt.
	  * Produkt, Leistung, Artikel
	  */
	public int getM_Product_ID();

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException;

    /** Column name M_RMALine_ID */
    public static final String COLUMNNAME_M_RMALine_ID = "M_RMALine_ID";

	/** Set RMA-Position.
	  * Return Material Authorization Line
	  */
	public void setM_RMALine_ID (int M_RMALine_ID);

	/** Get RMA-Position.
	  * Return Material Authorization Line
	  */
	public int getM_RMALine_ID();

	public org.compiere.model.I_M_RMALine getM_RMALine() throws RuntimeException;

    /** Column name PriceActual */
    public static final String COLUMNNAME_PriceActual = "PriceActual";

	/** Set Einzelpreis.
	  * Effektiver Preis
	  */
	public void setPriceActual (BigDecimal PriceActual);

	/** Get Einzelpreis.
	  * Effektiver Preis
	  */
	public BigDecimal getPriceActual();

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Verarbeitet.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Verarbeitet.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name QtyEntered */
    public static final String COLUMNNAME_QtyEntered = "QtyEntered";

	/** Set Menge.
	  * Die Eingegebene Menge basiert auf der gewaehlten Mengeneinheit
	  */
	public void setQtyEntered (BigDecimal QtyEntered);

	/** Get Menge.
	  * Die Eingegebene Menge basiert auf der gewaehlten Mengeneinheit
	  */
	public BigDecimal getQtyEntered();

    /** Column name QtyInvoiced */
    public static final String COLUMNNAME_QtyInvoiced = "QtyInvoiced";

	/** Set Quantity Invoiced.
	  * Invoiced Quantity
	  */
	public void setQtyInvoiced (BigDecimal QtyInvoiced);

	/** Get Quantity Invoiced.
	  * Invoiced Quantity
	  */
	public BigDecimal getQtyInvoiced();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Aktualisiert.
	  * Datum, an dem dieser Eintrag aktualisiert wurde
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Aktualisiert durch.
	  * Nutzer, der diesen Eintrag aktualisiert hat
	  */
	public int getUpdatedBy();
}
