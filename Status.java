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
// Status.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Status.java,v 1.2 2007-01-30 18:35:29 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Status command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
class Status extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Status";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "STATUS";
	/**
	 * Input into the status command, the Right Ascension.
	 */
	protected RA ra = null;
	/**
	 * Input into the status command, the Declination.
	 */
	protected Dec dec = null;
	/**
	 * Status category (or instrument ID) e.g. RATCAM, MECHANISM, METEO.
	 * According to the TOCA documentation, the full range of categories is documented in the RCS Status Server
	 * User Guide.
	 */
	protected String category = null;
	/**
	 * Status varaible keyword.
	 * According to the TOCA documentation, the full range of categories is documented in the RCS Status Server
	 * User Guide.
	 */
	protected String keyword = null;
	/**
	 * The value for the specified category:keyword, as returned by the TOCA status command.
	 */
	protected String value = null;

	/**
	 * Default constructor.
	 */
	public Status() 
	{
		super();
	}

	/**
	 * Set the input status category to the STATUS command.
	 * @param s The status category.
	 * @see #category
	 */
	public void setCategory(String s)
	{
		category = s;
	}

	/**
	 * Set the input status keyword to the STATUS command.
	 * @param s The status keyword.
	 * @see #keyword
	 */
	public void setKeyword(String s)
	{
		keyword = s;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+category+" "+keyword);
		super.run();
		// parse results
		if(getSuccessful())
		{
			try
			{
				// value is returned in the form: keyword = value
				value = getReplyValue(keyword);
				sessionData.setProperty(".status.value",value);
				// log
				logger.log(INFO, 1, CLASS, RCSID,"run","Status successful value : "+value+".");
			}
			catch(Exception e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing STATUS results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Status failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Get the value returned from the STATUS command.
	 * @return The value as a string.
	 * @see #value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Get the value returned from the STATUS command, as an int.
	 * @return The value as an integer.
	 * @exception NumberFormatException Thrown if the value is not a valid integer.
	 * @see #value
	 */
	public int getValueInt() throws NumberFormatException
	{
		return Integer.parseInt(value);
	}

	/**
	 * Get the value returned from the STATUS command, as a double.
	 * @return The value as a double.
	 * @exception NumberFormatException Thrown if the value is not a valid double.
	 * @see #value
	 */
	public double getValueDouble() throws NumberFormatException
	{
		return Double.parseDouble(value);
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Status status = null;
		File propertiesFile = null;
		Logger l = null;
		String keyword = null;
		String category = null;

		if(args.length != 3)
		{
			System.out.println("java org.estar.toop.Status <properties filename> <category> <keyword>");
			System.exit(1);
		}
		propertiesFile = new File(args[0]);
		category = args[1];
		keyword = args[2];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Status");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCSessionData");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		// load session data
		sessionData = new TOCSessionData();
		try
		{
			sessionData.load(propertiesFile);
		}
		catch(Exception e)
		{
			System.err.println("Loading session data failed:"+e);
			e.printStackTrace();
			System.exit(1);
		}
		status = new Status();
		status.setSessionData(sessionData);
		status.setCategory(category);
		status.setKeyword(keyword);
		status.run();
		if(status.getSuccessful())
		{
			System.out.println("Value: "+status.getValue()+".");
		}
		else
		{
			System.out.println("Status failed:"+status.getErrorString()+".");
		}
		System.out.println("Status finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/07 17:45:34  cjm
** Initial revision
**
** Revision 1.2  2005/06/06 17:46:56  cjm
** Comment fix.
**
** Revision 1.1  2005/06/06 14:45:12  cjm
** Initial revision
**
*/
