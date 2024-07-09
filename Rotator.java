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
// Rotator.java
// $Header$
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Rotator command implementation.
 * @author Chris Mottram
 * @version $Revision$
 */
class Rotator extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Rotator mode string: SKY. DO NOT USE THIS OPTION, it is not implemented properly in the RCS TOCA
	 * code (no cardinal pointing support).
	 */
	public final static String ROTATOR_MODE_SKY = "SKY";
	/**
	 * Rotator mode string: MOUNT. 
	 */
	public final static String ROTATOR_MODE_MOUNT = "MOUNT";
	/**
	 * Rotator mode string: FLOAT. 
	 */
	public final static String ROTATOR_MODE_FLOAT = "FLOAT";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Rotator";
	/**
	 * The command name.
	 */
	public final static String COMMAND_NAME = "ROTATOR";
	/**
	 * The rotator mode to configure.
	 */
	protected String rotatorMode = null;
	/**
	 * The mount angle to move the rotator to, before floating the rotator, when the rotator mode is 
	 * ROTATOR_MODE_MOUNT.
	 * @see #ROTATOR_MODE_MOUNT
	 */
	protected double mountAngle = 0.0;

	/**
	 * Default constructor.
	 */
	public Rotator() 
	{
		super();
	}

	/**
	 * The rotator mode to configure.
	 * @param s The rotator mode, one of "SKY", "MOUNT", "FLOAT".
	 * @see #rotatorMode
	 * @see #ROTATOR_MODE_SKY
	 * @see #ROTATOR_MODE_MOUNT
	 * @see #ROTATOR_MODE_FLOAT
	 */
	public void setRotatorMode(String s)
	{
		rotatorMode = s;
	}

	/**
	 * Set the mount angle to move the rotator to, if the rotator mode is "MOUNT".
	 * @param d The rotator mount angle in degrees.
	 * @see #mountAngle
	 */
	public void setMountAngle(double d)
	{
		mountAngle = d;
	}
	
	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #rotatorMode
	 * @see #mountAngle
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		// create command string
		if(rotatorMode.equalsIgnoreCase("MOUNT"))
		{
			commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+rotatorMode+" "+
						   mountAngle);
		}
		else
		{
			commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+rotatorMode);
		}
		// run the ROTATOR command
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Rotator successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Rotator failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Rotator rotator = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String rotatorMode = null;
		double mountAngle = 0.0;
		
		if(args.length < 2)
		{
			System.out.println("java org.estar.toop.Rotator <input properties filename> <rotator mode> [<mount angle>]");
			System.out.println("<rotator mode> should be one of: 'SKY','MOUNT' or 'FLOAT'.");
			System.out.println("<mount angle> is a valid angle in degrees (used for 'MOUNT' mode only).");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		rotatorMode = args[1];
		if(rotatorMode.equalsIgnoreCase("MOUNT"))
		{
			if(args.length < 3)
			{
				System.out.println("java org.estar.toop.Rotator <input properties filename> <rotator mode> [<mount angle>]");
				System.out.println("<rotator mode> should be one of: 'SKY','MOUNT' or 'FLOAT'.");
				System.out.println("<mount angle> is a valid angle in degrees (used for 'MOUNT' mode only).");
				System.exit(1);
			}
			mountAngle = Double.parseDouble(args[2]);
		}
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Rotator");
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
		// send command		
		rotator = new Rotator();
		rotator.setSessionData(sessionData);
		rotator.setRotatorMode(rotatorMode);
		rotator.setMountAngle(mountAngle);
		rotator.run();
		if(rotator.getSuccessful())
		{
			System.out.println("Rotator successful.");
		}
		else
		{
			System.out.println("Rotator failed:"+rotator.getErrorString()+".");
		}
		System.out.println("Rotator finished.");
		System.exit(0);
	}
}
		
