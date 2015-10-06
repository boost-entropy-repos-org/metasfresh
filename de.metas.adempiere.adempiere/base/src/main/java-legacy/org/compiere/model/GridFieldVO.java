/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 * @contributor Victor Perez , e-Evolution.SC FR [ 1757088 ]                  *
 *****************************************************************************/
package org.compiere.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.ad.expression.api.IExpressionFactory;
import org.adempiere.ad.expression.api.ILogicExpression;
import org.adempiere.ad.expression.api.IStringExpression;
import org.adempiere.ad.service.IDeveloperModeBL;
import org.adempiere.util.Services;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;


/**
 *  Field Model Value Object
 *
 *  @author Jorg Janke
 *  @author Victor Perez , e-Evolution.SC FR [ 1757088 ] , [1877902] Implement JSR 223 Scripting APIs to Callout
 *  @author Carlos Ruiz, qss FR [1877902]
 *  @author Juan David Arboleda (arboleda), GlobalQSS, [ 1795398 ] Process Parameter: add display and readonly logic
 *  @see  http://sourceforge.net/tracker/?func=detail&atid=879335&aid=1877902&group_id=176962 to FR [1877902]
 *  @version  $Id: GridFieldVO.java,v 1.3 2006/07/30 00:58:04 jjanke Exp $
 */
public class GridFieldVO implements Serializable
{

