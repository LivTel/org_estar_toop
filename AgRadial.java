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
// AgRadial.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/AgRadial.java,v 1.2 2007-01-30 18:35:20 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * AgRadial command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.2 $
 */
class AgRadial extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: AgRadial.java,v 1.2 2007-01-30 18:35:20 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "AgRadial";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "AGRADIAL";
	/**
	 * Input into the agradial command, the autoguider radial position from the field edge in mm.
	 */
	protected double position = 0.0;

	/**
	 * Default constructor.
	 */
	public AgRadial() 
	{
		super();
	}

	/**
	 * Set the autoguider radial position from the field edge in mm., an input into the AGRADIAL command.
	 * @param d The autoguider radial position from the field edge in mm.
	 * @see #position
	 */
	public void setPosition(double d)
	{
		position = d;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #position
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+position);
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","AgRadial successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","AgRadial failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		AgRadial agradial = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String positionString = null;
		double position;

		if(args.length != 2)
		{
			System.out.println("java org.estar.toop.AgRadial <input properties filename> <position in mm>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		positionString = args[1];
		// parse agradials
		position = Double.parseDouble(positionString);
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.AgRadial");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCSessionData");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		// load session data
		sessionData = new TOCSessionData();
		try
		{
			sessionData.load(inputPropertiesFile);
		}
		catch(Exception e)
		{
			System.err.println("Loading session data failed:"+e);
			e.printStackTrace();
			System.exit(1);
		}
		agradial = new AgRadial();
		agradial.setSessionData(sessionData);
		agradial.setPosition(position);
		agradial.run();
		if(agradial.getSuccessful())
		{
			System.out.println("AgRadial successful.");
		}
		else
		{
			System.out.println("AgRadial failed:"+agradial.getErrorString()+".");
		}
		System.out.println("AgRadial finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/08 18:05:01  cjm
** Initial revision
**
** Revision 1.1  2005/06/08 16:06:18  cjm
** Initial revision
**
** Revision 1.1  2005/06/06 14:45:04  cjm
** Initial revision
**
*/
