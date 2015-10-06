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
 *****************************************************************************/
package org.compiere.grid.ed;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;

import org.adempiere.images.Images;
import org.adempiere.plaf.AdempierePLAF;
import org.adempiere.plaf.VEditorDialogButtonAlign;
import org.adempiere.ui.editor.ICopyPasteSupportEditor;
import org.adempiere.ui.editor.ICopyPasteSupportEditorAware;
import org.adempiere.ui.editor.NullCopyPasteSupportEditor;
import org.adempiere.util.Check;
import org.compiere.apps.ADialog;
import org.compiere.grid.ed.menu.EditorContextPopupMenu;
import org.compiere.model.GridField;
import org.compiere.swing.CButton;
import org.compiere.swing.CTextField;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

/**
 * 	URL Editor
 *	
 *  @author Jorg Janke
 *  @version $Id: VURL.java,v 1.2 2006/07/30 00:51:28 jjanke Exp $
 */
public class VURL extends JComponent
	implements VEditor, ActionListener, KeyListener
	, ICopyPasteSupportEditorAware
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3023749380845372419L;
	
	/**
	 * 	IDE Constructor
	 */
	public VURL ()
	{
		this ("URL", false, false, true, 20, 60);
	}	//	VURL
	
	/**
	 *	Detail Constructor
	 *  @param columnName column name
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param displayLength display length
	 *  @param fieldLength field length
	 */
	public VURL (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		int displayLength, int fieldLength)
	{
		super.setName(columnName);
		m_columnName = columnName;
		m_fieldLength = fieldLength;
		m_mandatory = mandatory;
		LookAndFeel.installBorder(this, "TextField.border");
		this.setLayout(new BorderLayout());
		//  Size
		this.setPreferredSize(m_text.getPreferredSize());
		int height = m_text.getPreferredSize().height;

		//	***	Text	***
		m_text = new CTextField(displayLength>VString.MAXDISPLAY_LENGTH ? VString.MAXDISPLAY_LENGTH : displayLength);
		m_text.setEditable(isReadOnly);
		m_text.setFocusable(true);
		m_text.setBorder(null);
		m_text.setHorizontalAlignment(JTextField.LEADING);

		//	Background
		setMandatory(mandatory);
		this.add(m_text, BorderLayout.CENTER);

		//	***	Button	***
		{
			m_button.setIcon(Images.getImageIcon2("Online10"));	// should be 10
			m_button.setMargin(new Insets(0, 0, 0, 0));
			m_button.setPreferredSize(new Dimension(height, height));
			m_button.addActionListener(this);
			m_button.setFocusable(false);
			VEditorDialogButtonAlign.addVEditorButtonUsingBorderLayout(getClass(), this, m_button);
		}

		//	Preferred Size
		this.setPreferredSize(this.getPreferredSize());		//	causes r/o to be the same length
		//	ReadWrite
		if (isReadOnly || !isUpdateable)
			setReadWrite(false);
		else
			setReadWrite(true);

		m_text.addKeyListener(this);
		m_text.addActionListener(this);
		
		setForeground(AdempierePLAF.getTextColor_Normal());
		setBackground(AdempierePLAF.getFieldBackground_Normal());
		
		//
		// Create and bind the context menu
		new EditorContextPopupMenu(this);
	}	//	VURL

	/**	Logger	*/
	private static CLogger log = CLogger.getCLogger (VURL.class);
	/** Column Name				*/
	private String				m_columnName;
	/** The Text				*/
	private CTextField			m_text = new CTextField();
	private boolean				m_readWrite;
	private boolean				m_mandatory;
	/** The Button              */
	private CButton				m_button = new CButton();
	/** Grid Field				*/
	private GridField      		m_mField = null;

	private String				m_oldText;
	private String				m_initialText;
	/**	Setting new value			*/
	private volatile boolean	m_setting = false;
	/**	Field in focus				*/
	@SuppressWarnings("unused")
	private volatile boolean	m_infocus = false;
	/** Field Length				*/
	private int					m_fieldLength;

	/**
	 * 	Dispose resources
	 */
	@Override
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_mField = null;
	}	//	dispose

	/**
	 * 	Set Mandatory
	 * 	@param mandatory mandatory
	 */
	@Override
	public void setMandatory (boolean mandatory)
	{
		m_mandatory = mandatory;
		m_text.setMandatory(mandatory);
		setBackground (false);
	}	//	setMandatory

	/**
	 * 	Get Mandatory
	 *  @return mandatory
	 */
	@Override
	public boolean isMandatory()
	{
		return m_mandatory;
	}	//	isMandatory

	/**
	 * 	Set ReadWrite
	 * 	@param rw read/write
	 */
	@Override
	public void setReadWrite (boolean rw)
	{
		m_readWrite = rw;
		m_text.setReadWrite(rw);
		setBackground (false);
	}	//	setReadWrite

	/**
	 * 	Is Read Write
	 * 	@return read write
	 */
	@Override
	public boolean isReadWrite()
	{
		return m_readWrite;
	}	//	isReadWrite

	/**
	 * 	Set Foreground
	 * 	@param color color
	 */
	@Override
	public void setForeground (Color color)
	{
		m_text.setForeground(color);
	}	//	SetForeground

	/**
	 * 	Set Background
	 * 	@param error Error
	 */
	@Override
	public void setBackground (boolean error)
	{
		if (error)
			setBackground(AdempierePLAF.getFieldBackground_Error());
		else if (!m_readWrite)
			setBackground(AdempierePLAF.getFieldBackground_Inactive());
		else if (m_mandatory)
			setBackground(AdempierePLAF.getFieldBackground_Mandatory());
		else
			setBackground(AdempierePLAF.getFieldBackground_Normal());
	}	//	setBackground

	/**
	 * 	Set Background
	 * 	@param color Color
	 */
	@Override
	public void setBackground (Color color)
	{
		m_text.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Editor to value
	 *  @param value value
	 */
	@Override
	public void setValue(Object value)
	{
	//	log.config( "VString.setValue", value);
		if (value == null)
			m_oldText = "";
		else
			m_oldText = value.toString();
		//	only set when not updated here
		if (m_setting)
			return;
		setText (m_oldText);
		m_initialText = m_oldText;
		//	If R/O left justify 
		if (!m_text.isEditable() || !isEnabled())
			m_text.setCaretPosition(0);
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt event
	 */
	@Override
	public void propertyChange (PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(org.compiere.model.GridField.PROPERTY))
			setValue(evt.getNewValue());
		
		// metas: request focus (2009_0027_G131) 
		if (evt.getPropertyName().equals(org.compiere.model.GridField.REQUEST_FOCUS))
			requestFocus();
		// metas end
		
	}   //  propertyChange

	/**
	 *	Return Editor value
	 *  @return value
	 */
	@Override
	public Object getValue()
	{
		return getText();
	}	//	getValue

	/**
	 *  Return Display Value
	 *  @return value
	 */
	@Override
	public String getDisplay()
	{
		return m_text.getText();
	}   //  getDisplay


	/**
	 *	Key Released.
	 *	if Escape Restore old Text
	 *  @param e event
	 */
	@Override
	public void keyReleased(KeyEvent e)
	{
		log.finest("Key=" + e.getKeyCode() + " - " + e.getKeyChar()
			+ " -> " + getText());
		//  ESC
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			setText(m_initialText);
		m_setting = true;
		try
		{
			String clear = getText();
			if (clear.length() > m_fieldLength)
				clear = clear.substring(0, m_fieldLength);
			fireVetoableChange (m_columnName, m_oldText, clear);
		}
		catch (PropertyVetoException pve)	
		{
		}
		m_setting = false;
	}	//	keyReleased

	/**
	 * 	Key Pressed
	 *	@param e ignored
	 */
	@Override
	public void keyPressed (KeyEvent e)
	{
	}	//	keyPressed
	/**
	 * 	Key Typed
	 *	@param e ignored
	 */
	@Override
	public void keyTyped (KeyEvent e)
	{
	}	//	keyTyped
	
	/**
	 * 	Add Action Listener
	 *	@param listener listener
	 */
	@Override
	public void addActionListener (ActionListener listener)
	{
		m_text.addActionListener(listener);
	}	//	addActionListener
	
	/**
	 *	Data Binding to MTable (via GridController)	-	Enter pressed
	 *  @param e event
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == m_button)
		{
			action_button();
			return;
		}
		
		//  Data Binding
		try
		{
			fireVetoableChange(m_columnName, m_oldText, getText());
		}
		catch (PropertyVetoException pve)	
		{
		}
	}	//	actionPerformed

	/**
	 * 	Action button pressed - show URL
	 */
	private void action_button()
	{
		String urlString = m_text.getText();
		if (!Check.isEmpty(urlString, true))
		{
			urlString = urlString.trim();
			try
			{
				// validate the URL
				new URL(urlString);
				Env.startBrowser(urlString);
				return;
			}
			catch (Exception e)
			{
				final String message = e.getLocalizedMessage();
				ADialog.warn(0, this, "URLnotValid", message);
			}
		}
	}	//	action button
	
	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField field
	 */
	@Override
	public void setField (GridField mField)
	{
		this.m_mField = mField;

		EditorContextPopupMenu.onGridFieldSet(this);
	}   //  setField

	@Override
	public GridField getField() {
		return m_mField;
	}
	
	/**
	 * 	Set Text
	 *	@param text text
	 */
	public void setText (String text)
	{
		m_text.setText (text);
	}	//	setText

	
	/**
	 * 	Get Text (clear)
	 *	@return text
	 */
	public String getText ()
	{
		String text = m_text.getText();
		return text;
	}	//	getText

	/**
	 * 	Focus Gained.
	 * 	Enabled with Obscure
	 *	@param e event
	 */
	public void focusGained (FocusEvent e)
	{
		m_infocus = true;
		setText(getText());		//	clear
	}	//	focusGained

	/**
	 * 	Focus Lost
	 * 	Enabled with Obscure
	 *	@param e event
	 */
	public void focusLost (FocusEvent e)
	{
		m_infocus = false;
		setText(getText());		//	obscure
	}	//	focus Lost

	// metas
	@Override
	public boolean isAutoCommit()
	{
		return true;
	}

	@Override
	public final ICopyPasteSupportEditor getCopyPasteSupport()
	{
		return m_text == null ? NullCopyPasteSupportEditor.instance : m_text.getCopyPasteSupport();
	}
}	//	VURL
