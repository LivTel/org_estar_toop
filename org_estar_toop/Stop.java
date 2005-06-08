// Stop.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Stop.java,v 1.1 2005-06-08 15:46:12 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Stop command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Stop extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Stop.java,v 1.1 2005-06-08 15:46:12 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Stop";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "STOP";

	/**
	 * Default constructor.
	 */
	public Stop() 
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
			logger.log(INFO, 1, CLASS, RCSID,"run","Stop successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Stop failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Stop stop = null;
		File inputPropertiesFile = null;
		Logger l = null;

		if(args.length != 1)
		{
			System.out.println("java org.estar.toop.Stop <input properties filename>");
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
		l = LogManager.getLogger("org.estar.toop.Stop");
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
		stop = new Stop();
		stop.setSessionData(sessionData);
		stop.run();
		if(stop.getSuccessful())
		{
			System.out.println("Stop successful.");
		}
		else
		{
			System.out.println("Stop failed:"+stop.getErrorString()+".");
		}
		System.out.println("Stop finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.1  2005/06/06 14:45:08  cjm
** Initial revision
**
*/
