// Auto.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Auto.java,v 1.1 2005-06-08 17:53:42 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

import org.estar.astrometry.*;

/** 
 * Auto command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Auto extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Auto.java,v 1.1 2005-06-08 17:53:42 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Auto";
	/**
	 * Option string value, to turn the autoguider on.
	 */
	public static final String OPTION_ON = "ON";
	/**
	 * Option string value, to turn the autoguider off.
	 */
	public static final String OPTION_OFF = "OFF";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "AUTO";
	/**
	 * Input into the auto command, whether to turn it on or off.
	 * Should be set to "ON" or "OFF".
	 * @see #OPTION_ON
	 * @see #OPTION_OFF
	 */
	protected String optionString = null;

	/**
	 * Default constructor.
	 */
	public Auto() 
	{
		super();
	}

	/**
	 * Set the autoguider option to be ON.
	 * @see #optionString
	 * @see #OPTION_ON
	 */
	public void setOn()
	{
		optionString = OPTION_ON;
	}

	/**
	 * Set the autoguider option to be OFF.
	 * @see #optionString
	 * @see #OPTION_OFF
	 */
	public void setOff()
	{
		optionString = OPTION_OFF;
	}

	/**
	 * Run method. 
	 * Setup commandString.
	 * Call TOCCommand.run.
	 * If successful, look at the reply keyword/value pairs and extract the relevant data, updating the
	 *    sessionData as necessary.
	 * @see #COMMAND_NAME
	 * @see #commandString
	 * @see #optionString
	 * @see #sessionData
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+optionString);
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Auto successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Auto failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Auto auto = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String optionString = null;

		if(args.length != 2)
		{
			System.out.println("java org.estar.toop.Auto <input properties filename> <on|off>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		optionString = args[1];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Auto");
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
		auto = new Auto();
		auto.setSessionData(sessionData);
		if(optionString.equals("on"))
			auto.setOn();
		else if(optionString.equals("off"))
			auto.setOff();
		else
		{
			System.err.println("Illegal option string "+optionString+" : should be <on|off>.");
			System.exit(1);
		}
		auto.run();
		if(auto.getSuccessful())
		{
			System.out.println("Auto successful.");
		}
		else
		{
			System.out.println("Auto failed:"+auto.getErrorString()+".");
		}
		System.out.println("Auto finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/08 16:06:18  cjm
** Initial revision
**
** Revision 1.1  2005/06/06 14:45:04  cjm
** Initial revision
**
*/
