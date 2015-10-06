/**
 * 
 */
package de.metas.adempiere.form.terminal;

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


import java.beans.PropertyChangeListener;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.adempiere.util.Check;
import org.adempiere.util.beans.WeakPropertyChangeSupport;

import de.metas.adempiere.form.terminal.context.ITerminalContext;

/**
 * Abstract {@link ITerminalKey} implementation.
 * 
 * @author cg
 * 
 */
public abstract class TerminalKey implements ITerminalKey, IDisposable
{
	private final ITerminalContext terminalContext;
	protected final transient WeakPropertyChangeSupport listeners;

	private boolean enabledKey = true;
	protected ITerminalKeyStatus status = null;

	private int guiWidth;
	private int guiHeight;

	public TerminalKey(final ITerminalContext terminalContext)
	{
		super();

		Check.assumeNotNull(terminalContext, "terminalContext not null");
		this.terminalContext = terminalContext;
		this.listeners = terminalContext.createPropertyChangeSupport(this);
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		dispose();
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void dispose()
	{
		listeners.clear();
	}
	
	public final ITerminalContext getTerminalContext()
	{
		return terminalContext;
	}

	@Override
	public void addListener(final PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}

		listeners.removePropertyChangeListener(listener); // make sure we are not adding it twice
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void addListener(final String propertyName, final PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}

		listeners.removePropertyChangeListener(propertyName, listener); // make sure we are not adding it twice
		listeners.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removeListener(final PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}

		listeners.removePropertyChangeListener(listener);
	}

	@Override
	public void removeListener(final String propertyName, final PropertyChangeListener listener)
	{
		if (listener == null)
		{
			return;
		}

		listeners.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void setStatus(final ITerminalKeyStatus status)
	{
		final ITerminalKeyStatus statusOld = this.status;
		this.status = status;
		listeners.firePropertyChange(ACTION_StatusChanged, statusOld, this.status);
	}

	public void fireStatusChanged()
	{
		final ITerminalKeyStatus statusOld = null; // force always update
		listeners.firePropertyChange(ACTION_StatusChanged, statusOld, status);
	}

	@Override
	public ITerminalKeyStatus getStatus()
	{
		return status;
	}

	@Override
	public boolean isEnabledKey()
	{
		return enabledKey;
	}

	@Override
	public void setEnabledKey(final boolean enabledKey)
	{
		this.enabledKey = enabledKey;
	}

	@Override
	public String getDebugInfo()
	{
		return null;
	}

	@Override
	public int getSpanX()
	{
		return 1;
	}

	@Override
	public int getSpanY()
	{
		return 1;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public IKeyLayout getSubKeyLayout()
	{
		// no sub-KeyLayout by default
		return null;
	}

	@Override
	public String getText()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "["
				+ "name=" + getName()
				+ ", id=" + getId()
				+ "]";
	}

	@Override
	public void setWidth(final int width)
	{
		guiWidth = width;
	}

	@Override
	public int getWidth()
	{
		return guiWidth;
	}

	@Override
	public void setHeight(final int height)
	{
		guiHeight = height;
	}

	@Override
	public int getHeight()
	{
		return guiHeight;
	}
}