	/**
	 *  Return the SQL statement used for the MFieldVO.create
	 *  @param ctx context
	 *  @return SQL with or w/o translation and 1 parameter
	 */
	public static String getSQL (Properties ctx)
	{
		//	IsActive is part of View
		String sql = "SELECT * FROM AD_Field_v WHERE AD_Tab_ID=?"
			+ " ORDER BY IsDisplayed DESC, SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Tab"))
			sql = "SELECT * FROM AD_Field_vt WHERE AD_Tab_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'"
				+ " ORDER BY IsDisplayed DESC, SeqNo";
		return sql;
	}   //  getSQL

	public String InfoFactoryClass = null;

	/**
	 *  Create Field Value Object
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TabNo tab
	 *  @param AD_Window_ID window
	 *  @param AD_Tab_ID tab
	 *  @param readOnly r/o
	 *  @param rs resultset AD_Field_v
	 *  @return MFieldVO
	 */
	public static GridFieldVO create (Properties ctx, int WindowNo, int TabNo, 
		int AD_Window_ID, int AD_Tab_ID, boolean readOnly, ResultSet rs)
	{
		GridFieldVO vo = new GridFieldVO (ctx, WindowNo, TabNo, 
			AD_Window_ID, AD_Tab_ID, readOnly);
		String columnName = "ColumnName";
		//int AD_Field_ID = 0;
		try
		{
			vo.ColumnName = rs.getString("ColumnName");
			if (vo.ColumnName == null)
				return null;

			CLogger.get().fine(vo.ColumnName);

			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1; i <= rsmd.getColumnCount(); i++)
			{
				columnName = rsmd.getColumnName (i);
				if (columnName.equalsIgnoreCase("Name"))
					vo.Header = rs.getString (i);
				else if (columnName.equalsIgnoreCase("AD_Reference_ID"))
					vo.displayType = rs.getInt (i);
				else if (columnName.equalsIgnoreCase("AD_Column_ID"))
					vo.AD_Column_ID = rs.getInt (i);
				else if (columnName.equalsIgnoreCase("AD_Table_ID"))
					vo.AD_Table_ID = rs.getInt (i);
				// metas: begin
				else if (columnName.equalsIgnoreCase("AD_Field_ID"))
				{
					vo.AD_Field_ID = rs.getInt(i);
					if (rs.wasNull())
					{
						vo.AD_Field_ID = -1;
					}
				}
				// metas: end
				else if (columnName.equalsIgnoreCase("DisplayLength"))
					vo.DisplayLength = rs.getInt (i);
				// metas: begin
				else if (columnName.equalsIgnoreCase("ColumnDisplayLength"))
					vo.ColumnDisplayLength = rs.getInt(i);
				else if (columnName.equalsIgnoreCase(I_AD_Field.COLUMNNAME_SeqNo))
					vo.setSeqNo(rs.getInt(i));
				else if (columnName.equalsIgnoreCase(I_AD_Field.COLUMNNAME_SeqNoGrid))
					vo.setSeqNoGrid(rs.getInt(i));
				// metas: end
				else if (columnName.equalsIgnoreCase("IsSameLine"))
					vo.IsSameLine = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsDisplayed"))
					vo.IsDisplayed = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase(I_AD_Field.COLUMNNAME_IsDisplayedGrid))
					vo.setIsDisplayedGrid("Y".equals(rs.getString(i)));
				else if (columnName.equalsIgnoreCase("DisplayLogic"))
					vo.DisplayLogic = rs.getString (i);
				// metas-2009_0021_AP1_CR045: begin
				else if (columnName.equalsIgnoreCase("ColorLogic"))
					vo.ColorLogic = rs.getString(i);
				// metas-2009_0021_AP1_CR045: end
				else if (columnName.equalsIgnoreCase("DefaultValue"))
					vo.DefaultValue = rs.getString (i);
				else if (columnName.equalsIgnoreCase("IsMandatory"))
					vo.IsMandatory = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsReadOnly"))
					vo.IsReadOnly = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsUpdateable"))
					vo.IsUpdateable = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsAlwaysUpdateable"))
					vo.IsAlwaysUpdateable = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsHeading"))
					vo.IsHeading = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsFieldOnly"))
					vo.IsFieldOnly = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsEncryptedField"))
					vo.IsEncryptedField = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsEncryptedColumn"))
					vo.IsEncryptedColumn = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsSelectionColumn"))
					vo.IsSelectionColumn = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("SortNo"))
					vo.SortNo = rs.getInt (i);
				else if (columnName.equalsIgnoreCase("FieldLength"))
					vo.FieldLength = rs.getInt (i);
				else if (columnName.equalsIgnoreCase("VFormat"))
					vo.VFormat = rs.getString (i);
				else if (columnName.equalsIgnoreCase("ValueMin"))
					vo.ValueMin = rs.getString (i);
				else if (columnName.equalsIgnoreCase("ValueMax"))
					vo.ValueMax = rs.getString (i);
				else if (columnName.equalsIgnoreCase("FieldGroup"))
					vo.FieldGroup = rs.getString (i);
				else if (columnName.equalsIgnoreCase("FieldGroupType"))
					vo.FieldGroupType = rs.getString (i);
				else if (columnName.equalsIgnoreCase("IsKey"))
					vo.IsKey = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("IsParent"))
					vo.IsParent = "Y".equals(rs.getString (i));
				else if (columnName.equalsIgnoreCase("Description"))
					vo.Description = rs.getString (i);
				else if (columnName.equalsIgnoreCase("Help"))
					vo.Help = rs.getString (i);
				// metas: tsa: commented out because we load callouts above, from AD_ColumnCallout
				// else if (columnName.equalsIgnoreCase("Callout"))
				// vo.Callout = rs.getString (i);
				else if (columnName.equalsIgnoreCase("AD_Process_ID"))
					vo.AD_Process_ID = rs.getInt (i);
				else if (columnName.equalsIgnoreCase("ReadOnlyLogic"))
					vo.ReadOnlyLogic = rs.getString (i);
				else if (columnName.equalsIgnoreCase("MandatoryLogic"))
					vo.MandatoryLogic = rs.getString (i);	
				else if (columnName.equalsIgnoreCase("ObscureType"))
					vo.ObscureType = rs.getString (i);
				//
				else if (columnName.equalsIgnoreCase("AD_Reference_Value_ID"))
					vo.AD_Reference_Value_ID = rs.getInt(i);
				else if (columnName.equalsIgnoreCase("ValidationCode"))
					vo.ValidationCode = rs.getString(i);
				else if (columnName.equalsIgnoreCase("AD_Val_Rule_ID"))	// metas: 03271
					vo.AD_Val_Rule_ID = rs.getInt(i);					// metas: 03271
				else if (columnName.equalsIgnoreCase("ColumnSQL"))
					vo.ColumnSQL = rs.getString(i);
				// metas: adding cloumnclass
				else if (columnName.equalsIgnoreCase("ColumnClass"))
					vo.ColumnClass = rs.getString(i);
				// metas end
				//Feature Request FR [ 1757088 ]
				else if (columnName.equalsIgnoreCase("Included_Tab_ID"))
					vo.Included_Tab_ID = rs.getInt(i);
				// Collapse Default State
				else if (columnName.equalsIgnoreCase("IsCollapsedByDefault"))
					vo.IsCollapsedByDefault = "Y".equals(rs.getString(i));
				//Info Factory class
				else if (columnName.equalsIgnoreCase("InfoFactoryClass"))
					vo.InfoFactoryClass  = rs.getString(i);
