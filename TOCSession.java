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
// TOCSession.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCSession.java,v 1.19 2013-06-03 10:31:07 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.phase2.OConfig;
import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Helper class for a target of oppurtunity control session.
 * Should be called as follows:
 * <pre>
 * TOCSession ts = new TOCSession();
 * ts.loadSessionData("toop.properties");
 * ts.helo();
 * ts.init();
 * ts.slew("a-star","01:02:03","+45:56:01");
 * ts.focalPlane("IO:O");
 * ts.instrIOO({"","SDSS-R","clear","clear"},2,false,false);
 * ts.expose(10000,1,true);
 * for(int i = 0;i < ts.getExposeFilenameCount(); i++)
 *     System.out.println(""+i+" "+ts.getExposeFilename(i));
 * ts.stop();
 * ts.quit();
 * </pre>
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
public class TOCSession implements Logging
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "TOCSession";
	/**
	 * Acquire Mode: None.
	 * @see Acquire
	 */
	public static final String ACQUIRE_MODE_NONE = "NONE";
	/**
	 * Acquire Mode: Brightest.
	 * @see Acquire
	 */
	public static final String ACQUIRE_MODE_BRIGHTEST = "BRIGHTEST";
	/**
	 * Acquire Mode: WCS.
	 * @see Acquire
	 */
	public static final String ACQUIRE_MODE_WCS = "WCS";
	/**
	 * Class logger.
	 */
	private Logger logger = null;
	/**
	 * Reference to the session data.
	 */
	private TOCSessionData sessionData = null;
	/**
	 * When reference.
	 */
	private When when  = null;
	/**
	 * Position reference.
	 */
	private Position position = null;
	/**
	 * Status reference.
	 */
	private Status status = null;
	/**
	 * Helo reference.
	 */
	private Helo helo = null;
	/**
	 * Init reference.
	 */
	private Init init = null;
	/**
	 * Slew reference.
	 */
	private Slew slew = null;
	/**
	 * Offset reference.
	 */
	private Offset offset = null;
	/**
	 * Auto reference.
	 */
	private Auto auto = null;
	/**
	 * AgRadial reference.
	 */
	private AgRadial agRadial = null;
	/**
	 * Acquire reference.
	 */
	private Acquire acquire = null;
	/**
	 * FocalPlane reference.
	 */
	private FocalPlane focalPlane = null;
	/**
	 * Instr reference.
	 */
	private Instr instr = null;
	/**
	 * Expose reference.
	 */
	private Expose expose = null;
	/**
	 * Arc reference.
	 */
	private Arc arc = null;
	/**
	 * Stop reference.
	 */
	private Stop stop = null;
	/**
	 * Quit reference.
	 */
	private Quit quit = null;

	/**
	 * The logger instance is created. The command implementor references are created.
	 * @see #logger
	 * @see #when
	 * @see #position
	 * @see #status
	 * @see #helo
	 * @see #init
	 * @see #slew
	 * @see #offset
	 * @see #auto
	 * @see #agRadial
	 * @see #acquire
	 * @see #focalPlane
	 * @see #instr
	 * @see #expose
	 * @see #arc
	 * @see #stop
	 * @see #quit
	 */
	public TOCSession() 
	{
		super();
		logger = LogManager.getLogger(this);
		when = new When();
		position  = new Position();
		status = new Status();
		helo = new Helo();
		slew = new Slew();
		init = new Init();
		slew = new Slew();
		offset = new Offset();
		auto = new Auto();
		agRadial = new AgRadial();
		acquire = new Acquire();
		focalPlane = new FocalPlane();
		instr = new Instr();
		expose = new Expose();
		arc = new Arc();
		stop = new Stop();
		quit = new Quit();
	}

	/**
	 * Set the session data. All the command implementor's session data is set.
	 * @param d The data to set.
	 * @see #sessionData
	 * @see #when
	 * @see #position
	 * @see #status
	 * @see #helo
	 * @see #init
	 * @see #slew
	 * @see #offset
	 * @see #auto
	 * @see #agRadial
	 * @see #acquire
	 * @see #focalPlane
	 * @see #instr
	 * @see #expose
	 * @see #arc
	 * @see #stop
	 * @see #quit
	 */
	public void setSessionData(TOCSessionData d)
	{
		sessionData = d;
		when.setSessionData(sessionData);
		position.setSessionData(sessionData);
		status.setSessionData(sessionData);
		helo.setSessionData(sessionData);
		init.setSessionData(sessionData);
		slew.setSessionData(sessionData);
		offset.setSessionData(sessionData);
		auto.setSessionData(sessionData);
		agRadial.setSessionData(sessionData);
		acquire.setSessionData(sessionData);
		focalPlane.setSessionData(sessionData);
		instr.setSessionData(sessionData);
		expose.setSessionData(sessionData);
		arc.setSessionData(sessionData);
		stop.setSessionData(sessionData);
		quit.setSessionData(sessionData);
	}

	/**
	 * Load session data from a file.
	 * @param f The Java properties file containing the session data to load.
	 * @exception FileNotFoundException Thrown if the file doesn't exist.
	 * @exception IOException Thrown if the load failed.
	 * @see #sessionData
	 * @see #setSessionData
	 */
	public void loadSessionData(File f) throws FileNotFoundException, IOException
	{
		sessionData = new TOCSessionData();
		sessionData.load(f);
		// set sessiondata to itself! (Also sets all command implementors session data)
		setSessionData(sessionData);
	}


	/**
	 * Find out when a helo command will next succeed.
	 * You do not have to call the helo command before this one.
	 * @return The number of seconds until a HELO comamnd with the session Data's service ID will succeed.
	 * @exception TOCException Thrown if the helo command fails.
	 * @see #when
	 * @see #sessionData
	 */
	public int when() throws TOCException
	{
		when.run();
		if(when.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":when failed:"+when.getErrorString());
		}
		return when.getTime();
	}

	/**
	 * Find out information about the position of the specified target.
	 * You do not have to call the helo command before this one.
	 * Further information from the results of this command can be got using getPosition.
	 * @param ra The right ascension of the target.
	 * @param dec The declination of the target.
	 * @return A string, whether the target is "RISEN" or "SET".
	 * @exception TOCException Thrown if the position command fails.
	 * @see #getPosition
	 * @see #position
	 * @see #sessionData
	 */
	public String position(RA ra,Dec dec) throws TOCException
	{
		position.setRA(ra);
		position.setDec(dec);
		position.run();
		if(position.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":position failed:"+position.getErrorString());
		}
		return position.getState();
	}

	/**
	 * Find out information about the telescope status.
	 * You do not have to call the helo command before this one.
	 * Further information from the results of this command can be got using getStatus.
	 * @param category The status category e.g. METEO.
	 * @param keyword The status keyword e.g. humidity.
	 * @return A string, containing the value of the status e.g. "0.74".
	 * @exception TOCException Thrown if the status command fails.
	 * @see #getStatus
	 * @see #status
	 * @see #sessionData
	 */
	public String status(String category,String keyword) throws TOCException
	{
		status.setCategory(category);
		status.setKeyword(keyword);
		status.run();
		if(status.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":status failed:"+status.getErrorString());
		}
		return status.getValue();
	}

	/**
	 * Start a connection to the RCS TOCA.
	 * You must have set or loaded the session data before calling this method.
	 * @exception TOCException Thrown if the helo command fails.
	 * @see #helo
	 */
	public void helo() throws TOCException
	{
		helo.run();
		if(helo.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":helo failed:"+helo.getErrorString());
		}
	}

	/**
	 * Initialise the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> before this method. 
	 * @exception TOCException Thrown if the init command fails.
	 * @see #init
	 */
	public void init() throws TOCException
	{
		init.run();
		if(init.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":init failed:"+init.getErrorString());
		}
	}

	/**
	 * Slew the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> before this method. 
	 * @param sourceId A string representing the source, ends up in the <i>OBJECT</i> keyword in the FITS headers.
	 * @param ra The right ascension to slew to.
	 * @param dec The declination to slew to.
	 * @exception TOCException Thrown if the slew command fails.
	 * @see #slew
	 */
	public void slew(String sourceId,RA ra,Dec dec) throws TOCException
	{
		slew.setSourceId(sourceId);
		slew.setRA(ra);
		slew.setDec(dec);
		slew.run();
		if(slew.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":slew failed:"+slew.getErrorString());
		}
	}

	/**
	 * Slew the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> before this method. 
	 * @param sourceId A string representing the source, ends up in the <i>OBJECT</i> keyword in the FITS headers.
	 * @param raString A string representing the right ascension, in the format HH:MM:SS.ss.
	 * @param decString A string representing the declination, in the format [+|-]DD:MM:SS.ss.
	 * @exception TOCException Thrown if the slew command fails.
	 * @exception NumberFormatException Thrown if the RA/Dec parsing fails.
	 * @see #slew
	 */
	public void slew(String sourceId,String raString,String decString) throws TOCException, NumberFormatException
	{
		slew.setSourceId(sourceId);
		slew.setRA(raString);
		slew.setDec(decString);
		slew.run();
		if(slew.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":slew failed:"+slew.getErrorString());
		}
	}

	/**
	 * Offset the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> and <b>slew</b> before this method. 
	 * @param dRA The right ascension offset in arcseconds.
	 * @param dDec The declination offset in arcseconds.
	 * @exception TOCException Thrown if the offset command fails.
	 * @see #offset
	 */
	public void offset(double dRA, double dDec) throws TOCException
	{
		offset.setDRA(dRA);
		offset.setDDec(dDec);
		offset.run();
		if(offset.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":offset failed:"+offset.getErrorString());
		}
	}

	/**
	 * Switch the autoguider on or off after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> and <b>slew</b> before this method. 
	 * @param on A boolean, if "true" turn the autoguider on, otherwise turn it off.
	 * @exception TOCException Thrown if the auto command fails.
	 * @see #auto
	 */
	public void auto(boolean on) throws TOCException
	{
		if(on)
			auto.setOn();
		else
			auto.setOff();
		auto.run();
		if(auto.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":auto failed:"+auto.getErrorString());
		}
	}

	/**
	 * Move the autoguider pick-off mirror to a specified position from the field edge.
	 * You should have called <b>helo</b> before this method. 
	 * @param d The position from the edge of the field, in mm.
	 * @exception TOCException Thrown if the agradial command fails.
	 * @see #agRadial
	 */
	public void agradial(double d) throws TOCException
	{
		agRadial.setPosition(d);
		agRadial.run();
		if(agRadial.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":agradial failed:"+agRadial.getErrorString());
		}
	}

	/**
	 * Acquire the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> before this method. 
	 * @param ra The right ascension to acquire to.
	 * @param dec The declination to acquire to.
	 * @param acquireMode Which method to use to acquire.
	 * @exception TOCException Thrown if the slew command fails.
	 * @see #acquire
	 * @see #ACQUIRE_MODE_NONE
	 * @see #ACQUIRE_MODE_BRIGHTEST
	 * @see #ACQUIRE_MODE_WCS
	 */
	public void acquire(RA ra,Dec dec,String acquireMode) throws TOCException
	{
		acquire.setRA(ra);
		acquire.setDec(dec);
		acquire.setAcquireMode(acquireMode);
		acquire.run();
		if(acquire.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":acquire failed:"+acquire.getErrorString());
		}
	}

	/**
	 * Acquire the telescope after starting a RCS TOCA session using helo.
	 * You should have called <b>helo</b> before this method. 
	 * @param raString A string representing the right ascension, in the format HH:MM:SS.ss.
	 * @param decString A string representing the declination, in the format [+|-]DD:MM:SS.ss.
	 * @param acquireMode Which method to use to acquire.
	 * @exception TOCException Thrown if the slew command fails.
	 * @exception NumberFormatException Thrown if the RA/Dec parsing fails.
	 * @see #acquire
	 * @see #ACQUIRE_MODE_NONE
	 * @see #ACQUIRE_MODE_BRIGHTEST
	 * @see #ACQUIRE_MODE_WCS
	 */
	public void acquire(String raString,String decString,String acquireMode) throws TOCException, 
											NumberFormatException
	{
		acquire.setRA(raString);
		acquire.setDec(decString);
		acquire.setAcquireMode(acquireMode);
		acquire.run();
		if(acquire.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":acquire failed:"+acquire.getErrorString());
		}
	}

	/**
	 * Configure the focal plane (telescope aperture offset) for an instrument.
	 * You should have called <b>helo</b> before this method. 
	 * @param instrumentName The name of the instrument.
	 * @exception TOCException Thrown if the focal plane command fails.
	 * @see #focalPlane
	 */
	public void focalPlane(String instrumentName) throws TOCException
	{
		focalPlane.setInstrumentName(instrumentName);
		focalPlane.run();
		if(focalPlane.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":focal plane failed:"+
					       focalPlane.getErrorString());
		}
	}

	/**
	 * Configure an instrument, and make it the current TOCA instrument.
	 * You should have called <b>helo</b> before this method. 
	 * @param instID The ID of the instrument.
	 * @param filter0 A string representing a filter type string, i.e. SDSS-R. Used for RATCAM lower filter,
	 *                IR filter type, IO:O filter type, NUVSPEC wavelength (string), EM01 filter 0.
	 * @param filter1 A string representing a filter type string, i.e. clear. Used for RATCAM upper filter, 
	 *                EM01 filter 1.
	 * @param filter2 A string representing a filter type string, i.e. clear. Used for EM01 filter 2.
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually false.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually false.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instr(String instID,String filter0,String filter1,String filter2,
			  int xBin,int yBin,boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId(instID);
		instr.setFilter(0,filter0);
		instr.setFilter(1,filter1);
		instr.setFilter(2,filter2);
		instr.setXBinning(xBin);
		instr.setYBinning(yBin);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	/**
	 * Configure the RATCAM instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param lowerFilterType A string representing the lower filter type string, i.e. SDSS-R.
	 * @param upperFilterType A string representing the upper filter type string, i.e. clear.
	 * @param bin How to bin the chip, usually use 2.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually false.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually false.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instrImager
	 */
	public void instrRatcam(String lowerFilterType,String upperFilterType,int bin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instrImager("RATCAM",lowerFilterType,upperFilterType,bin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure a standard 2 wheel imager instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param instrumentName Which particular instrument name to use (RATCAM, EA01).
	 * @param lowerFilterType A string representing the lower filter type string, i.e. SDSS-R.
	 * @param upperFilterType A string representing the upper filter type string, i.e. clear.
	 * @param bin How to bin the chip, usually use 2.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually false.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually false.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrImager(String instrumentName, String lowerFilterType,String upperFilterType,int bin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr(instrumentName,lowerFilterType,upperFilterType,null,
		      bin,bin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure a Merope 3 wheel imager instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param instrumentName Which particular instrument name to use (EM01,...).
	 * @param filter0 A string representing the filter type string in wheel 0, i.e. SDSS-R.
	 * @param filter1 A string representing the filter type string in wheel 1, i.e. air.
	 * @param filter2 A string representing the filter type string in wheel 2, i.e. air.
	 * @param bin How to bin the chip, usually use 2.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrMerope(String instrumentName,String filter0,String filter1,String filter2,int bin) 
		throws TOCException
	{
		instr(instrumentName,filter0,filter1,filter2,bin,bin,false,false);
	}

	/**
	 * Configure the RISE instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param bin How to bin the chip, usually use 1.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually true.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually true.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrRISE(int bin,boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("RISE",null,null,null,bin,bin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure the IO:O instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param filterTypeList An array of strings representing the filter type strings, i.e. 'R'.
	 *        There should be three for IO:O but starting from index 1 i.e. 1 = filter wheel, 
	 *        2 = lower filter slide, 3 = upper filter slide. There fore the array should have a length of
	 *        at least 4.
	 * @param bin How to bin the chip, usually use 1.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually true.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually true.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrIOO(String filterTypeList[],int bin,
			     boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		if(filterTypeList.length < OConfig.O_FILTER_INDEX_COUNT)
		{
			throw new TOCException(this.getClass().getName()+
					       ":instr failed:filterTypeList length was too short:"+
					       filterTypeList.length+" vs "+OConfig.O_FILTER_INDEX_COUNT);
		}
		instr.setInstId("IO:O");
		for(int i = OConfig.O_FILTER_INDEX_FILTER_WHEEL;
		    i <= OConfig.O_FILTER_INDEX_FILTER_SLIDE_UPPER; i++)
		{
			instr.setFilter(i,filterTypeList[i]);
		}
		instr.setXBinning(bin);
		instr.setYBinning(bin);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	/**
	 * Configure the IRCAM instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param filterType A string representing the filter type string, i.e. Barr-J.
	 * @param bin How to bin the chip, usually use 1.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually true.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually true.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrIRcam(String filterType,int bin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("IRCAM",filterType,null,null,bin,bin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure the LIRIC instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param filterType A string representing the filter type string, i.e. Barr-H.
	 * @param nudgematicOffsetSize A string describing the nudgematic offset size to use, one of : 'small','large',
	 *        'none'.
	 * @param coaddExposureLength An integer describing the coadd exposure length to use in milliseconds, 
	 *        either 100 or 1000.
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrLiric(String filterType,String nudgematicOffsetSize,int coaddExposureLength,int xBin,int yBin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId("LIRIC");
		instr.setSingleFilter(filterType);
		instr.setNudgematicOffsetSize(nudgematicOffsetSize);
		instr.setCoaddExposureLength(coaddExposureLength);
		instr.setXBinning(xBin);
		instr.setYBinning(yBin);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	/**
	 * Configure the FIXEDSPEC instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrFixedSpec(int xBin,int yBin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("FIXEDSPEC",null,null,null,xBin,yBin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure the NUVSPEC (Meaburn) instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param wavelengthString The central wavelength of the spectra to take usually one of: 
	 *       "4690.2", "6182.6", "7291.6".
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrMeaburnSpec(String wavelengthString,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("NUVSPEC",wavelengthString,null,null,1,1,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure the RINGOSTAR instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrRingoStar(int xBin,int yBin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("RINGO",null,null,null,xBin,yBin,calibrateBefore,calibrateAfter);
	}

	/**
	 * Configure the RINGO3 instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param triggerType A string describing what trigger type to use, one of : 'internal','external'.
	 * @param emGain The EMGain to use, an integer, usually 1,10, or 100.
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrRingo3(String triggerType,int emGain,int xBin,int yBin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId("RINGO3");
		instr.setFilter(0,null);
		instr.setFilter(1,null);
		instr.setFilter(2,null);
		instr.setTriggerType(triggerType);
		instr.setEMGain(emGain);
		instr.setXBinning(xBin);
		instr.setYBinning(yBin);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	/**
	 * Configure the MOPTOP instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param filterType A string representing the filter type string, i.e. MOP-R.
	 * @param rotorSpeed A string describing the rotor speed to use, one of : 'slow','fast'.
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrMoptop(String filterType,String rotorSpeed,int xBin,int yBin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId("MOPTOP");
		instr.setSingleFilter(filterType);
		instr.setRotorSpeed(rotorSpeed);
		instr.setXBinning(xBin);
		instr.setYBinning(yBin);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	/**
	 * Configure the IO:THOR instrument, and make it the current TOCA instrument
	 * You should have called <b>helo</b> before this method. 
	 * @param emGain The EMGain to use, an integer, usually 1,10, or 100.
	 * @param bin How to bin the chip in X and Y.
	 * @param xStart The start position of the window in X, in pixels.
	 * @param yStart The start position of the window in Y, in pixels.
	 * @param xEnd The end position of the window in X, in pixels.
	 * @param yEnd The end position of the window in Y, in pixels.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instrIOTHOR(int emGain,int bin,int xStart,int yStart,int xEnd,int yEnd,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId("IO:THOR");
		instr.setFilter(0,null);
		instr.setFilter(1,null);
		instr.setFilter(2,null);
		instr.setEMGain(emGain);
		instr.setXBinning(bin);
		instr.setYBinning(bin);
		instr.setWindow(xStart,yStart,xEnd,yEnd);
		instr.setCalibrateBefore(calibrateBefore);
		instr.setCalibrateAfter(calibrateAfter);
		instr.run();
		if(instr.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":instr failed:"+instr.getErrorString());
		}
	}

	// diddly instrFrodospec FRODOSPEC TODO

	/**
	 * Take a MULTRUN exposure.
	 * You should have called <b>helo</b>, <b>slew</b> and <b>instr</b> before this method. 
	 * @param exposureLength The length of each exposure in milliseconds
	 * @param exposureCount How many exposure frames to do.
	 * @param dataPipelineFlag Whether to call the data pipeline.
	 * @exception TOCException Thrown if the expose command fails.
	 * @see #expose
	 */
	public void expose(int exposureLength,int exposureCount,
				boolean dataPipelineFlag) throws TOCException
	{
		expose.setExposureLength(exposureLength);
		expose.setExposureCount(exposureCount);
		expose.setRunatDate(null);
		expose.setDataPipelineFlag(dataPipelineFlag);
		expose.run();
		if(expose.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":expose failed:"+expose.getErrorString());
		}
	}

	/**
	 * Take a RUNAT exposure.
	 * You should have called <b>helo</b>, <b>slew</b> and <b>instr</b> before this method. 
	 * @param exposureLength The length of each exposure in milliseconds
	 * @param date The date/time to open the shutter.
	 * @param dataPipelineFlag Whether to call the data pipeline.
	 * @exception TOCException Thrown if the expose command fails.
	 * @see #expose
	 */
	public void expose(int exposureLength,Date date,
				boolean dataPipelineFlag) throws TOCException
	{
		expose.setExposureLength(exposureLength);
		expose.setExposureCount(1);
		expose.setRunatDate(date);
		expose.setDataPipelineFlag(dataPipelineFlag);
		expose.run();
		if(expose.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":expose failed:"+expose.getErrorString());
		}
	}

	/**
	 * Take an Arc with the currently selected instrument.
	 * You should have called <b>helo</b>, <b>slew</b> and <b>instr</b> before this method. 
	 * @param lampName The name of the lamp to use.
	 * @exception TOCException Thrown if the arc command fails.
	 * @see #arc
	 */
	public void arc(String lampName) throws TOCException
	{
		arc.setLampName(lampName);
		arc.run();
		if(arc.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":arc failed:"+arc.getErrorString());
		}
	}

	/**
	 * Stop the telescope, if it is slewing/tracking.
	 * You should have called <b>helo</b> before this method. 
	 * @exception TOCException Thrown if the stop command fails.
	 * @see #stop
	 */
	public void stop() throws TOCException
	{
		stop.run();
		if(stop.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":stop failed:"+stop.getErrorString());
		}
	}

	/**
	 * Quit a RCS TOCA session.
	 * You should have called <b>helo</b> before this method. 
	 * @exception TOCException Thrown if the quit command fails.
	 * @see #quit
	 */
	public void quit() throws TOCException
	{
		quit.run();
		if(quit.getSuccessful() == false)
		{
			throw new TOCException(this.getClass().getName()+":quit failed:"+quit.getErrorString());
		}
	}

	/**
	 * Get the session data currently in use by this session, and all it's command implementors.
	 * @return The instance of session data.
	 * @see #sessionData
	 */
	public TOCSessionData getSessionData()
	{
		return sessionData;
	}

	/**
	 * Get the helo command implementor.
	 * @see #helo
	 */
	public Helo getHelo()
	{
		return helo;
	}

	/**
	 * Get the position command implementor.
	 * @see #position
	 */
	public Position getPosition()
	{
		return position;
	}

	/**
	 * Get the when command implementor.
	 * @see #when
	 */
	public When getWhen()
	{
		return when;
	}

	/**
	 * Get the status command implementor.
	 * @see #status
	 */
	public Status getStatus()
	{
		return status;
	}

	/**
	 * Get the expose command implementor.
	 * @see #expose
	 */
	public Expose getExpose()
	{
		return expose;
	}

	/**
	 * Get the number of filenames generated by the last expose command.
	 * @return The number of filenames, or 0 if no expose command has occured yet.
	 * @see #expose
	 */
	public int getExposeFilenameCount()
	{
		return expose.getFilenameCount();
	}

	/**
	 * Get the ith filenames generated by the last expose command.
	 * @param i The index in the list of filenames.
	 * @return The filename (on the RCS machine (occ)).
	 * @see #expose
	 */
	public String getExposeFilename(int i)
	{
		return expose.getFilename(i);
	}

	/**
	 * Get the number of filenames generated by the last arc command.
	 * @return The number of filenames, or 0 if no arc command has occured yet.
	 * @see #arc
	 */
	public int getArcFilenameCount()
	{
		return arc.getFilenameCount();
	}

	/**
	 * Get the ith filenames generated by the last arc command.
	 * @param i The index in the list of filenames.
	 * @return The filename (on the RCS machine (occ)).
	 * @see #arc
	 */
	public String getArcFilename(int i)
	{
		return arc.getFilename(i);
	}

	/**
	 * Initialise org.estar.toop loggers. A static method - no session has to be instantiated to call this.
	 * @param handler The log handler to point the loggers to.
	 * @param logLevel The log level to set the loggers to.
	 */
	public static void initLoggers(LogHandler handler,int logLevel)
	{
		Logger l = null;

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Acquire");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.AgRadial");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Arc");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Auto");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Expose");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.FocalPlane");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Helo");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Init");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Instr");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Offset");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Position");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Quit");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Slew");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Status");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Stop");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.When");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.TOCSessionData");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.TOCSession");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.18  2013/01/17 14:17:02  cjm
** Added instrIOTHOR.
**
** Revision 1.17  2013/01/11 17:57:01  cjm
** Added Ringo3 support.
**
** Revision 1.16  2012/08/23 14:00:09  cjm
** Added instrIOO.
**
** Revision 1.15  2008/03/28 16:49:32  cjm
** Fixed comments.
**
** Revision 1.14  2008/03/28 16:47:11  cjm
** Moved ACQUIRE_MODE_ constants from Acquire, which is a package only class.
**
** Revision 1.13  2008/03/28 15:51:23  cjm
** Fixed docs.
**
** Revision 1.12  2008/03/27 19:44:39  cjm
** Added Acquire command for spectrograph acquisition.
**
** Revision 1.11  2008/03/27 12:50:59  cjm
** Rewrote instr command, now has 3 generic filters.
** Added instrImager to specify inst name.
** Added instrMerope,instrRISE,instrMeaburnSpec.
**
** Revision 1.10  2007/04/25 10:34:06  cjm
** Added instrRingoStar method for RINGO instrument support.
**
** Revision 1.9  2007/01/30 18:35:34  cjm
** gnuify: Added GNU General Public License.
**
** Revision 1.8  2005/06/15 14:35:30  cjm
** Changed initLoggers to static.
**
** Revision 1.7  2005/06/08 18:09:11  cjm
** Added agradial method.
**
** Revision 1.6  2005/06/08 17:53:35  cjm
** Added auto method.
**
** Revision 1.5  2005/06/08 16:16:10  cjm
** Added offset method.
**
** Revision 1.4  2005/06/08 15:45:11  cjm
** Added stop method.
**
** Revision 1.3  2005/06/07 17:51:54  cjm
** Added status.
**
** Revision 1.2  2005/06/07 13:34:32  cjm
** Added initLoggers.
**
** Revision 1.1  2005/06/06 17:46:33  cjm
** Initial revision
**
*/
