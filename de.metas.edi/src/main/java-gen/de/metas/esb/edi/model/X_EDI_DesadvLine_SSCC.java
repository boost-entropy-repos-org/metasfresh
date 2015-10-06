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
package de.metas.esb.edi.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.Env;

/** Generated Model for EDI_DesadvLine_SSCC
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_EDI_DesadvLine_SSCC extends org.compiere.model.PO implements I_EDI_DesadvLine_SSCC, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = -1715309174L;

    /** Standard Constructor */
    public X_EDI_DesadvLine_SSCC (Properties ctx, int EDI_DesadvLine_SSCC_ID, String trxName)
    {
      super (ctx, EDI_DesadvLine_SSCC_ID, trxName);
      /** if (EDI_DesadvLine_SSCC_ID == 0)
        {
			setEDI_DesadvLine_ID (0);
			setEDI_DesadvLine_SSCC_ID (0);
			setIPA_SSCC18 (null);
        } */
    }

    /** Load Constructor */
    public X_EDI_DesadvLine_SSCC (Properties ctx, ResultSet rs, String trxName)
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

	@Override
	public de.metas.esb.edi.model.I_EDI_DesadvLine getEDI_DesadvLine() throws RuntimeException
	{
		return get_ValueAsPO(COLUMNNAME_EDI_DesadvLine_ID, de.metas.esb.edi.model.I_EDI_DesadvLine.class);
	}

	@Override
	public void setEDI_DesadvLine(de.metas.esb.edi.model.I_EDI_DesadvLine EDI_DesadvLine)
	{
		set_ValueFromPO(COLUMNNAME_EDI_DesadvLine_ID, de.metas.esb.edi.model.I_EDI_DesadvLine.class, EDI_DesadvLine);
	}

	/** Set EDI_DesadvLine.
		@param EDI_DesadvLine_ID EDI_DesadvLine	  */
	@Override
	public void setEDI_DesadvLine_ID (int EDI_DesadvLine_ID)
	{
		if (EDI_DesadvLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_EDI_DesadvLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_EDI_DesadvLine_ID, Integer.valueOf(EDI_DesadvLine_ID));
	}

	/** Get EDI_DesadvLine.
		@return EDI_DesadvLine	  */
	@Override
	public int getEDI_DesadvLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_EDI_DesadvLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set EDI_DesadvLine_SSCC.
		@param EDI_DesadvLine_SSCC_ID EDI_DesadvLine_SSCC	  */
	@Override
	public void setEDI_DesadvLine_SSCC_ID (int EDI_DesadvLine_SSCC_ID)
	{
		if (EDI_DesadvLine_SSCC_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_EDI_DesadvLine_SSCC_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_EDI_DesadvLine_SSCC_ID, Integer.valueOf(EDI_DesadvLine_SSCC_ID));
	}

	/** Get EDI_DesadvLine_SSCC.
		@return EDI_DesadvLine_SSCC	  */
	@Override
	public int getEDI_DesadvLine_SSCC_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_EDI_DesadvLine_SSCC_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set SSCC18.
		@param IPA_SSCC18 SSCC18	  */
	@Override
	public void setIPA_SSCC18 (java.lang.String IPA_SSCC18)
	{
		set_Value (COLUMNNAME_IPA_SSCC18, IPA_SSCC18);
	}

	/** Get SSCC18.
		@return SSCC18	  */
	@Override
	public java.lang.String getIPA_SSCC18 () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_IPA_SSCC18);
	}

	/** Set Menge CU.
		@param QtyCU Menge CU	  */
	@Override
	public void setQtyCU (java.math.BigDecimal QtyCU)
	{
		set_Value (COLUMNNAME_QtyCU, QtyCU);
	}

	/** Get Menge CU.
		@return Menge CU	  */
	@Override
	public java.math.BigDecimal getQtyCU () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyCU);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Menge CU/LU.
		@param QtyCUsPerLU Menge CU/LU	  */
	@Override
	public void setQtyCUsPerLU (java.math.BigDecimal QtyCUsPerLU)
	{
		set_Value (COLUMNNAME_QtyCUsPerLU, QtyCUsPerLU);
	}

	/** Get Menge CU/LU.
		@return Menge CU/LU	  */
	@Override
	public java.math.BigDecimal getQtyCUsPerLU () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyCUsPerLU);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Menge TU.
		@param QtyTU Menge TU	  */
	@Override
	public void setQtyTU (int QtyTU)
	{
		set_Value (COLUMNNAME_QtyTU, Integer.valueOf(QtyTU));
	}

	/** Get Menge TU.
		@return Menge TU	  */
	@Override
	public int getQtyTU () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_QtyTU);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}