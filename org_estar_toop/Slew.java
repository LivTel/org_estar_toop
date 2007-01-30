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
// Slew.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Slew.java,v 1.2 2007-01-30 18:35:28 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Slew command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.2 $
 */
class Slew extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Slew.java,v 1.2 2007-01-30 18:35:28 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Slew";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "SLEW";
	/**
	 * The name of the target we are slewing to - appears in FITS headers.
	 */
	protected String sourceID = null;
	/**
	 * Input into the slew command, the Right Ascension.
	 */
	protected RA ra = null;
	/**
	 * Input into the slew command, the Declination.
	 */
	protected Dec dec = null;

	/**
	 * Default constructor.
	 */
	public Slew() 
	{
		super();
	}

	/**
	 * The name of the target we are slewing to - appears in FITS headers.
	 * @param s A name string.
	 * @see #sourceID
	 */
	public void setSourceId(String s)
	{
		sourceID = s;
	}

	/**
	 * Set the input RA to the SLEW command.
	 * @param r The ra.
	 * @see #ra
	 */
	public void setRA(RA r)
	{
		ra = r;
	}

	/**
	 * Set the input RA to the SLEW command.
	 * @param s The ra, as a string in the form HH:MM:SS.ss.
	 * @exception NumberFormatException Thrown if the RA is not legal.
	 * @see #ra
	 */
	public void setRA(String s) throws NumberFormatException
	{
		ra = new RA();
		ra.parseColon(s);
	}

	/**
	 * Set the input declination to the SLEW command.
	 * @param d The declination.
	 * @see #dec
	 */
	public void setDec(Dec d)
	{
		dec = d;
	}

	/**
	 * Set the input declination to the SLEW command.
	 * @param s The dec, as a string in the form [+|-]DD:MM:SS.ss.
	 * @exception NumberFormatException Thrown if the declination is not legal.
	 * @see #dec
	 */
	public void setDec(String s) throws NumberFormatException
	{
		dec = new Dec();
		dec.parseColon(s);
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #sourceID
	 * @see #ra
	 * @see #dec
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+
					   " "+sourceID+
					   " "+ra.toString(':')+
					   " "+dec.toString(':'));
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Slew successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Slew failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Slew slew = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String sourceID = null;
		String raString = null;
		String decString = null;

		if(args.length != 4)
		{
			System.out.println("java org.estar.toop.Slew <input properties filename> <source ID> <RA> <Dec>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		sourceID = args[1];
		raString = args[2];
		decString = args[3];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Slew");
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
		slew = new Slew();
		slew.setSessionData(sessionData);
		slew.setSourceId(sourceID);
		slew.setRA(raString);
		slew.setDec(decString);
		slew.run();
		if(slew.getSuccessful())
		{
			System.out.println("Slew successful.");
		}
		else
		{
			System.out.println("Slew failed:"+slew.getErrorString()+".");
		}
		System.out.println("Slew finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/06 14:45:04  cjm
** Initial revision
**
*/
