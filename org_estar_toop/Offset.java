// Offset.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Offset.java,v 1.1 2005-06-08 16:06:18 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Offset command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Offset extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Offset.java,v 1.1 2005-06-08 16:06:18 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Offset";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "OFFSET";
	/**
	 * Input into the offset command, the Right Ascension offset in arcseconds.
	 */
	protected double dRA = 0.0;
	/**
	 * Input into the offset command, the Declination offset in arcseconds.
	 */
	protected double dDec = 0.0;

	/**
	 * Default constructor.
	 */
	public Offset() 
	{
		super();
	}

	/**
	 * Set the delta Right Ascension, an input into the OFFSET command.
	 * @param d The Right Ascension offset in arcseconds.
	 * @see #dRA
	 */
	public void setDRA(double d)
	{
		dRA = d;
	}

	/**
	 * Set the delta Declination, an input into the OFFSET command.
	 * @param d The Declination offset in arcseconds.
	 * @see #dDec
	 */
	public void setDDec(double d)
	{
		dDec = d;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #dRA
	 * @see #dDec
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+dRA+" "+dDec);
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Offset successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Offset failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Offset offset = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String dRAString = null;
		String dDecString = null;
		double dRA, dDec;

		if(args.length != 3)
		{
			System.out.println("java org.estar.toop.Offset <input properties filename> <dRA> <dDec>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		dRAString = args[1];
		dDecString = args[2];
		// parse offsets
		dRA = Double.parseDouble(dRAString);
		dDec = Double.parseDouble(dDecString);
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Offset");
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
		offset = new Offset();
		offset.setSessionData(sessionData);
		offset.setDRA(dRA);
		offset.setDDec(dDec);
		offset.run();
		if(offset.getSuccessful())
		{
			System.out.println("Offset successful.");
		}
		else
		{
			System.out.println("Offset failed:"+offset.getErrorString()+".");
		}
		System.out.println("Offset finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/06 14:45:04  cjm
** Initial revision
**
*/
