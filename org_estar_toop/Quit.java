// Quit.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Quit.java,v 1.1 2005-06-06 14:45:08 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Quit command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Quit extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Quit.java,v 1.1 2005-06-06 14:45:08 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Quit";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "QUIT";

	/**
	 * Default constructor.
	 */
	public Quit() 
	{
		super();
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
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId());
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Quit successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Quit failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Quit quit = null;
		File inputPropertiesFile = null;
		Logger l = null;

		if(args.length != 1)
		{
			System.out.println("java org.estar.toop.Quit <input properties filename>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Quit");
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
		quit = new Quit();
		quit.setSessionData(sessionData);
		quit.run();
		if(quit.getSuccessful())
		{
			System.out.println("Quit successful.");
		}
		else
		{
			System.out.println("Quit failed:"+quit.getErrorString()+".");
		}
		System.out.println("Quit finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
*/
