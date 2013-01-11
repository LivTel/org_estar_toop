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
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Instr.java,v 1.7 2013-01-11 17:57:10 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Instr command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.7 $
 */
class Instr extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Instr.java,v 1.7 2013-01-11 17:57:10 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Instr";
	/**
	 * The command name.
	 */
	public final static String COMMAND_NAME = "INSTR";
	/**
	 * The maximum number of filters that can be specified.
	 */
	public final static int FILTER_LIST_COUNT = 3;
	/**
	 * The index in the list of the lower filter.
	 */
	public final static int LOWER_FILTER_INDEX = 0;
	/**
	 * The index in the list of the upper filter.
	 */
	public final static int UPPER_FILTER_INDEX = 1;
	/**
	 * The index in the list of the IR filter.
	 */
	public final static int IR_FILTER_INDEX = 0;
	/**
	 * The index in the list of a single filter. Used for IO:O at the moment.
	 */
	public final static int SINGLE_FILTER_INDEX = 0;
	/**
	 * The index in the filter list of the NUVSPEC central wavelength.
	 * Although not really a filter it is the NUVSPEC equivalent.
	 */
	public final static int NUVIEW_WAVELENGTH_FILTER_INDEX = 0;
	/**
	 * The instrument name - RATCAM, EA01, EM01, NUVSPEC, FIXEDSPEC, IRCAM, RINGOSTAR, GROPE, IO:O, RINGO3.
	 */
	protected String instID = null;
	/**
	 * A list of filters to configure the instrument.
	 */
	protected String filterList[] = new String[FILTER_LIST_COUNT];
	/**
	 * Input into the instr command, the x binning.
	 */
	protected int xBinning = 2;
	/**
	 * Input into the instr command, the y binning.
	 */
	protected int yBinning = 2;
	/**
	 * Input into the instr command, the trigger type. One of 'internal / 'external'. RINGO3 only.
	 */
	protected String triggerType = null;
	/**
	 * Input into the instr command, the EMGain. Number, usually 1,10 or 100. RINGO3/THOR only.
	 */
	protected int emGain = 0;
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
	 * Set the input lower filter to the INSTR command (for RATCAM/EA01 only).
	 * @param s A filter type, e.g. SDSS-R.
	 * @see #setFilter
	 * @see #LOWER_FILTER_INDEX
	 */
	public void setLowerFilter(String s)
	{
		setFilter(LOWER_FILTER_INDEX,s);
	}

	/**
	 * Set the input upper filter to the INSTR command (for RATCAM/EA01 only).
	 * @param s A filter type, e.g. SDSS-I, clear.
	 * @see #setFilter
	 * @see #UPPER_FILTER_INDEX
	 */
	public void setUpperFilter(String s)
	{
		setFilter(UPPER_FILTER_INDEX,s);
	}

	/**
	 * Set the input filter to the INSTR command (for single filter instruments only).
	 * @param s A filter type, e.g. 'R'.
	 * @see #setFilter
	 * @see #SINGLE_FILTER_INDEX
	 */
	public void setSingleFilter(String s)
	{
		setFilter(SINGLE_FILTER_INDEX,s);
	}

	/**
	 * Set the input filter to the INSTR command (for IRCAM only).
	 * @param s A filter type, e.g. Barr-J.
	 * @see #setFilter
	 * @see #IR_FILTER_INDEX
	 */
	public void setIRFilter(String s)
	{
		setFilter(IR_FILTER_INDEX,s);
	}

	/**
	 * Set the central wavelength of the INSTR command, when configuring NUVSPEC
	 * @param s The central wavelength, as a string, e.g "4690.2", "6182.6", "7291.6" 
	 *          (NUVSPEC standard wavelengths).
	 * @see #setFilter
	 * @see #NUVIEW_WAVELENGTH_FILTER_INDEX
	 */
	public void setWavelength(String s)
	{
		setFilter(NUVIEW_WAVELENGTH_FILTER_INDEX,s);
	}

	/**
	 * Set a filter to the INSTR command.
	 * @param index The index in the filter list of this filter.
	 * @param s A filter type.
	 * @exception IllegalArgumentException Thrown if index is out of range 0..FILTER_LIST_COUNT.
	 * @see #filterList
	 * @see #FILTER_LIST_COUNT
	 */
	public void setFilter(int index,String s) throws IllegalArgumentException
	{
		if((index < 0) || (index >= FILTER_LIST_COUNT))
		{
			throw new IllegalArgumentException(this.getClass().getName()+
							   ":setFilter:Illegal filter Index:"+index);
		}
		filterList[index] = s;
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
	 * Set the input trigger type to the INSTR command. Used by RINGO3.
	 * @param s The trigger type, one of 'internal' / 'external'.
	 * @see #triggerType
	 */
	public void setTriggerType(String s)
	{
		triggerType = s;
	}

	/**
	 * Set the input EM Gain to the INSTR command. Used by RINGO3/THOR.
	 * @param emGain The emGain. Usually 1, 10 or 100.
	 * @see #emGain
	 */
	public void setEMGain(int g)
	{
		emGain = g;
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
	 * @see #triggerType
	 * @see #emGain
	 * @see #calibrateBefore
	 * @see #calibrateAfter
	 * @see #filterList
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		// common start bits
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+instID+" ");
		if(instID.equals("RATCAM")||instID.equals("HAWKCAM")||instID.equals("EA01")||instID.equals("EA02"))
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
			commandString = new String(commandString+filterList[LOWER_FILTER_INDEX]+" "+
						   filterList[UPPER_FILTER_INDEX]+" "+xBinning);
		}
		else if(instID.equals("EM01")||instID.equals("EM02"))
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
			commandString = new String(commandString+filterList[0]+" "+filterList[1]+" "+filterList[2]+
						   " "+xBinning);
		}
		else if(instID.equals("RISE"))
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
			commandString = new String(commandString+xBinning);
		}
		else if(instID.equals("IO:O"))
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
			commandString = new String(commandString+filterList[SINGLE_FILTER_INDEX]+" "+xBinning);
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
			commandString = new String(commandString+filterList[IR_FILTER_INDEX]+" "+xBinning);
		}
		else if(instID.equals("FIXEDSPEC"))
		{
			commandString = new String(commandString+xBinning+" "+yBinning);
		}
		else if(instID.equals("NUVSPEC"))
		{
			commandString = new String(commandString+filterList[NUVIEW_WAVELENGTH_FILTER_INDEX]);
		}
		else if(instID.equals("RINGO")||instID.equals("RINGOSTAR")||instID.equals("GROPE"))
		{
			commandString = new String(commandString+xBinning+" "+yBinning);
		}
		else if(instID.equals("RINGO3"))
		{
			// INSTR <session id> RINGO3 <internal|external> <emgain> <xbin> <ybin>
			commandString = new String(commandString+triggerType+" "+emGain+" "+xBinning+" "+yBinning);
		}
		// diddly FRODOSPEC TODO
		else
		{
			successful = false;
			errorString = new String(this.getClass().getName()+
						 ":run:Unknown instrument: "+instID+".");
			logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
			return;
		}
		if(instID.equals("EM01") == false)// does not apply to Merope
		{
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
		}
		// run the INSTR command
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
		String singleFilterString = null;
		String triggerTypeString = null;
		String emGainString = null;
		String calibrateBeforeString = null;
		String calibrateAfterString = null;
		Boolean b = null;
		int xBinning,yBinning,emGain;
		boolean calibrateBefore,calibrateAfter;

		if(args.length < 2)
		{
			System.out.println("java org.estar.toop.Instr <input properties filename> <inst ID> [<lfilter> <ufilter> <bin>]|[<single filter> <bin>]|[<xbin> <ybin>]|[<trigger type> <emgain> <bin>] <calibrate before> <calibrate after>");
			System.out.println("Instrument ID must be one of RATCAM,IRCAM,IO:O, FIXEDSPEC, RINGO3.");
			System.out.println("The first set of optional parameters are for RATCAM, the second for IRCAM/IO:O and the third for FIXEDSPEC, the fourth for RINGO3.");
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
		else if(instID.equals("IO:O"))
		{
			if(args.length != 6)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			singleFilterString = args[2];
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
		else if(instID.equals("RINGO3"))
		{
			if(args.length != 7)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			triggerTypeString = args[2];
			emGainString = args[3];
			xBinningString = args[4];
			yBinningString = args[4];
			calibrateBeforeString = args[5];
			calibrateAfterString = args[6];
		}
		// diddly FrodoSpec
		else
		{
			System.out.println("Instrument ID must be one of RATCAM,IRCAM,IO:O or FIXEDSPEC.");
			System.exit(1);
		}
		// convert emGain strings into number
		emGain = Integer.parseInt(emGainString);
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
		if(lowerFilterString != null)
			instr.setLowerFilter(lowerFilterString);
		if(upperFilterString != null)
			instr.setUpperFilter(upperFilterString);
		if(irFilterString != null)
			instr.setIRFilter(irFilterString);
		if(singleFilterString != null)
			instr.setSingleFilter(singleFilterString);
		if(triggerTypeString != null)
			instr.setTriggerType(triggerTypeString);
		instr.setEMGain(emGain);
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
** Revision 1.6  2012/08/23 14:01:16  cjm
** Added IO:O support.
**
** Revision 1.5  2008/03/27 12:42:20  cjm
** Added RISE Instr and more aliases for RATCAM/HAWKCAM.
**
** Revision 1.4  2008/03/26 15:00:30  cjm
** Changed filter handling to be list based.
** Added Merope (EM01), EA01, and NUVSPEC implementations.
**
** Revision 1.3  2007/04/25 10:33:41  cjm
** Added RINGO instrument support.
**
** Revision 1.2  2007/01/30 18:35:25  cjm
** gnuify: Added GNU General Public License.
**
** Revision 1.1  2005/06/06 14:45:16  cjm
** Initial revision
**
*/
