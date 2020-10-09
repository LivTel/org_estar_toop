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
// Position.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Position.java,v 1.4 2007-01-30 18:35:26 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Position command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
public class Position extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Position";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "POSITION";
	/**
	 * Position state returned from the position command.
	 * @see #state
	 */
	public static final String POSITION_STATE_RISEN = "RISEN";
	/**
	 * Position state returned from the position command.
	 * @see #state
	 */
	public static final String POSITION_STATE_SET = "SET";
	/**
	 * Input into the position command, the Right Ascension.
	 */
	protected RA ra = null;
	/**
	 * Input into the position command, the Declination.
	 */
	protected Dec dec = null;
	/**
	 * Result returned from the Position command, showing what state the position is in.
	 * One of NEVER_SETS, NEVER_RISES, RISER.
	 */
	protected String category = null;
	/**
	 * Result returned from the Position command, showing the current state the position is in.
	 * One of RISEN, SET.
	 */
	protected String state = null;
	/**
	 * The number of seconds until the position will rise above the altitude limit of the telescope.
	 */
	protected int timeToRise = 0;
	/**
	 * The number of seconds until the position will set below the altitude limit of the telescope.
	 */
	protected int timeToSet = 0;
	/**
	 * The current altitude of the target, in decimal degrees.
	 */
	protected double altitude = 0;
	/**
	 * The current azimuth of the target, in decimal degrees.
	 */
	protected double azimuth = 0;
	/**
	 * The distance to the moon from the target, in decimal degrees.
	 */
	protected double moonDistance = 0;

	/**
	 * Default constructor.
	 */
	public Position() 
	{
		super();
	}

	/**
	 * Set the input RA to the POSITION command.
	 * @param r The ra.
	 * @see #ra
	 */
	public void setRA(RA r)
	{
		ra = r;
	}

	/**
	 * Set the input RA to the POSITION command.
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
	 * Set the input declination to the POSITION command.
	 * @param d The declination.
	 * @see #dec
	 */
	public void setDec(Dec d)
	{
		dec = d;
	}

	/**
	 * Set the input declination to the POSITION command.
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
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+ra.toString(':')+" "+dec.toString(':'));
		super.run();
		// diddly results
		if(getSuccessful())
		{
			try
			{
				// altitude
				altitude = getReplyValueDouble("alt");// in decimal degrees
				sessionData.setProperty(".position.altitude",""+altitude);
				// azimuth
				azimuth = getReplyValueDouble("az");// in decimal degrees
				sessionData.setProperty(".position.azimuth",""+azimuth);
				// timeToRise
				timeToRise = getReplyValueInt("rise");// in seconds
				sessionData.setProperty(".position.time_to_rise",""+timeToRise);
				// timeToSet
				timeToSet = getReplyValueInt("set");// in seconds
				sessionData.setProperty(".position.time_to_set",""+timeToSet);
				// moonDistance
				moonDistance = getReplyValueDouble("moon");// in decimal degrees
				sessionData.setProperty(".position.moon_distance",""+moonDistance);
				// category
				category = getReplyValue("cat");
				sessionData.setProperty(".position.category",category);
				// state
				state = getReplyValue("state");
				sessionData.setProperty(".position.state",state);
				// log
				logger.log(INFO, 1, CLASS, RCSID,"run","Position successful altitude : "+altitude+
					   " azimuth : "+azimuth+
					   " category : "+category+
					   " state : "+state+
					   " time to set : "+timeToSet+" seconds "+
					   " time to rise : "+timeToRise+" seconds "+
					   " moon distance : "+moonDistance+" degrees.");
			}
			catch(NGATPropertyException e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing POSITION results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Position failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Get the altitude returned from the POSITION command.
	 * @return The altitude in decimal degrees.
	 * @see #altitude
	 */
	public double getAltitude()
	{
		return altitude;
	}

	/**
	 * Get the azimuth returned from the POSITION command.
	 * @return The azimuth in decimal degrees.
	 * @see #azimuth
	 */
	public double getAzimuth()
	{
		return azimuth;
	}

	/**
	 * Get the moon distance returned from the POSITION command.
	 * @return The moon distance in decimal degrees.
	 * @see #moonDistance
	 */
	public double getMoonDistance()
	{
		return moonDistance;
	}

	/**
	 * Get the category returned from the POSITION command.
	 * @return The category of the target position, one of: NEVER_SETS, NEVER_RISES, RISER.
	 * @see #category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * Get the state returned from the POSITION command.
	 * @return The state of the target position, one of: RISEN, SET.
	 * @see #state
	 */
	public String getState()
	{
		return state;
	}

	/**
	 * The number of seconds until the target rises. Returned from the POSITION command.
	 * @return The number of seconds.
	 * @see #timeToRise
	 */
	public int getTimeToRise()
	{
		return timeToRise;
	}

	/**
	 * The number of seconds until the target sets. Returned from the POSITION command.
	 * @return The number of seconds.
	 * @see #timeToSet
	 */
	public int getTimeToSet()
	{
		return timeToSet;
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Position position = null;
		File propertiesFile = null;
		Logger l = null;
		String raString = null;
		String decString = null;

		if(args.length != 3)
		{
			System.out.println("java org.estar.toop.Position <properties filename> <RA> <Dec>");
			System.exit(1);
		}
		propertiesFile = new File(args[0]);
		raString = args[1];
		decString = args[2];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Position");
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
		position = new Position();
		position.setSessionData(sessionData);
		position.setRA(raString);
		position.setDec(decString);
		position.run();
		if(position.getSuccessful())
		{
			System.out.println("Altitude: "+position.getAltitude()+" degrees.");
			System.out.println("Azimuth: "+position.getAzimuth()+" degrees.");
			System.out.println("Category: "+position.getCategory()+".");
			System.out.println("State: "+position.getState()+".");
			System.out.println("Time To Set: "+position.getTimeToSet()+" seconds.");
			System.out.println("Time To Rise: "+position.getTimeToRise()+" seconds.");
			System.out.println("Moon Distance: "+position.getMoonDistance()+" degrees.");
		}
		else
		{
			System.out.println("Position failed:"+position.getErrorString()+".");
		}
		System.out.println("Position finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.3  2005/06/15 12:05:03  cjm
** Made Position class public, so we can access constants within it.
**
** Revision 1.2  2005/06/06 17:46:56  cjm
** Comment fix.
**
** Revision 1.1  2005/06/06 14:45:12  cjm
** Initial revision
**
*/
