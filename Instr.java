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
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Instr.java,v 1.10 2013-06-03 10:32:35 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.phase2.OConfig;
import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Instr command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
class Instr extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Instr";
	/**
	 * The command name.
	 */
	public final static String COMMAND_NAME = "INSTR";
	/**
	 * The maximum number of filters that can be specified. This is now 4, as IO:O requires 3
	 * filters, but there are indexed starting at position 1.
	 */
	public final static int FILTER_LIST_COUNT = 4;
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
	 * The index in the list of a single filter. Used for IO:O and MOPTOP.
	 */
	public final static int SINGLE_FILTER_INDEX = 0;
	/**
	 * The index in the filter list of the NUVSPEC central wavelength.
	 * Although not really a filter it is the NUVSPEC equivalent.
	 */
	public final static int NUVIEW_WAVELENGTH_FILTER_INDEX = 0;
	/**
	 * The instrument name - RATCAM, EA01, EM01, NUVSPEC, FIXEDSPEC, IRCAM, RINGOSTAR, GROPE, IO:O, RINGO3, IO:THOR, MOPTOP.
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
	 * Input into the instr command, the trigger type. One of 'internal' / 'external'. RINGO3 only.
	 */
	protected String triggerType = null;
	/**
	 * Input into the instr command, the rotator speed. One of 'slow' / 'fast'. MOPTOP only.
	 */
	protected String rotorSpeed = null;
	/**
	 * Input into the instr command, the EMGain. Number, usually 1,10 or 100. RINGO3/IO:THOR only.
	 */
	protected int emGain = 0;
	/**
	 * Input into the instr command, a window. Used for THOR only.
	 * @see #window
	 */
	protected InstrWindow window = new InstrWindow();
	/**
	 * Input into the instr command, whether to do calibration before any exposures using this configuration.
	 */
	protected boolean calibrateBefore = false;
	/**
	 * Input into the instr command, whether to do calibration after any exposures using this configuration.
	 */
	protected boolean calibrateAfter = false;

	/**
	 * Default constructor. A new window is constructed.
	 * @see #window
	 */
	public Instr() 
	{
		super();
		window = new InstrWindow();
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
	 * Set the rotor speed to the INSTR command. Used by MOPTOP.
	 * @param rs The rotor speed, one of 'slow' / 'fast'.
	 * @see #rotorSpeed
	 */
	public void setRotorSpeed(String rs)
	{
		rotorSpeed = rs;
	}

	/**
	 * Set the input EM Gain to the INSTR command. Used by RINGO3/THOR.
	 * @param g The emGain. Usually 1, 10 or 100.
	 * @see #emGain
	 */
	public void setEMGain(int g)
	{
		emGain = g;
	}

	/**
	 * Set the input window o the INSTR command. Used by THOR.
	 * @param xStart The X Start position of the window, in pixels.
	 * @param yStart The Y Start position of the window, in pixels.
	 * @param xEnd The X End position of the window, in pixels.
	 * @param yEnd The Y End position of the window, in pixels.
	 * @see #window
	 */
	public void setWindow(int xStart,int yStart,int xEnd,int yEnd)
	{
		window.setXStart(xStart);
		window.setYStart(yStart);
		window.setXEnd(xEnd);
		window.setYEnd(yEnd);
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
	 * @see #rotorSpeed
	 * @see #emGain
	 * @see #window
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
			commandString = new String(commandString+filterList[OConfig.O_FILTER_INDEX_FILTER_WHEEL]+" "+
						   filterList[OConfig.O_FILTER_INDEX_FILTER_SLIDE_LOWER]+" "+
						   filterList[OConfig.O_FILTER_INDEX_FILTER_SLIDE_UPPER]+" "+
						   xBinning);
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
		else if(instID.equals("MOPTOP"))
		{
			// INSTR <session id> MOPTOP <rotorSpeed> <filter> <xbin> <ybin>
			commandString = new String(commandString+rotorSpeed+" "+filterList[SINGLE_FILTER_INDEX]+" "+
						   xBinning+" "+yBinning);
		}
		else if(instID.equals("IO:THOR"))
		{
			// INSTR <sessionId> IO:THOR <emgain> <binxy> <xs> <xe> <ys> <ye> 
			commandString = new String(commandString+emGain+" "+xBinning+" "+window.getXStart()+" "+
						   window.getXEnd()+" "+window.getYStart()+" "+window.getYEnd());

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
		String rotorSpeedString = null;
		String emGainString = null;
		InstrWindow win = null;
		String calibrateBeforeString = null;
		String calibrateAfterString = null;
		Boolean b = null;
		int xBinning,yBinning,emGain=0;
		boolean calibrateBefore,calibrateAfter;

		if(args.length < 2)
		{
			System.out.println("java org.estar.toop.Instr <input properties filename> <inst ID> [<lfilter> <ufilter> <bin>]|[<single filter> <bin>]|[<xbin> <ybin>]|[<trigger type> <emgain> <bin>][<emgain> <bin> <xs> <ys> <xe> <ye>][<rotorSpeed> <filter> <xbin> <ybin>] <calibrate before> <calibrate after>");
			System.out.println("Instrument ID must be one of RATCAM,IRCAM,IO:O, FIXEDSPEC, RINGO3, IO:THOR, MOPTOP.");
			System.out.println("Additional parameters for RATCAM : - [<lfilter> <ufilter> <bin>].");
			System.out.println("Additional parameters for IRCAM/IO:O : - [<single filter> <bin>].");
			System.out.println("Additional parameters for FIXEDSPEC : - [<xbin> <ybin>].");
			System.out.println("Additional parameters for RINGO3 : - [<trigger type> <emgain> <bin>].");
			System.out.println("Additional parameters for IO:THOR : - <emgain> <bin> <xs> <ys> <xe> <ye>].");
			System.out.println("Additional parameters for MOPTOP : - [<rotorSpeed> <filter> <bin>].");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		instID = args[1];
		if(instID.equals("RATCAM"))
		{
			// <lfilter> <ufilter> <bin>
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
			// <single filter> <bin>
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
			// <single filter> <bin>
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
			// <xbin> <ybin>
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
			// <trigger type> <emgain> <bin>
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
		else if(instID.equals("IO:THOR"))
		{
			// <emgain> <bin> <xs> <ys> <xe> <ye>
			if(args.length != 10)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			win = new InstrWindow();
			emGainString = args[2];
			xBinningString = args[3];
			yBinningString = args[3];
			win.setXStart(args[4]);
			win.setYStart(args[5]);
			win.setXEnd(args[6]);
			win.setYEnd(args[7]);
			calibrateBeforeString = args[8];
			calibrateAfterString = args[9];
		}
		else if(instID.equals("MOPTOP"))
		{
			// <rotorSpeed> <filter> <bin>
			if(args.length != 7)
			{
				System.err.println("Wrong number of arguments: "+args.length+".");
				System.exit(1);
			}
			rotorSpeedString = args[2];
			singleFilterString = args[3];
			xBinningString = args[4];
			yBinningString = args[4];
			calibrateBeforeString = args[5];
			calibrateAfterString = args[6];
		}
		// diddly FrodoSpec
		else
		{
			System.out.println("Instrument ID must be one of RATCAM,IRCAM,IO:O, FIXEDSPEC, RINGO3, IO:THOR, MOPTOP.");
			System.exit(1);
		}
		// convert emGain strings into number
		if(emGainString != null)
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
		if(rotorSpeedString != null)
			instr.setRotorSpeed(rotorSpeedString);
		instr.setEMGain(emGain);
		instr.setXBinning(xBinning);
		instr.setYBinning(yBinning);
		if(win != null)
			instr.setWindow(win.getXStart(),win.getYStart(),win.getXEnd(),win.getYEnd());
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

	/**
	 * Inner class defining a Window object, to store a sub-area
	 * of the CCD for windowing configuration. This is currently used only by
	 * IO:THOR.
	 */
	public static class InstrWindow
	{
		/** 
		 * The X axis position of the top/left corner of the window.
		 */
		protected int xStart;
		/** 
		 * The X axis position of the bottom/right corner of the window.
		 */
		protected int xEnd;
		/** 
		 * The Y axis position of the top/left corner of the window.
		 */
 		protected int yStart;
		/**
		 *The Y axis position of the bottom/right corner of the window.
		 */
		protected int yEnd;

		/**
		 * Default constructor.
		 */
		public InstrWindow()
		{
			this(0,0,0,0);
		}

		/**
		 * Constructor.
		 * @param xs The X Start position of the window, in pixels.
		 * @param ys The Y Start position of the window, in pixels.
		 * @param xe The X End position of the window, in pixels.
		 * @param ye The Y End position of the window, in pixels.
		 */
		public InstrWindow(int xs,int ys,int xe, int ye)
		{
			this.xStart = xs;
			this.yStart = ys;
			this.xEnd = xe;
			this.yEnd = ye;
		}

		/**
		 * Retrieve the X Start position of this window.
		 * @return An integer, the X Start position of this window in pixels.
		 * @see #xStart
		 */
		public int getXStart()
		{
			return xStart;
		}

		/**
		 * Retrieve the Y Start position of this window.
		 * @return An integer, the Y Start position of this window in pixels.
		 * @see #yStart
		 */
		public int getYStart()
		{
			return yStart;
		}

		/**
		 * Retrieve the X End position of this window.
		 * @return An integer, the X End position of this window in pixels.
		 * @see #xEnd
		 */
		public int getXEnd()
		{
			return xEnd;
		}

		/**
		 * Retrieve the Y End position of this window.
		 * @return An integer, the Y End position of this window in pixels.
		 * @see #yEnd
		 */
		public int getYEnd()
		{
			return yEnd;
		}

		/**
		 * Set the X Start position of this window.
		 * @param xs An integer, the X Start position of this window in pixels.
		 * @see #xStart
		 */
		public void setXStart(int xs)
		{
			xStart = xs;
		}

		/**
		 * Set the X Start position of this window.
		 * @param s A string, resresenting an integer,the X Start position of this window in pixels.
		 * @see #xStart
		 */
		public void setXStart(String s) throws NumberFormatException
		{
			xStart = Integer.parseInt(s);
		}

		/**
		 * Set the Y Start position of this window.
		 * @param ys An integer, the Y Start position of this window in pixels.
		 * @see #yStart
		 */
		public void setYStart(int ys)
		{
			yStart = ys;
		}

		/**
		 * Set the Y Start position of this window.
		 * @param s A string, resresenting an integer,the Y Start position of this window in pixels.
		 * @see #yStart
		 */
		public void setYStart(String s) throws NumberFormatException
		{
			yStart = Integer.parseInt(s);
		}

		/**
		 * Set the X End position of this window.
		 * @param xe An integer, the X End position of this window in pixels.
		 * @see #xEnd
		 */
		public void setXEnd(int xe)
		{
			xEnd = xe;
		}

		/**
		 * Set the X End position of this window.
		 * @param s A string, resresenting an integer,the X End position of this window in pixels.
		 * @see #xEnd
		 */
		public void setXEnd(String s) throws NumberFormatException
		{
			xEnd = Integer.parseInt(s);
		}

		/**
		 * Set the Y End position of this window.
		 * @param ye An integer, the Y End position of this window in pixels.
		 * @see #yEnd
		 */
		public void setYEnd(int ye)
		{
			yEnd = ye;
		}

		/**
		 * Set the Y End position of this window.
		 * @param s A string, resresenting an integer,the Y End position of this window in pixels.
		 * @see #yEnd
		 */
		public void setYEnd(String s) throws NumberFormatException
		{
			yEnd = Integer.parseInt(s);
		}

		/**
		 * Get the width of the window, in pixels. This assumes the width is exclusive not inclusive.
		 * @return An integer, the width of the window.
		 * @see #xEnd
		 * @see #xStart
		 */
		public int getWidth()
		{
			return xEnd - xStart;
		}

		/**
		 * Get the height of the window, in pixels. This assumes the height is exclusive not inclusive.
		 * @return An integer, the height of the window.
		 * @see #yEnd
		 * @see #yStart
		 */
		public int getHeight()
		{
			return yEnd - yStart;
		}

		/**
		 * Return a string representation of this window, of the form:
		 * InstrWindow(xStart = n,yStart = n,xEnd = n,yEnd = n).
		 * @return A string.
		 * @see #toString(java.lang.String)
		 */
		public String toString()
		{
			return toString("");
		}

		/**
		 * Return a string representation of this window, of the form:
		 * InstrWindow(xStart = n,yStart = n,xEnd = n,yEnd = n).
		 * @param prefix A string to prepend to the string.
		 * @return A string.
		 * @see #xStart
		 * @see #yStart
		 * @see #xEnd
		 * @see #yEnd
		 */
		public String toString(String prefix)
		{
			StringBuffer sb = null;

			sb = new StringBuffer();
			sb.append(prefix+"InstrWindow(");
			sb.append("xStart = "+xStart+",");
			sb.append("yStart = "+yStart+",");
			sb.append("xEnd = "+xEnd+",");
			sb.append("yEnd = "+yEnd+")");
			return sb.toString();
		}
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.9  2013/01/17 14:17:17  cjm
** Added IO:THOR support.
**
** Revision 1.8  2013/01/11 18:34:22  cjm
** Comment fix.
**
** Revision 1.7  2013/01/11 17:57:10  cjm
** Added Ringo3 support.
**
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
