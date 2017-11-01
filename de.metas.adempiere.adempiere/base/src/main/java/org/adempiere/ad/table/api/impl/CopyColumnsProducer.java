package org.adempiere.ad.table.api.impl;

import java.util.List;
import java.util.Properties;

import org.adempiere.ad.callout.api.IADColumnCalloutBL;
import org.adempiere.ad.table.api.IADTableDAO;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.util.Check;
import org.adempiere.util.ILoggable;
import org.adempiere.util.Services;
import org.compiere.model.I_AD_Column;
import org.compiere.model.I_AD_Table;
import org.compiere.model.M_Element;
import org.compiere.util.Env;

import com.google.common.collect.ImmutableList;

import de.metas.adempiere.service.IColumnBL;
import lombok.NonNull;

/**
 * Producer class used to given {@link I_AD_Column}s (see {@link #setSourceColumns(List)}) to another table (see {@link #setTargetTable(I_AD_Table)}).
 *
 * @author tsa
 *
 */
public class CopyColumnsProducer
{
	public static CopyColumnsProducer newInstance()
	{
		return new CopyColumnsProducer();
	}

	//
	// Services
	private final IADTableDAO adTableDAO = Services.get(IADTableDAO.class);
	private final IADColumnCalloutBL adColumnCalloutBL = Services.get(IADColumnCalloutBL.class);
	private final IColumnBL columnBL = Services.get(IColumnBL.class);

	private ILoggable _logger;
	private I_AD_Table _targetTable;
	private List<I_AD_Column> _sourceColumns;
	private String _entityType = null;

	private CopyColumnsProducer()
	{
	}

	private void addLog(final String message)
	{
		if (_logger == null)
		{
			return;
		}

		_logger.addLog(message);
	}

	public CopyColumnsProducer setLogger(final ILoggable logger)
	{
		_logger = logger;
		return this;
	}

	private I_AD_Table getTargetTable()
	{
		Check.assumeNotNull(_targetTable, "targetTable not null");
		return _targetTable;
	}

	public CopyColumnsProducer setTargetTable(@NonNull final I_AD_Table targetTable)
	{
		_targetTable = targetTable;
		return this;
	}

	private List<I_AD_Column> getSourceColumns()
	{
		Check.assumeNotEmpty(_sourceColumns, "sourceColumns not empty");
		return _sourceColumns;
	}

	public CopyColumnsProducer setSourceColumns(final List<I_AD_Column> sourceColumns)
	{
		Check.assumeNotEmpty(sourceColumns, "sourceColumns not empty");
		_sourceColumns = ImmutableList.copyOf(sourceColumns);
		return this;
	}

	public CopyColumnsProducer setEntityType(final String entityType)
	{
		_entityType = entityType;
		return this;
	}

	private String getEntityTypeEffective()
	{
		if (Check.isEmpty(_entityType))
		{
			return getTargetTable().getEntityType();
		}
		else
		{
			return _entityType;
		}
	}

	/**
	 * @return now many columns were created
	 */
	public int create()
	{
		final List<I_AD_Column> sourceColumns = getSourceColumns();

		int countCreated = 0;
		for (final I_AD_Column sourceColumn : sourceColumns)
		{
			final I_AD_Column targetColumn = copyColumn(sourceColumn);
			if (targetColumn == null)
			{
				continue;
			}

			countCreated++;
		}

		return countCreated;
	}