//				Feature Request FR [ 2003044 ]
				else if (columnName.equalsIgnoreCase("IsAutocomplete"))
					vo.autocomplete  = "Y".equals(rs.getString(i));
				// metas-2009_0021_AP1_CR051: begin
				else if (columnName.equalsIgnoreCase("IncludedTabHeight"))
					vo.IncludedTabHeight = rs.getInt(i);
				// metas-2009_0021_AP1_CR051: end
				// metas: begin: us215
				else if (columnName.equalsIgnoreCase("IsCalculated"))
					vo.IsCalculated = "Y".equals(rs.getString (i));
				// metas: end: us215
			}
			if (vo.Header == null)
				vo.Header = vo.ColumnName;
			//AD_Field_ID  = rs.getInt("AD_Field_ID");
		}
		catch (SQLException e)
		{
			CLogger.get().log(Level.SEVERE, "ColumnName=" + columnName, e);
			return null;
		}
		// ASP
		if (vo.IsDisplayed)
		{
			MClient client = MClient.get(ctx);
			// ASP for fields has a different approach - it must be defined as a field but hidden
			//   in order to have the proper context variable filled with defaults
			// Validate field and put IsDisplayed=N if must be hidden
			if (!client.isDisplayField(vo.AD_Field_ID))
			{
				vo.IsDisplayed = false;
				vo.isDisplayedGrid = false;
			}
		}
		MUserDefWin.apply(vo); // metas: Apply UserDef settings
