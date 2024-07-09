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
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Acquire.java,v 1.2 2008-03-28 16:46:46 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Acquire command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
class Acquire extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Acquire";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "ACQUIRE";
	/**
	 * Input into the acquire command, the Right Ascension.
	 */
	protected RA ra = null;
	/**
	 * Input into the acquire command, the Declination.
	 */
	protected Dec dec = null;
	/**
	 * What sort of acquisition to perform.
	 * @see TOCSession#ACQUIRE_MODE_NONE
	 * @see TOCSession#ACQUIRE_MODE_BRIGHTEST
	 * @see TOCSession#ACQUIRE_MODE_WCS
	 */
	protected String acquireMode = TOCSession.ACQUIRE_MODE_NONE;
	/**
	 * Whether to do a NORMAL (highPrecision = false) or HIGH precision (highPrecision = true) acquisition.
	 */
	protected boolean highPrecision = false;
	/**
	 * Default constructor.
	 */
	public Acquire() 
	{
		super();
	}

	/**
	 * The acquire mode.
	 * @param s The acquire mode.
	 * @see #acquireMode
	 * @see TOCSession#ACQUIRE_MODE_NONE
	 * @see TOCSession#ACQUIRE_MODE_BRIGHTEST
	 * @see TOCSession#ACQUIRE_MODE_WCS
	 */
	public void setAcquireMode(String s)
	{
		acquireMode = s;
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
	 * Set whether the acquision threshold should be NORMAL or HIGH precision.
	 * @param s A string, either "NORMAL" or "HIGH".
	 * @see #highPrecision
	 * @exception NullPointerException Thrown if s is null.
	 * @exception IllegalArgumentException Thrown if s is NOT one of "NORMAL" or "HIGH".
	 */
	public void setPrecision(String s) throws NullPointerException, IllegalArgumentException
	{
		if(s ==null)
		{
			throw new NullPointerException(this.getClass().getName()+
							   ":setPrecision: No precision string specified.");	
		}
		if(s.equalsIgnoreCase("NORMAL"))
			setPrecision(false);
		else if(s.equalsIgnoreCase("HIGH"))
			setPrecision(true);
		else
		{
			throw new IllegalArgumentException(this.getClass().getName()+
							   ":setPrecision: Illegal precision:"+s);
		}
	}

	/**
	 * Set whether the acquision threshold should be NORMAL (false) or HIGH precision (true).
	 * @param b A boolean, if true the high precision threshold is used, 
	 *       otherwise the normal precision threshold is used.
	 * @see #highPrecision

	 */
	public void setPrecision(boolean b)
	{
		highPrecision = b;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #acquireMode
	 * @see #highPrecision
	 * @see #ra
	 * @see #dec
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		String precisionString = null;

		if(highPrecision)
			precisionString = new String("HIGH");
		else
			precisionString = new String("NORMAL");
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+
					   " "+ra.toString(':')+
					   " "+dec.toString(':')+
					   " "+acquireMode+
					   " "+precisionString);
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Acquire successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Acquire failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Acquire acquire = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String acquireMode = null;
		String precisionString = null;
		String raString = null;
		String decString = null;

		if(args.length != 5)
		{
			System.out.println("java org.estar.toop.Acquire <input properties filename> <RA> <Dec> <acquire mode> <precision>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		raString = args[1];
		decString = args[2];
		acquireMode = args[3];
		precisionString = args[4];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Acquire");
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
		acquire = new Acquire();
		acquire.setSessionData(sessionData);
		acquire.setRA(raString);
		acquire.setDec(decString);
		acquire.setAcquireMode(acquireMode);
		acquire.setPrecision(precisionString);
		acquire.run();
		if(acquire.getSuccessful())
		{
			System.out.println("Acquire successful.");
		}
		else
		{
			System.out.println("Acquire failed:"+acquire.getErrorString()+".");
		}
		System.out.println("Acquire finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2008/03/27 19:43:42  cjm
** Initial revision
**
**
*/
