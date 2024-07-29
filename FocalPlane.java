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
// FocalPlane.java
// $Header$
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * FocalPlane command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
class FocalPlane extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "FocalPlane";
	/**
	 * The command name.
	 */
	public final static String COMMAND_NAME = "FOCALPLANE";
	/**
	 * The instrument name - FIXEDSPEC, IO:O, LIRIC, MOPTOP, RISE, SPRAT.
	 */
	// RATCAM, EA01, EM01, NUVSPEC, IRCAM, RINGOSTAR, GROPE, RINGO3, IO:THOR, 
	protected String instrumentName = null;

	/**
	 * Default constructor.
	 */
	public FocalPlane() 
	{
		super();
	}

	/**
	 * The name of the target we are setting the focal plane/aperture for.
	 * @param s An instrument name string.
	 * @see #instrumentName
	 */
	public void setInstrumentName(String s)
	{
		instrumentName = s;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #instrumentName
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		// create command string
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+instrumentName+" ");
		// run the FOCALPLANE command
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","FocalPlane successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","FocalPlane failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		FocalPlane focalPlane = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String instrumentName = null;
		Boolean b = null;

		if(args.length != 2)
		{
			System.out.println("java org.estar.toop.FocalPlane <input properties filename> <inst ID> ");
			System.out.println("Instrument name must be one of IO:O, FIXEDSPEC,  LIRIC, MOPTOP, RISE, SPRAT.");// RATCAM,RINGO3, IRCAM,IO:THOR,
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		instrumentName = args[1];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.FocalPlane");
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
		focalPlane = new FocalPlane();
		focalPlane.setSessionData(sessionData);
		focalPlane.setInstrumentName(instrumentName);
		focalPlane.run();
		if(focalPlane.getSuccessful())
		{
			System.out.println("FocalPlane successful.");
		}
		else
		{
			System.out.println("FocalPlane failed:"+focalPlane.getErrorString()+".");
		}
		System.out.println("FocalPlane finished.");
		System.exit(0);
	}
}