//		vo.Callout = MColumnCallout.getCallouts(vo); // metas: callouts
		// metas: tsa: if debugging display ColumnNames instead of regular name
		if (Services.get(IDeveloperModeBL.class).isEnabled())
		{
			vo.Description = vo.Header+" - "+vo.Description;
			vo.Header = vo.ColumnName;
		}
		//
		vo.initFinish();
		return vo;
	}   //  create

	/**
	 *  Init Field for Process Parameter
	 *  @param ctx context
	 *  @param WindowNo window No
	 *  @param tabNo Tab No or {@link Env#TAB_None}
	 *  @param rs result set AD_Process_Para
	 *  @return MFieldVO
	 */
	public static GridFieldVO createParameter (final Properties ctx, final int WindowNo, final int tabNo, final ResultSet rs)
	{
		final GridFieldVO vo = new GridFieldVO (ctx, WindowNo, tabNo, 0, 0, false);
		vo.isProcess = true;
		vo.isProcessParameterTo = false;
		vo.IsDisplayed = true;
		vo.isDisplayedGrid = false;
		vo.IsReadOnly = false;
		vo.IsUpdateable = true;

		try
		{
			vo.AD_Table_ID = 0;
			vo.AD_Field_ID = 0; // metas
			vo.AD_Column_ID = 0; // metas-tsa: we cannot use the AD_Column_ID to store the AD_Process_Para_ID because we get inconsistencies elsewhere // rs.getInt("AD_Process_Para_ID");
			vo.ColumnName = rs.getString("ColumnName");
			vo.Header = rs.getString("Name");
			vo.Description = rs.getString("Description");
			vo.Help = rs.getString("Help");
			vo.displayType = rs.getInt("AD_Reference_ID");
			vo.IsMandatory = rs.getString("IsMandatory").equals("Y");
			vo.FieldLength = rs.getInt("FieldLength");
			vo.DisplayLength = vo.FieldLength;
			vo.DefaultValue = rs.getString("DefaultValue");
			vo.DefaultValue2 = rs.getString("DefaultValue2");
			vo.VFormat = rs.getString("VFormat");
			vo.ValueMin = rs.getString("ValueMin");
			vo.ValueMax = rs.getString("ValueMax");
			vo.isRange = rs.getString("IsRange").equals("Y");
			vo.IsEncryptedField = "Y".equals(rs.getString("IsEncrypted")); // metas: tsa: US745
			//
			vo.AD_Reference_Value_ID = rs.getInt("AD_Reference_Value_ID");
			// vo.ValidationCode = rs.getString("ValidationCode"); // metas: 03271
			vo.autocomplete = "Y".equals(rs.getString("IsAutoComplete"));
			vo.AD_Val_Rule_ID = rs.getInt("AD_Val_Rule_ID"); // metas: 03271
			vo.ReadOnlyLogic = rs.getString("ReadOnlyLogic");
			vo.DisplayLogic= rs.getString("DisplayLogic");
			
		}
		catch (SQLException e)
		{
			CLogger.get().log(Level.SEVERE, "createParameter", e);
		}
		//
		vo.initFinish();
		if (vo.DefaultValue2 == null)
			vo.DefaultValue2 = "";
		return vo;
	}   //  createParameter

	/**
	 *  Create range "to" Parameter Field from "from" Parameter Field
	 *  @param voF field value object
	 *  @return to MFieldVO
	 */
	public static GridFieldVO createParameter (GridFieldVO voF)
	{
		final GridFieldVO voT = voF.clone(voF.ctx, voF.WindowNo, voF.TabNo, voF.AD_Window_ID, voF.AD_Tab_ID, voF.tabReadOnly);
		voT.isProcess = true;
		voT.isProcessParameterTo = true;
		voT.IsDisplayed = true;
		voT.isDisplayedGrid = false;
		voT.IsReadOnly = false;
		voT.IsUpdateable = true;
		//
		voT.AD_Field_ID = voF.AD_Field_ID; // metas
		voT.AD_Table_ID = voF.AD_Table_ID;
		voT.AD_Column_ID = voF.AD_Column_ID;    //  AD_Process_Para_ID
		voT.ColumnName = voF.ColumnName;
		voT.Header = voF.Header;
		voT.Description = voF.Description;
		voT.Help = voF.Help;
		voT.displayType = voF.displayType;
		voT.IsMandatory = voF.IsMandatory;
		voT.FieldLength = voF.FieldLength;
		voT.DisplayLength = voF.FieldLength;
		voT.ColumnDisplayLength = voF.ColumnDisplayLength; // metas
		voT.DefaultValue = voF.DefaultValue2;
		voT.VFormat = voF.VFormat;
		voT.ValueMin = voF.ValueMin;
		voT.ValueMax = voF.ValueMax;
		voT.isRange = voF.isRange;
		//
		// Genied: For a range parameter the second field 
		// lookup behaviour should match the first one.
		voT.AD_Reference_Value_ID = voF.AD_Reference_Value_ID;
		voT.autocomplete = voF.autocomplete;
		
		voT.initFinish();
		return voT;
	}   //  createParameter


	/**
	 *  Make a standard field (Created/Updated/By)
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TabNo tab
	 *  @param AD_Window_ID window
	 *  @param AD_Tab_ID tab
	 *  @param tabReadOnly rab is r/o
	 *  @param isCreated is Created field
	 *  @param isTimestamp is the timestamp (not by)
	 *  @return MFieldVO
	 */
	public static GridFieldVO createStdField (Properties ctx, int WindowNo, int TabNo, 
		int AD_Window_ID, int AD_Tab_ID, boolean tabReadOnly,
		boolean isCreated, boolean isTimestamp)
	{
		GridFieldVO vo = new GridFieldVO (ctx, WindowNo, TabNo, 
			AD_Window_ID, AD_Tab_ID, tabReadOnly);
		vo.ColumnName = isCreated ? "Created" : "Updated";
		if (!isTimestamp)
			vo.ColumnName += "By";
		vo.displayType = isTimestamp ? DisplayType.DateTime : DisplayType.Table;
		if (!isTimestamp)
			vo.AD_Reference_Value_ID = 110;		//	AD_User Table Reference
		vo.IsDisplayed = false;
		vo.isDisplayedGrid = false;
		vo.IsMandatory = false;
		vo.IsReadOnly = false;
		vo.IsUpdateable = true;
		vo.initFinish();
		return vo;
	}   //  initStdField

	
	/**************************************************************************
	 *  Private constructor.
	 *  @param Ctx context
	 *  @param windowNo window
	 *  @param tabNo tab
	 *  @param ad_Window_ID window
	 *  @param ad_Tab_ID tab
	 *  @param TabReadOnly tab read only
	 */
	private GridFieldVO (Properties Ctx, int windowNo, int tabNo, 
		int ad_Window_ID, int ad_Tab_ID, boolean TabReadOnly)
	{
		ctx = Ctx;
		WindowNo = windowNo;
		TabNo = tabNo;
		AD_Window_ID = ad_Window_ID;
		AD_Tab_ID = ad_Tab_ID;
		tabReadOnly = TabReadOnly;
	}   //  MFieldVO

	static final long serialVersionUID = 4385061125114436797L;
	
	/** Context                     */
	private Properties   ctx = null;
	/** Window No                   */
	public int          WindowNo;
	/** Tab No                      */
	public int          TabNo;
	/** AD_Winmdow_ID               */
	public int          AD_Window_ID;
	/** AD_Tab_ID					*/
	public int			AD_Tab_ID;
	/** Is the Tab Read Only        */
	public boolean      tabReadOnly = false;

	/** Is Process Parameter        */
	private boolean      isProcess = false;

	/**
	 * Is Process Parameter To.
	 * 
	 * NOTE: This one is set to true only if {@link #isProcess} is set.
	 */
	private boolean isProcessParameterTo = false;

	/**	Column name		*/
	private String       ColumnName = "";
	/**	Column sql		*/
	public String       ColumnSQL;

	// metas: adding column class
	/** Column class */
	public String ColumnClass;
	// metas end

	/**	Label			*/
	public String       Header = "";
	/**	DisplayType		*/
	private int          displayType = 0;
	/**	Table ID		*/
	private int          AD_Table_ID = 0;
	/**	Clumn ID		*/
	public int          AD_Column_ID = 0;
	/** Field ID */
	public int AD_Field_ID = 0; // metas
	/**	Display Length	*/
	public int          DisplayLength = 0;
	/** Column Display Length */
	private int ColumnDisplayLength = 0; // metas
	/**	Same Line		*/
	public boolean      IsSameLine = false;
	private int			seqNo = 0;
	private int			seqNoGrid = 0;
	/**	Displayed		*/
	private boolean      IsDisplayed = false;
	/**	Displayed (grid mode) */
	private boolean      isDisplayedGrid = false;
	/**	Dislay Logic	*/
	private String       DisplayLogic = "";
	private ILogicExpression DisplayLogicExpr; // metas: 03093
	/** Color Logic */
	private String ColorLogic = "";
	private IStringExpression ColorLogicExpr = IStringExpression.NULL; // metas-2009_0021_AP1_CR045
	/**	Default Value	*/
	public String       DefaultValue = "";
	/**	Mandatory		*/
	public boolean      IsMandatory = false;
	/**	Read Only		*/
	public boolean      IsReadOnly = false;
	/**	Updateable		*/
	public boolean      IsUpdateable = false;
	/**	Always Updateable	*/
	public boolean      IsAlwaysUpdateable = false;
	/**	Heading Only	*/
	public boolean      IsHeading = false;
	/**	Field Only		*/
	public boolean      IsFieldOnly = false;
	/**	Display Encryption	*/
	public boolean      IsEncryptedField = false;
	/**	Storage Encryption	*/
	public boolean      IsEncryptedColumn = false;
	/**	Find Selection		*/
	public boolean		IsSelectionColumn = false;
	/**	Order By		*/
	public int          SortNo = 0;
	/**	Field Length		*/
	public int          FieldLength = 0;
	/**	Format enforcement		*/
	public String       VFormat = "";
	/**	Min. Value		*/
	public String       ValueMin = "";
	/**	Max. Value		*/
	public String       ValueMax = "";
	/**	Field Group		*/
	public String       FieldGroup = "";
	/**	Field Group	Type	*/
	public String       FieldGroupType = "";
	/**	PK				*/
	public boolean      IsKey = false;
	/**	FK				*/
	public boolean      IsParent = false;
