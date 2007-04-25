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
// Instr.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Instr.java,v 1.3 2007-04-25 10:33:41 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Instr command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.3 $
 */
class Instr extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Instr.java,v 1.3 2007-04-25 10:33:41 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Instr";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "INSTR";
	/**
	 * The instrument name - RATCAM, FIXEDSPEC or IRCAM.
	 */
	protected String instID = null;
	/**
	 * If the instrument is RATCAM, the lower filter configuration.
	 */
	protected String lowerFilter = null;
	/**
	 * If the instrument is RATCAM, the upper filter configuration.
	 */
	protected String upperFilter = null;
	/**
	 * If the instrument is IRCAM, the filter configuration.
	 */
	protected String irFilter = null;
	/**
	 * Input into the instr command, the x binning.
	 */
	protected int xBinning = 2;
	/**
	 * Input into the instr command, the y binning.
	 */
	protected int yBinning = 2;
	/**
	 * Input into the instr command, whether to do calibration before any exposures using this configuration.
	 */
	protected boolean calibrateBefore = false;
	/**
	 * Input into the instr command, whether to do calibration after any exposures using this configuration.
	 */
	protected boolean calibrateAfter = false;

	/**
	 * Default constructor.
	 */
	public Instr() 
	{
		super();
	}

	/**
	 * The name of the target we are instring to - appears in FITS headers.
	 * @param s A name string.
	 * @see #instID
	 */
	public void setInstId(String s)
	{
		instID = s;
	}

	/**
	 * Set the input lower filter to the INSTR command (for RATCAM only).
	 * @param s A filter type.
	 * @see #lowerFilter
	 */
	public void setLowerFilter(String s)
	{
		lowerFilter = s;
	}

	/**
	 * Set the input upper filter to the INSTR command (for RATCAM only).
	 * @param s A filter type.
	 * @see #upperFilter
	 */
	public void setUpperFilter(String s)
	{
		upperFilter = s;
	}

	/**
	 * Set the input filter to the INSTR command (for IRCAM only).
	 * @param s A filter type.
	 * @see #irFilter
	 */
	public void setIRFilter(String s)
	{
		irFilter = s;
	}

	/**
	 * Set the input X binning to the INSTR command.
	 * For RATCAM, the x binning and y binning MUST be indentical.
	 * @param bin The binning.
	 * @see #xBinning
	 */
	public void setXBinning(int bin)
	{
		xBinning = bin;
	}

	/**
	 * Set the input Y binning to the INSTR command.
	 * For RATCAM, the x binning and y binning MUST be indentical.
	 * @param bin The binning.
	 * @see #yBinning
	 */
	public void setYBinning(int bin)
	{
		yBinning = bin;
	}

	/**
	 * Set the input to the INSTR command, 
	 * which determines whether the instrument will do a calibration frame before using this configuration.
	 * @param b A boolean
	 * @see #calibrateBefore
	 */
	public void setCalibrateBefore(boolean b)
	{
		calibrateBefore = b;
	}

	/**
	 * Set the input to the INSTR command, 
	 * which determines whether the instrument will do a calibration frame after using this configuration.
	 * @param b A boolean.
	 * @see #calibrateAfter
	 */
	public void setCalibrateAfter(boolean b)
	{
		calibrateAfter = b;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #instID
	 * @see #xBinning
	 * @see #yBinning
	 * @see #calibrateBefore
	 * @see #calibrateAfter
	 * @see #lowerFilter
	 * @see #upperFilter
	 * @see #irFilter
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		// common start bits
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+instID+" ");
		if(instID.equals("RATCAM"))
		{
			if(xBinning != yBinning)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:X binning "+xBinning+" does not match Y binning "+
							 yBinning+".");
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				return;
			}
			commandString = new String(commandString+lowerFilter+" "+upperFilter+" "+xBinning);
		}
		else if(instID.equals("IRCAM"))
		{
			if(xBinning != yBinning)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:X binning "+xBinning+" does not match Y binning "+
							 yBinning+".");
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				return;
			}
			commandString = new String(commandString+irFilter+" "+xBinning);
		}
		else if(instID.equals("FIXEDSPEC"))
		{
			commandString = new String(commandString+xBinning+" "+yBinning);
		}
		else if(instID.equals("RINGO"))
		{
			commandString = new String(commandString+xBinning+" "+yBinning);
		}
		else
		{
			successful = false;
			errorString = new String(this.getClass().getName()+
						 ":run:Unknown instrument: "+instID+".");
			logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
			return;
		}
		// common end bits
		commandString = new String(commandString+" ");
		if(calibrateBefore)
			commandString = new String(commandString+"T");
		else
			commandString = new String(commandString+"F");
		if(calibrateAfter)
			commandString = new String(commandString+"T");
		else
			commandString = new String(commandString+"F");
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Instr successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Instr failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Instr instr = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String instID = null;
		String lowerFilterString = null;
		String upperFilterString = null;
		String xBinningString = null;
		String yBinningString = null;
		String irFilterString = null;
		String calibrateBeforeString = null;
		String calibrateAfterString = null;
		Boolean b = null;
		int xBinning,yBinning;
		boolean calibrateBefore,calibrateAfter;

		if(args.length < 2)
		{
			System.out.println("java org.estar.toop.Instr <input properties filename> <inst ID> [<lfilter> <ufilter> <bin>]|[<ir filter> <bin>]|[<xbin> <ybin>] <calibrate before> <calibrate after>");
			System.out.println("Instrument ID must be one of RATCAM,IRCAM or FIXEDSPEC.");
			System.out.println("The first set of optional parameters are for RATCAM, the second for IRCAM and the third for FIXEDSPEC.");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		instID = args[1];
		if(instID.equals("RATCAM"))
		{
			if(args.length != 7)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			lowerFilterString = args[2];
			upperFilterString = args[3];
			xBinningString = args[4];
			yBinningString = args[4];
			calibrateBeforeString = args[5];
			calibrateAfterString = args[6];
		}
		else if(instID.equals("IRCAM"))
		{
			if(args.length != 6)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			irFilterString = args[2];
			xBinningString = args[3];
			yBinningString = args[3];
			calibrateBeforeString = args[4];
			calibrateAfterString = args[5];
		}
		else if(instID.equals("FIXEDSPEC"))
		{
			if(args.length != 6)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			xBinningString = args[2];
			yBinningString = args[3];
			calibrateBeforeString = args[4];
			calibrateAfterString = args[5];
		}
		else
		{
			System.out.println("Instrument ID must be one of RATCAM,IRCAM or FIXEDSPEC.");
			System.exit(1);
		}
		// convert binning strings to numbers
		xBinning = Integer.parseInt(xBinningString);
		yBinning = Integer.parseInt(yBinningString);
		// convert calibrate before and after strings to booleans
		b = Boolean.valueOf(calibrateBeforeString);
		calibrateBefore = b.booleanValue();
		b = Boolean.valueOf(calibrateAfterString);
		calibrateAfter = b.booleanValue();
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Instr");
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
		instr = new Instr();
		instr.setSessionData(sessionData);
		instr.setInstId(instID);
		instr.setLowerFilter(lowerFilterString);
		instr.setUpperFilter(upperFilterString);
		instr.setIRFilter(irFilterString);
		instr.setXBinning(xBinning);
		instr.setYBinning(yBinning);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful())
		{
			System.out.println("Instr successful.");
		}
		else
		{
			System.out.println("Instr failed:"+instr.getErrorString()+".");
		}
		System.out.println("Instr finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.2  2007/01/30 18:35:25  cjm
** gnuify: Added GNU General Public License.
**
** Revision 1.1  2005/06/06 14:45:16  cjm
** Initial revision
**
*/
