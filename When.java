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
// When.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/When.java,v 1.2 2007-01-30 18:35:35 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * When command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.2 $
 */
class When extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: When.java,v 1.2 2007-01-30 18:35:35 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "When";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "WHEN";
	/**
	 * The number of seconds until the specified override can take charge of the telescope.
	 */
	protected int time = 0;
	/**
	 * The current service.
	 */
	protected String currentService = null;

	/**
	 * Default constructor.
	 */
	public When() 
	{
		super();
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getServiceId());
		super.run();
		// diddly results
		if(getSuccessful())
		{
			try
			{
				time = getReplyValueInt("Time");// in seconds
				sessionData.setProperty(".when.time",""+time);
				currentService = getReplyValue("Current");
				sessionData.setProperty(".when.current_service",currentService);
				logger.log(INFO, 1, CLASS, RCSID,"run","When successful with time : "+time+
				   " seconds and current service : "+currentService+".");
			}
			catch(NGATPropertyException e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing WHEN results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","When failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Return the number of seconds before this service can take control of the telescope.
	 * @return A time in seconds.
	 * @see #time
	 */
	public int getTime()
	{
		return time;
	}
	
	/**
	 * Return the current TOCS service in control of the telescope.
	 * @return The current service
	 * @see #currentService
	 */
	public String getCurrentService()
	{
		return currentService;
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		When when = null;
		File propertiesFile = null;
		Logger l = null;

		if(args.length != 1)
		{
			System.out.println("java org.estar.toop.When <properties filename>");
			System.exit(1);
		}
		propertiesFile = new File(args[0]);
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.When");
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
		when = new When();
		when.setSessionData(sessionData);
		when.run();
		if(when.getSuccessful())
		{
			System.out.println("Time we can next take control:"+when.getTime()+" seconds.");
			System.out.println("Currently active service ID:"+when.getCurrentService()+".");
		}
		else
		{
			System.out.println("When failed:"+when.getErrorString()+".");
		}
		System.out.println("When finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/06 14:44:40  cjm
** Initial revision
**
*/
