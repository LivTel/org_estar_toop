// Init.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Init.java,v 1.1 2005-06-06 14:45:19 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Init command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Init extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Init.java,v 1.1 2005-06-06 14:45:19 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Init";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "INIT";

	/**
	 * Default constructor.
	 */
	public Init() 
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
		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+
					   " "+sessionData.getInitRotatorOption()+
					   " "+sessionData.getInitFocusOption()+
					   " "+sessionData.getInitAGOption());
		super.run();
		// results
		if(getSuccessful())
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Init successful.");
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Init failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Init init = null;
		File inputPropertiesFile = null;
		Logger l = null;

		if(args.length != 1)
		{
			System.out.println("java org.estar.toop.Init <input properties filename>");
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
		l = LogManager.getLogger("org.estar.toop.Init");
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
		init = new Init();
		init.setSessionData(sessionData);
		init.run();
		if(init.getSuccessful())
		{
			System.out.println("Init successful.");
		}
		else
		{
			System.out.println("Init failed:"+init.getErrorString()+".");
		}
		System.out.println("Init finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
*/