	private I_AD_Column copyColumn(final I_AD_Column sourceColumn)
	{
		final I_AD_Table targetTable = getTargetTable();
		if (targetTable.getAD_Table_ID() == sourceColumn.getAD_Table_ID())
		{
			addLog("@AD_Column_ID@ " + targetTable.getTableName() + "." + sourceColumn.getColumnName() + ": same table [SKIP]");
			return null;
		}

		final String sourceTableName = adTableDAO.retrieveTableName(sourceColumn.getAD_Table_ID());

		final I_AD_Column colTarget = InterfaceWrapperHelper.newInstance(I_AD_Column.class);
		colTarget.setAD_Org_ID(0);
		colTarget.setAD_Table_ID(targetTable.getAD_Table_ID());
		colTarget.setEntityType(getEntityTypeEffective());

		// special case the key -> sourceTable_ID
		if (sourceColumn.getColumnName().equals(sourceTableName + "_ID"))
		{
			final String targetColumnName = new String(targetTable.getTableName() + "_ID");
			colTarget.setColumnName(targetColumnName);
			// if the element don't exist, create it
			final Properties ctx = Env.getCtx();
			M_Element element = M_Element.get(ctx, targetColumnName);
			if (element == null)
			{
				element = new M_Element(ctx, targetColumnName, targetTable.getEntityType(), ITrx.TRXNAME_ThreadInherited);
				if (targetColumnName.equalsIgnoreCase(targetTable.getTableName() + "_ID"))
				{
					element.setColumnName(targetTable.getTableName() + "_ID");
					element.setName(targetTable.getName());
					element.setPrintName(targetTable.getName());
				}
				element.saveEx(ITrx.TRXNAME_ThreadInherited);
				addLog("@AD_Element_ID@ " + element.getColumnName() + ": @Created@"); // metas
			}
			colTarget.setAD_Element_ID(element.getAD_Element_ID());
			colTarget.setName(targetTable.getName());
			colTarget.setDescription(targetTable.getDescription());
			colTarget.setHelp(targetTable.getHelp());
		}
		else
		{
			colTarget.setColumnName(sourceColumn.getColumnName());
			colTarget.setAD_Element_ID(sourceColumn.getAD_Element_ID());
			colTarget.setName(sourceColumn.getName());
			colTarget.setDescription(sourceColumn.getDescription());
			colTarget.setHelp(sourceColumn.getHelp());
			colTarget.setIsAllowLogging(columnBL.getDefaultAllowLoggingByColumnName(colTarget.getColumnName()));
		}

		// metas: begin
		if (adTableDAO.hasColumnName(targetTable.getTableName(), colTarget.getColumnName()))
		{
			addLog("@AD_Column_ID@ " + targetTable.getTableName() + "." + colTarget.getColumnName() + ": already exists [SKIP]");
			return null;
		}
		// metas: end

		colTarget.setVersion(sourceColumn.getVersion());
		colTarget.setAD_Val_Rule_ID(sourceColumn.getAD_Val_Rule_ID());
		colTarget.setDefaultValue(sourceColumn.getDefaultValue());
		colTarget.setFieldLength(sourceColumn.getFieldLength());
		colTarget.setIsKey(sourceColumn.isKey());
		colTarget.setIsParent(sourceColumn.isParent());
		colTarget.setIsMandatory(sourceColumn.isMandatory());
		colTarget.setIsTranslated(sourceColumn.isTranslated());
		colTarget.setIsIdentifier(sourceColumn.isIdentifier());
		colTarget.setSeqNo(sourceColumn.getSeqNo());
		colTarget.setIsEncrypted(sourceColumn.getIsEncrypted());
		colTarget.setAD_Reference_ID(sourceColumn.getAD_Reference_ID());
		colTarget.setAD_Reference_Value_ID(sourceColumn.getAD_Reference_Value_ID());
		colTarget.setIsActive(sourceColumn.isActive());
		colTarget.setVFormat(sourceColumn.getVFormat());
		// colTarget.setCallout(sourceColumns[i].getCallout()); // metas
		colTarget.setIsUpdateable(sourceColumn.isUpdateable());
		colTarget.setAD_Process_ID(sourceColumn.getAD_Process_ID());
		colTarget.setValueMin(sourceColumn.getValueMin());
		colTarget.setValueMax(sourceColumn.getValueMax());
		colTarget.setIsSelectionColumn(sourceColumn.isSelectionColumn());
		colTarget.setReadOnlyLogic(sourceColumn.getReadOnlyLogic());
		colTarget.setIsSyncDatabase(sourceColumn.getIsSyncDatabase());
		colTarget.setIsAlwaysUpdateable(sourceColumn.isAlwaysUpdateable());
		colTarget.setColumnSQL(sourceColumn.getColumnSQL());

		InterfaceWrapperHelper.save(colTarget);
		addLog("@AD_Column_ID@ " + targetTable.getTableName() + "." + colTarget.getColumnName() + ": @Created@"); // metas

		adColumnCalloutBL.copyAllFrom(colTarget, sourceColumn.getAD_Column_ID()); // metas: Copy callouts

		// TODO: Copy translations

		return colTarget;
	}
}
