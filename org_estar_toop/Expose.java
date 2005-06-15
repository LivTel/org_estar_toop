// Expose.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Expose.java,v 1.4 2005-06-15 16:56:18 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;
import java.text.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Expose command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.4 $
 */
public class Expose extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Expose.java,v 1.4 2005-06-15 16:56:18 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Expose";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "EXPOSE";
	/**
	 * The exposure length in milliseconds.
	 */
	protected int exposureLength = 0;
	/**
	 * The number of exposures to do. This is specified only if runatDate is <b>NOT</b> specified.
	 * @see #runatDate
	 */
	protected int exposureCount = 0;
	/**
	 * Whether to call the data pipeline or not.
	 */
	protected boolean dataPipelineFlag = false;
	/**
	 * The time to start the exposure. This is an alternative to exposureCount.
	 * @see #exposureCount
	 */
	protected Date runatDate = null;
	/**
	 * Return value from the EXPOSE command.
	 * A list of returned exposure image filenames.
	 */
	protected List filenameList = null;
	/**
	 * Return value from the EXPOSE command.
	 * The seeing in arcseconds.
	 */
	protected double seeing = 0.0;
	/**
	 * Return value from the EXPOSE command.
	 * The integrated counts from the brightest object in the field, in ADU.
	 */
	protected int counts = 0;
	/**
	 * Return value from the EXPOSE command.
	 * Photometricity from the DpRt, in magnitudes of extinction (standard fields only).
	 */
	protected double photometric = 0.0;
	/**
	 * Return value from the EXPOSE command.
	 * Sky brightness from the DpRt, in magnitudes per square arcsecond.
	 */
	protected double skyBrightness = 0.0;
	/**
	 * Return value from the EXPOSE command.
	 * The x pixel of the brightest object in the field, in pixels.
	 */
	protected double xPix = 0.0;
	/**
	 * Return value from the EXPOSE command.
	 * The y pixel of the brightest object in the field, in pixels.
	 */
	protected double yPix = 0.0;

	/**
	 * Default constructor.
	 */
	public Expose() 
	{
		super();
	}

	/**
	 * Set the exposure length.
	 * @param i The exposure length in milliseconds.
	 * @see #exposureLength
	 */
	public void setExposureLength(int i)
	{
		exposureLength = i;
	}

	/**
	 * Set the number of exposures to do of the specified length (MULTRUN count).
	 * Set <b>either</b> this or the runat date.
	 * @param i The number of exposures.
	 * @see #exposureLength
	 */
	public void setExposureCount(int i)
	{
		exposureCount = i;
	}

	/**
	 * Set whether to call the data pipeline for these exposures.
	 * @param b A boolean, if true call the data pipeline, else don't.
	 * @see #dataPipelineFlag
	 */
	public void setDataPipelineFlag(boolean b)
	{
		dataPipelineFlag = b;
	}

	/**
	 * Set the exposure start date.
	 * Set <b>either</b> this or the exposure count - RUNATs can only have 1 exposure.
	 * @param d A date, the time the shutter should open.
	 * @see #runatDate
	 */
	public void setRunatDate(Date d)
	{
		runatDate = d;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #sessionData
	 * @see #exposureLength
	 * @see #exposureCount
	 * @see #dataPipelineFlag
	 * @see #runatDate
	 * @see #filenameList
	 * @see #seeing
	 * @see #counts
	 * @see #photometric
	 * @see #skyBrightness
	 * @see #xPix
	 * @see #yPix
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		DateFormat df = null;
		String filename = null;
		boolean done;
		int index;

		if(runatDate == null)
		{
			commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+
						   " "+exposureLength+
						   " "+exposureCount);
		}
		else
		{
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+
						   " "+exposureLength+
						   " "+df.format(runatDate));
		}
		// postpend data pipeline flag
		if(dataPipelineFlag)
			commandString = new String(commandString+" T");
		else
			commandString = new String(commandString+" F");
		super.run();
		// results
		if(getSuccessful())
		{
			try
			{
				// file<n> from n=1. Usually exposureCount results - but not always! (RUNAT)
				filenameList = new Vector();
				index = 1;// file<n> starts from n=1
				done = false;
				while(done == false)
				{
					filename = getReplyValue("file"+index);
					if(filename != null)
					{
						filenameList.add(filename);
						index++;
					}
					else
					{
						done = true;
					}
				}
				// seeing
				seeing = getReplyValueDouble("seeing");
				sessionData.setProperty(".expose.seeing",""+seeing);
				// counts
				counts = getReplyValueInt("counts");
				sessionData.setProperty(".expose.counts",""+counts);
				// photometric
				photometric = getReplyValueDouble("photom");
				sessionData.setProperty(".expose.photometric",""+photometric);
				// skyBrightness
				skyBrightness = getReplyValueDouble("skybright");
				sessionData.setProperty(".expose.sky_brightness",""+skyBrightness);
				// xPix
				xPix = getReplyValueDouble("xpix");
				sessionData.setProperty(".expose.xpix",""+xPix);
				// yPix
				yPix = getReplyValueDouble("ypix");
				sessionData.setProperty(".expose.ypix",""+yPix);
				// logging
				logger.log(INFO, 1, CLASS, RCSID,"run","Expose successful with seeing : "+seeing+
					   ", counts : "+counts+", photometric : "+photometric+
					   ", xpix : "+xPix+", ypix : "+yPix+".");
			}
			catch(NGATPropertyException e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing EXPOSE results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Expose failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Return the number of FITS image filenames returned from the EXPOSE command.
	 * @return The number of filenames. If filenameList is null, 0 is returned.
	 * @see #filenameList
	 */
	public int getFilenameCount()
	{
		if(filenameList == null)
			return 0;
		return filenameList.size();
	}

	/**
	 * Return one of the FITS image filenames returned from the EXPOSE command.
	 * NB It is possible for an IndexOutOfBoundsException or ClassCastException to occur.
	 * @param i The index in the list of the filename to return. 
	 * @return A FITS image filename. This filename exists on the <b>occ</b> machine, not the proxy machine.
	 * @see #filenameList
	 */
	public String getFilename(int i)
	{
		return (String)(filenameList.get(i));
	}

	/**
	 * Return the DpRt calculated seeing from the frames taken.
	 * @return The seeing in arcseconds.
	 * @see #seeing
	 */
	public double getSeeing()
	{
		return seeing;
	}

	/**
	 * Return the DpRt calculated integrated counts from the brightest object in the frame.
	 * @return The integrated counts of the brightest object in ADU.
	 * @see #counts
	 */
	public int getCounts()
	{
		return counts;
	}

	/**
	 * Return the DpRt calculated photometric flag, the number of magnitudes of extinction for this field.
	 * This normally returns -999.0, it only returns the magnitudes of extinction for standard fields.
	 * @return A number of magnitudes of extinction (for standard fields), or -999.0.
	 * @see #photometric
	 */
	public double getPhotometric()
	{
		return photometric;
	}

	/**
	 * Return the DpRt calculated sky brightness, the number of magnitudes per square arcsecond for this field.
	 * This normally returns 99.0.
	 * @return A number of magnitudes per square arcsecond, or 99.0.
	 * @see #skyBrightness
	 */
	public double getSkyBrightness()
	{
		return skyBrightness;
	}

	/**
	 * Return the DpRt calculated x pixel location of the brightest object in the field.
	 * @return The x pixel position in pixels.
	 * @see #xPix
	 */
	public double getXPix()
	{
		return xPix;
	}

	/**
	 * Return the DpRt calculated y pixel location of the brightest object in the field.
	 * @return The y pixel position in pixels.
	 * @see #yPix
	 */
	public double getYPix()
	{
		return yPix;
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Expose expose = null;
		File inputPropertiesFile = null;
		Logger l = null;
		DateFormat df = null;
		Boolean b = null;
		Date runatDate = null;
		int exposureLength = 0;
		int exposureCount = 0;
		boolean dataPipelineFlag;

		if(args.length != 4)
		{
			System.out.println("java org.estar.toop.Expose <input properties filename> <exposure length ms> <<exposure count>|<runat date>> <data pipeline flag>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		exposureLength = Integer.parseInt(args[1]);
		if(args[2].indexOf("T") > -1)
		{
			try
			{
				df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				runatDate = df.parse(args[2]);
			}
			catch(Exception e)
			{
				System.err.println("Parsing runat date "+args[2]+" failed:"+e);
				e.printStackTrace();
				System.exit(1);
			}
		}
		else
		{
			exposureCount = Integer.parseInt(args[2]);
		}
		b = Boolean.valueOf(args[3]);
		dataPipelineFlag = b.booleanValue();
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Expose");
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
		expose = new Expose();
		expose.setSessionData(sessionData);
		expose.setExposureLength(exposureLength);
		expose.setExposureCount(exposureCount);
		expose.setRunatDate(runatDate);
		expose.setDataPipelineFlag(dataPipelineFlag);
		expose.run();
		if(expose.getSuccessful())
		{
			for(int i = 0; i < expose.getFilenameCount(); i++)
			{
				System.out.println("Filename "+i+":"+expose.getFilename(i)+".");
			}
			System.out.println("Seeing:"+expose.getSeeing()+" arc-seconds.");
			System.out.println("Counts:"+expose.getCounts()+" ADU.");
			System.out.println("Position:"+expose.getXPix()+","+expose.getYPix()+".");
			System.out.println("Photometricity:"+expose.getPhotometric()+" magnitudes.");
			System.out.println("Sky Brightness:"+expose.getSkyBrightness()+" magnitudes per square arc-second.");
		}
		else
		{
			System.out.println("Expose failed:"+expose.getErrorString()+".");
		}
		System.out.println("Expose finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.3  2005/06/07 13:27:58  cjm
** Comment fix.
**
** Revision 1.2  2005/06/06 17:46:56  cjm
** Comment fix.
**
** Revision 1.1  2005/06/06 16:01:13  cjm
** Initial revision
**
*/
