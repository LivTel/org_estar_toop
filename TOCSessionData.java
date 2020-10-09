/*   
    Copyright 2006, Astrophysics Research Institute, Liverpool John Moores University.

    This file is part of org.estar.toop.

    org.estar.toop is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    org.estar.toop is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with org.estar.toop; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
// TOCSessionData.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCSessionData.java,v 1.4 2007-01-30 18:35:33 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Looks after data associated with a target of oppurtunity control session
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
public class TOCSessionData implements Logging
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "TOCSessionData";
	/**
	 * Constant defaining the start of all keys in the session data properties.
	 */
	protected static final String ROOT_KEY = "toop";
	/**
	 * Class logger.
	 */
	private Logger logger = null;
	/**
	 * Properties filled with keyword-values.
	 */
	private NGATProperties properties = null;

	/**
	 * The logger instance is created. The properties instance is initialised.
	 * @see #logger
	 * @see #properties
	 */
	public TOCSessionData() 
	{
		super();
	        properties = new NGATProperties();
		logger = LogManager.getLogger(this);
	}

	/**
	 * Load session data from a file.
	 * @param f The file to load from.
	 * @exception FileNotFoundException Thrown if the file doesn't exist.
	 * @exception IOException Thrown if the load failed.
	 * @see #properties
	 */
	public void load(File f) throws FileNotFoundException, IOException
	{
		properties.load(f);
	}

	/**
	 * Save session data to a file.
	 * @param f The file to save to.
	 * @exception IOException Thrown if the save failed.
	 * @see #properties
	 */
	public void save(File f) throws IOException
	{
		properties.save(f,this.getClass().getName()+":save:"+new Date());
	}

	/**
	 * Set session data from a previously loaded/set set of properties.
	 * @param p The properties to inherit.
	 * @see #properties
	 */
	public void set(NGATProperties p)
	{
		properties = p;
	}

	/**
	 * Set the init focus option.
	 * @param s A string representing the init focus option, either FOCUS_ON or FOCUS_OFF.
	 * @see #setProperty
	 */
	public void setInitFocusOption(String s)
	{
		setProperty(".init.focus_option",s);
	}

	/**
	 * Set the init rotator option.
	 * @param s A string representing the init rotator option, either ROT_SKY or ROT_MOUNT.
	 * @see #setProperty
	 */
	public void setInitRotatorOption(String s)
	{
		setProperty(".init.rotator_option",s);
	}

	/**
	 * Set the init rotator option.
	 * @param s A string representing the init autoguider option, either AG_SELECT or AG_NO_SELECT.
	 * @see #setProperty
	 */
	public void setInitAGOption(String s)
	{
		setProperty(".init.ag_option",s);
	}

	/**
	 * Set the host on which the RCS TOCA is running.
	 * @param s A string representing the host.
	 * @see #setProperty
	 */
	public void setTOCSHost(String s)
	{
		setProperty(".tocs_host",s);
	}

	/**
	 * Set the port on which the RCS TOCA is running.
	 * @param p An integer representing the port.
	 * @see #setProperty
	 */
	public void setTOCAHostPort(int p)
	{
		setProperty(".tocs_port",""+p);
	}

	/**
	 * Set the service id to use.
	 * @param s A string representing a service Id.
	 * @see #setProperty
	 */
	public void setServiceId(String s)
	{
		setProperty(".service_id",s);
	}

	/**
	 * Set the session id to use.
	 * @param s A string representing a session Id.
	 * @see #setProperty
	 */
	public void setSessionId(String s)
	{
		setProperty(".session_id",s);
	}

	/**
	 * Get the init autoguider option.
	 * @return A string representing the init autoguider option, should be either AG_SELECT or AG_NO_SELECT.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getInitAGOption()
	{
		return properties.getProperty(ROOT_KEY+".init.ag_option");
	}

	/**
	 * Get the init rotator option.
	 * @return A string representing the init rotator option, should be either ROT_SKY or ROT_MOUNT.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getInitRotatorOption()
	{
		return properties.getProperty(ROOT_KEY+".init.rotator_option");
	}

	/**
	 * Get the init focus option.
	 * @return A string representing the init focus option, should be either FOCUS_ON or FOCUS_OFF.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getInitFocusOption()
	{
		return properties.getProperty(ROOT_KEY+".init.focus_option");
	}

	/**
	 * Get the current host the RCS TOCA is running on.
	 * @return A string representing the host.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getTOCSHost()
	{
		return properties.getProperty(ROOT_KEY+".tocs_host");
	}

	/**
	 * Get the current host the RCS TOCA is running on.
	 * @return A string representing the host.
	 * @exception NGATPropertyException Thrown if the property get fails (property does not exist/not a valid int).
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public int getTOCSPort() throws NGATPropertyException
	{
		return properties.getInt(ROOT_KEY+".tocs_port");
	}

	/**
	 * Get the current service id.
	 * @return A string representing a service Id.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getServiceId()
	{
		return properties.getProperty(ROOT_KEY+".service_id");
	}

	/**
	 * Get the current session id.
	 * @return A string representing a session Id.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public String getSessionId()
	{
		return properties.getProperty(ROOT_KEY+".session_id");
	}

	/**
	 * Internal method to set the value of a property. The ROOT_KEY is prepended to the keyword before
	 * saving into the properties.
	 * Also logs all sets to create noticeboard style logs.
	 * @param keyword The keyword of the property.
	 * @param value The value of the property.
	 * @see #ROOT_KEY
	 * @see #properties
	 */
	public void setProperty(String keyword,String value)
	{
		logger.log(INFO, 1, CLASS, RCSID,"setProperty","Keyword: "+ROOT_KEY+keyword+" Value : "+value+".");
		properties.setProperty(ROOT_KEY+keyword,value);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.3  2005/06/14 12:40:45  cjm
** Added set method.
**
** Revision 1.2  2005/06/06 17:46:56  cjm
** Made class public.
**
** Revision 1.1  2005/06/06 14:44:49  cjm
** Initial revision
**
*/
