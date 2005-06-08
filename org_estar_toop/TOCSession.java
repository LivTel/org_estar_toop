// TOCSession.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCSession.java,v 1.4 2005-06-08 15:45:11 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

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
 * ts.instrRatcam("SDSS-R","clear",2,false,false);
 * ts.expose(10000,1,true);
 * for(int i = 0;i < ts.getExposeFilenameCount(); i++)
 *     System.out.println(""+i+" "+ts.getExposeFilename(i));
 * ts.stop();
 * ts.quit();
 * </pre>
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.4 $
 */
public class TOCSession implements Logging
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: TOCSession.java,v 1.4 2005-06-08 15:45:11 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "TOCSession";
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
	 * Instr reference.
	 */
	private Instr instr = null;
	/**
	 * Expose reference.
	 */
	private Expose expose = null;
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
	 * @see #instr
	 * @see #expose
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
		instr = new Instr();
		expose = new Expose();
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
	 * @see #instr
	 * @see #expose
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
		slew.setSessionData(sessionData);
		init.setSessionData(sessionData);
		slew.setSessionData(sessionData);
		instr.setSessionData(sessionData);
		expose.setSessionData(sessionData);
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
	 * Initialise org.estar.toop loggers.
	 * @param handler The log handler to point the loggers to.
	 * @param logLevel The log level to set the loggers to.
	 */
	public void initLoggers(LogHandler handler,int logLevel)
	{
		Logger l = null;

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Expose");
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
		l = LogManager.getLogger("org.estar.toop.Position");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Quit");
		l.setLogLevel(logLevel);	
		l.addHandler(handler);
		l = LogManager.getLogger("org.estar.toop.Slew");
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
	 * Configure an instrument, and make it the current TOCA instrument.
	 * You should have called <b>helo</b> before this method. 
	 * @param instID The ID of the instrument.
	 * @param lowerFilterType A string representing the lower filter type string, i.e. SDSS-R.
	 * @param upperFilterType A string representing the upper filter type string, i.e. clear.
	 * @param irFilterType A string representing the IR type string, i.e. Barr-J.
	 * @param xBin How to bin the chip in X.
	 * @param yBin How to bin the chip in Y.
	 * @param calibrateBefore Whether to do calibration frames before using this configuration, usually false.
	 * @param calibrateAfter Whether to do calibration frames after using this configuration, usually false.
	 * @exception TOCException Thrown if the instr command fails.
	 * @see #instr
	 */
	public void instr(String instID,String lowerFilterType,String upperFilterType,String irFilterType,
			  int xBin,int yBin,boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr.setInstId(instID);
		instr.setLowerFilter(lowerFilterType);
		instr.setUpperFilter(upperFilterType);
		instr.setIRFilter(irFilterType);
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
	 * @see #instr
	 */
	public void instrRatcam(String lowerFilterType,String upperFilterType,int bin,
				boolean calibrateBefore,boolean calibrateAfter) throws TOCException
	{
		instr("RATCAM",lowerFilterType,upperFilterType,null,
		      bin,bin,calibrateBefore,calibrateAfter);
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
		instr("IRCAM",null,null,filterType,
		      bin,bin,calibrateBefore,calibrateAfter);
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
}
/*
** $Log: not supported by cvs2svn $
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