//	/**	Callout			*/
//	public String       Callout = "";
	/**	Process			*/
	public int          AD_Process_ID = 0;
	/**	Description		*/
	public String       Description = "";
	/**	Help			*/
	public String       Help = "";
	/**	Mandatory Logic	*/
	public String 		MandatoryLogic = "";
	private ILogicExpression MandatoryLogicExpr; // metas: 03093
	/**	Read Only Logic	*/
	public String       ReadOnlyLogic = "";
	private ILogicExpression ReadOnlyLogicExpr; // metas: 03093
	/**	Display Obscure	*/
	public String		ObscureType = null;
	/** Included Tab Height - metas-2009_0021_AP1_CR051 */
	public int IncludedTabHeight = 0; // metas

	/**	Lookup Validation code	*/
	private String		ValidationCode = "";	 // metas: 03271: changed to private to not be accessible
	public int			AD_Val_Rule_ID = -1;	 // metas: 03271
	/**	Reference Value			*/
	public int			AD_Reference_Value_ID = 0;

	/**	Process Parameter Range		*/
	public boolean      isRange = false;
	/**	Process Parameter Value2	*/
	public String       DefaultValue2 = "";

	/** Lookup Value Object     */
	private MLookupInfo  lookupInfo = null;
	
	
	//*  Feature Request FR [ 1757088 ]
	public int          Included_Tab_ID = 0;

	/** Collapse By Default * */
	public boolean IsCollapsedByDefault = false;
	/**  Autocompletion for textfields - Feature Request FR [ 1757088 ] */
	private boolean autocomplete = false;

	public boolean IsCalculated = false; // metas
	
	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		if (lookupInfo != null)
		{
			lookupInfo.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Validate Fields and create LookupInfo if required
	 */
	private void initFinish()
	{
		final IExpressionFactory expressionFactory = Services.get(IExpressionFactory.class);
		
		//  Not null fields
		if (DisplayLogic == null)
			DisplayLogic = "";
		DisplayLogicExpr = expressionFactory.compileOrDefault(DisplayLogic, ILogicExpression.TRUE, ILogicExpression.class); // metas: 03093
		// metas-2009_0021_AP1_CR045: begin
		if (ColorLogic == null)
			ColorLogic = "";
		ColorLogicExpr = expressionFactory.compileOrDefault(ColorLogic, IStringExpression.NULL, IStringExpression.class);
		// metas-2009_0021_AP1_CR045: end
		if (DefaultValue == null)
			DefaultValue = "";
		if (FieldGroup == null)
			FieldGroup = "";
		if (FieldGroupType == null)
			FieldGroupType = "";
		if (Description == null)
			Description = "";
		if (Help == null)
			Help = "";
//		if (Callout == null)
//			Callout = "";
		if (ReadOnlyLogic == null)
			ReadOnlyLogic = "";
		ReadOnlyLogicExpr = expressionFactory.compileOrDefault(ReadOnlyLogic, ILogicExpression.FALSE, ILogicExpression.class); // metas: 03093
		if (MandatoryLogic == null)
			MandatoryLogic = "";
		MandatoryLogicExpr = expressionFactory.compileOrDefault(MandatoryLogic, ILogicExpression.FALSE, ILogicExpression.class); // metas: 03093

		createLookupInfo(true); // metas : cg: task  02354 // tsa: always create the lookupInfo
	}   //  initFinish
	
	/**
	 * Create lookup info if the type is lookup and control the creation trough displayed param
	 * 
	 * @param alwaysCreate
	 *            always create the lookup info, even if the field is not displayed
	 */
	// metas : cg: task  02354
	private void createLookupInfo(boolean alwaysCreate)
	{
		// Shall we create the MLookupInfo?
		if (lookupInfo != null)
		{
			return;
		}
		
		//  Create Lookup, if not ID
		final boolean displayed = this.IsDisplayed || this.isDisplayedGrid;
		if (DisplayType.isLookup(displayType) && (displayed || alwaysCreate))
		{
			try
			{
				if (this.lookupLoadFromColumn)
				{
					lookupInfo = MLookupFactory.getLookupInfo(Env.getCtx(), WindowNo, AD_Column_ID, displayType);
				}
				else
				{
					lookupInfo = MLookupFactory.getLookupInfo(ctx, WindowNo, AD_Column_ID, displayType,
							Env.getLanguage(ctx), ColumnName, AD_Reference_Value_ID,
							IsParent, AD_Val_Rule_ID); // metas: 03271
				}
				lookupInfo.InfoFactoryClass = this.InfoFactoryClass;
			}
			catch (Exception e)     //  Cannot create Lookup
			{
				CLogger.get().log(Level.SEVERE, "No LookupInfo for " + ColumnName, e);
				displayType = DisplayType.ID;
				lookupInfo = null;
			}
		}
	}

	/**
	 * Clone Field.
	 * <br/>
	 * Please note that following fields are not copied:
	 * <ul>
	 * <li>{@link #isProcess} is set to false</li>
	 * <li>{@link #isProcessParameterTo} is set to false</li>
	 * </ul>
	 * 
	 * @param Ctx context
	 * @param windowNo window no
	 * @param tabNo tab no
	 * @param ad_Window_ID window id
	 * @param ad_Tab_ID tab id
	 * @param TabReadOnly r/o
	 */
	public GridFieldVO clone(Properties Ctx, int windowNo, int tabNo, 
		int ad_Window_ID, int ad_Tab_ID, 
		boolean TabReadOnly)
	{
		final GridFieldVO clone = new GridFieldVO(Ctx, windowNo, tabNo,  ad_Window_ID, ad_Tab_ID, TabReadOnly);
		//
		clone.isProcess = false;
		clone.isProcessParameterTo = false;
		//  Database Fields
		clone.ColumnName = ColumnName;
		clone.ColumnSQL = ColumnSQL;
		clone.Header = Header;
		clone.displayType = displayType;
		clone.seqNo = seqNo;
		clone.seqNoGrid = seqNoGrid;
		clone.AD_Field_ID = AD_Field_ID; // metas
		clone.AD_Table_ID = AD_Table_ID;
		clone.AD_Column_ID = AD_Column_ID;
		clone.DisplayLength = DisplayLength;
		clone.ColumnDisplayLength = ColumnDisplayLength; // metas
		clone.IsSameLine = IsSameLine;
		clone.IsDisplayed = IsDisplayed;
		clone.isDisplayedGrid = isDisplayedGrid;
		clone.DisplayLogic = DisplayLogic;
		clone.DisplayLogicExpr = DisplayLogicExpr; // metas: 03093
		clone.ColorLogic = ColorLogic; // metas-2009_0021_AP1_CR045
		clone.ColorLogicExpr = ColorLogicExpr;
		clone.DefaultValue = DefaultValue;
		clone.IsMandatory = IsMandatory;
		clone.IsReadOnly = IsReadOnly;
		clone.IsUpdateable = IsUpdateable;
		clone.IsAlwaysUpdateable = IsAlwaysUpdateable;
		clone.IsHeading = IsHeading;
		clone.IsFieldOnly = IsFieldOnly;
		clone.IsEncryptedField = IsEncryptedField;
		clone.IsEncryptedColumn = IsEncryptedColumn;
		clone.IsSelectionColumn = IsSelectionColumn;
		clone.autocomplete = autocomplete;
		clone.SortNo = SortNo;
		clone.FieldLength = FieldLength;
		clone.VFormat = VFormat;
		clone.ValueMin = ValueMin;
		clone.ValueMax = ValueMax;
		clone.FieldGroup = FieldGroup;
		clone.FieldGroupType = FieldGroupType;
		clone.IsKey = IsKey;
		clone.IsParent = IsParent;
//		clone.Callout = Callout;
		clone.AD_Process_ID = AD_Process_ID;
		clone.Description = Description;
		clone.Help = Help;
		clone.ReadOnlyLogic = ReadOnlyLogic;
		clone.ReadOnlyLogicExpr = ReadOnlyLogicExpr; // metas: 03093
		clone.MandatoryLogic = MandatoryLogic;
		clone.MandatoryLogicExpr = MandatoryLogicExpr; // metas: 03093
		clone.ObscureType = ObscureType;
		clone.IncludedTabHeight = IncludedTabHeight; // metas-2009_0021_AP1_CR051
		//	Lookup
		clone.ValidationCode = ValidationCode;
		clone.AD_Val_Rule_ID = AD_Val_Rule_ID; // metas: 03271
		clone.AD_Reference_Value_ID = AD_Reference_Value_ID;
		clone.lookupInfo = lookupInfo == null ? null : lookupInfo.cloneIt();

		//  Process Parameter
		clone.isRange = isRange;
		clone.DefaultValue2 = DefaultValue2;
		
		clone.IsCalculated = IsCalculated; // metas: us215

		return clone;
	}	//	clone

	public GridFieldVO copy()
	{
		return clone(ctx, WindowNo, TabNo, AD_Window_ID, AD_Tab_ID, tabReadOnly);
	}
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	@Override
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MFieldVO[");
		sb.append(AD_Column_ID).append("-").append(ColumnName)
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	public int getDisplayType()
	{
		return this.displayType;
	}
	
	// NOTE: not setting to package level because we call it from zkwebui
	public void setDisplayType(final int displayType)
	{
		if (this.displayType == displayType)
		{
			return;
		}
		this.displayType = displayType;
		this.lookupInfo = null; // reset lookup info
	}
	
	public ILogicExpression getDisplayLogic()
	{
		return DisplayLogicExpr;
	}

	// NOTE: temporary here to let org.compiere.model.MUserDefField.apply(GridFieldVO) work
	protected void setDisplayLogic(final String displayLogicStr)
	{
		final IExpressionFactory expressionFactory = Services.get(IExpressionFactory.class);
		this.DisplayLogic = displayLogicStr;
		
		//  Not null fields
		if (DisplayLogic == null)
			DisplayLogic = "";
		DisplayLogicExpr = expressionFactory.compileOrDefault(DisplayLogic, ILogicExpression.TRUE, ILogicExpression.class); // metas: 03093
	}
	
	public ILogicExpression getReadOnlyLogic()
	{
		return ReadOnlyLogicExpr;
	}
	
	public ILogicExpression getMandatoryLogic()
	{
		return MandatoryLogicExpr;
	}
	
	public IStringExpression getColorLogic()
	{
		return ColorLogicExpr;
	}
	
	public MLookupInfo getLookupInfo()
	{
		if (lookupInfo == null)
		{
			createLookupInfo(true);
		}
		return this.lookupInfo;
	}

	public void setIsDisplayed(boolean displayed)
	{
		if (this.IsDisplayed == displayed)
		{
			return;
		}
		this.IsDisplayed = displayed;
		//this.lookupInfo = null; // no need to reset lookup info
	}
	
	public boolean isDisplayed()
	{
		return this.IsDisplayed;
	}

	public boolean isAutocomplete()
	{
		return autocomplete;
	}

	public void setAutocomplete(boolean autocomplete)
	{
		this.autocomplete = autocomplete;
	}

	public boolean isDisplayed(final GridTabLayoutMode tabLayoutMode)
	{
		if (tabLayoutMode == GridTabLayoutMode.SingleRowLayout)
		{
			return this.IsDisplayed;
		}
		else if (tabLayoutMode == GridTabLayoutMode.Grid)
		{
			return this.isDisplayedGrid;
		}
		else
		{
			throw new IllegalArgumentException("Unknown GridTabLayoutMode: " + tabLayoutMode);
		}
	}

	public void setAD_Reference_Value_ID(final int AD_Reference_Value_ID)
	{
		if (this.AD_Reference_Value_ID == AD_Reference_Value_ID)
		{
			return;
		}
		
		this.AD_Reference_Value_ID = AD_Reference_Value_ID;
		this.lookupInfo = null; // reset lookup info
	}

	public void setColumnName(String columnName)
	{
		this.ColumnName = columnName;
	}
	
	public String getColumnName()
	{
		return this.ColumnName;
	}

	private boolean lookupLoadFromColumn = false;
	public void setLookupLoadFromColumn(boolean lookupLoadFromColumn)
	{
		if (this.lookupLoadFromColumn == lookupLoadFromColumn)
		{
			return;
		}
		this.lookupLoadFromColumn = lookupLoadFromColumn;
		this.lookupInfo = null; // reset
	}

	public int getAD_Table_ID()
	{
		return this.AD_Table_ID;
	}
	
	public Properties getCtx()
	{
		return ctx;
	}

	public int getAD_Column_ID()
	{
		return AD_Column_ID;
	}

	public int getSeqNo()
	{
		return seqNo;
	}

	private void setSeqNo(int seqNo)
	{
		this.seqNo = seqNo;
	}

	public int getSeqNoGrid()
	{
		return seqNoGrid;
	}

	public void setSeqNoGrid(int seqNoGrid)
	{
		this.seqNoGrid = seqNoGrid;
	}

	public boolean isDisplayedGrid()
	{
		return isDisplayedGrid;
	}

	public void setIsDisplayedGrid(boolean isDisplayedGrid)
	{
		this.isDisplayedGrid = isDisplayedGrid;
	}

	public int getColumnDisplayLength()
	{
		return this.ColumnDisplayLength;
	}
	
	public void setColumnDisplayLength(final int columnDisplayLength)
	{
		this.ColumnDisplayLength = columnDisplayLength;
	}

	public int getAD_Field_ID()
	{
		return AD_Field_ID;
	}
	
	public boolean isProcessParameter()
	{
		return isProcess;
	}

	public boolean isCalculated()
	{
		return IsCalculated;
	}
}   //  MFieldVO
