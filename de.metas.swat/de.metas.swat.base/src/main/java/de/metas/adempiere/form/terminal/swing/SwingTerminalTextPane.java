/**
 * 
 */
package de.metas.adempiere.form.terminal.swing;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.awt.Component;

import org.compiere.swing.CTextPane;

import de.metas.adempiere.form.terminal.ITerminalTextPane;
import de.metas.adempiere.form.terminal.context.ITerminalContext;

/**
 * @author cg
 * 
 */
/* package */class SwingTerminalTextPane implements ITerminalTextPane
{
	private final CTextPane textPane;
	private final ITerminalContext tc;

	protected SwingTerminalTextPane(ITerminalContext tc, String text)
	{
		super();
		
		this.tc = tc;
		this.textPane = new CTextPane();
		this.textPane.setText(text);
	}

	@Override
	public Component getComponent()
	{
		return textPane;
	}

	@Override
	public ITerminalContext getTerminalContext()
	{
		return tc;
	}

	@Override
	public String getText()
	{
		return textPane.getText();
	}

	@Override
	public Object getValue()
	{
		return textPane.getValue();
	}

	@Override
	public boolean isEditable()
	{
		return textPane.isEditable();
	}

	@Override
	public boolean isMandatory()
	{
		return textPane.isMandatory();
	}

	@Override
	public boolean isReadWrite()
	{
		return textPane.isReadWrite();
	}

	@Override
	public void setEditable(boolean edit)
	{
		textPane.setEditable(edit);
	}

	@Override
	public void setMandatory(boolean mandatory)
	{
		textPane.setMandatory(mandatory);
	}

	@Override
	public void setReadWrite(boolean rw)
	{
		textPane.setReadWrite(rw);
	}

	@Override
	public void setText(String text)
	{
		textPane.setText(text);
	}

	@Override
	public void setValue(Object value)
	{
		textPane.setValue(value);
	}

	@Override
	public void dispose()
	{
	}
}
