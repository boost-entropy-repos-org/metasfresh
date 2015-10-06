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
/** Generated Model - DO NOT CHANGE */
package de.metas.printing.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for AD_Printer_Config
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_AD_Printer_Config extends org.compiere.model.PO implements I_AD_Printer_Config, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = -2013919627L;

    /** Standard Constructor */
    public X_AD_Printer_Config (Properties ctx, int AD_Printer_Config_ID, String trxName)
    {
      super (ctx, AD_Printer_Config_ID, trxName);
      /** if (AD_Printer_Config_ID == 0)
        {
			setAD_Printer_Config_ID (0);
			setHostKey (null);
// @#AD_Session.HostKey@
			setIsSharedPrinterConfig (false);
// N
        } */
    }

    /** Load Constructor */
    public X_AD_Printer_Config (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** Set Printer Matching Config.
		@param AD_Printer_Config_ID Printer Matching Config	  */
	@Override
	public void setAD_Printer_Config_ID (int AD_Printer_Config_ID)
	{
		if (AD_Printer_Config_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_Printer_Config_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_Printer_Config_ID, Integer.valueOf(AD_Printer_Config_ID));
	}

	/** Get Printer Matching Config.
		@return Printer Matching Config	  */
	@Override
	public int getAD_Printer_Config_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Printer_Config_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public de.metas.printing.model.I_AD_Printer_Config getAD_Printer_Config_Shared() throws RuntimeException
	{
		return get_ValueAsPO(COLUMNNAME_AD_Printer_Config_Shared_ID, de.metas.printing.model.I_AD_Printer_Config.class);
	}

	@Override
	public void setAD_Printer_Config_Shared(de.metas.printing.model.I_AD_Printer_Config AD_Printer_Config_Shared)
	{
		set_ValueFromPO(COLUMNNAME_AD_Printer_Config_Shared_ID, de.metas.printing.model.I_AD_Printer_Config.class, AD_Printer_Config_Shared);
	}

	/** Set Benutzte Konfiguration.
		@param AD_Printer_Config_Shared_ID Benutzte Konfiguration	  */
	@Override
	public void setAD_Printer_Config_Shared_ID (int AD_Printer_Config_Shared_ID)
	{
		if (AD_Printer_Config_Shared_ID < 1) 
			set_Value (COLUMNNAME_AD_Printer_Config_Shared_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Printer_Config_Shared_ID, Integer.valueOf(AD_Printer_Config_Shared_ID));
	}

	/** Get Benutzte Konfiguration.
		@return Benutzte Konfiguration	  */
	@Override
	public int getAD_Printer_Config_Shared_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Printer_Config_Shared_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Host key.
		@param HostKey 
		Unique identifier of a host
	  */
	@Override
	public void setHostKey (java.lang.String HostKey)
	{
		set_ValueNoCheck (COLUMNNAME_HostKey, HostKey);
	}

	/** Get Host key.
		@return Unique identifier of a host
	  */
	@Override
	public java.lang.String getHostKey () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_HostKey);
	}

	/** Set Geteilt.
		@param IsSharedPrinterConfig Geteilt	  */
	@Override
	public void setIsSharedPrinterConfig (boolean IsSharedPrinterConfig)
	{
		set_Value (COLUMNNAME_IsSharedPrinterConfig, Boolean.valueOf(IsSharedPrinterConfig));
	}

	/** Get Geteilt.
		@return Geteilt	  */
	@Override
	public boolean isSharedPrinterConfig () 
	{
		Object oo = get_Value(COLUMNNAME_IsSharedPrinterConfig);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}