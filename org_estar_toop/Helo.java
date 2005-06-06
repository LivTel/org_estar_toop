// Helo.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/Helo.java,v 1.1 2005-06-06 14:45:21 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Helo command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class Helo extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: Helo.java,v 1.1 2005-06-06 14:45:21 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Helo";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "HELO";
	/**
	 * The allocated session ID.
	 */
	protected String sessionID = null;
	/**
	 * The number of seconds allocated for this session.
	 */
	protected int sessionLimit = 0;
	/**
	 * The number of seconds remaining in this allocation period.
	 */
	protected int timeRemaining = 0;
	/**
	 * The priority allocated to this session.
	 */
	protected int priority = 0;

	/**
	 * Default constructor.
	 */
	public Helo() 
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
	 * @see #sessionID
	 * @see #sessionLimit
	 * @see #timeRemaining
	 * @see #priority
	 * @see #logger
	 * @see #successful
	 * @see #errorString
	 */
	public void run()
	{
		commandString = new String(COMMAND_NAME+" "+sessionData.getServiceId());
		super.run();
		// results
		if(getSuccessful())
		{
			try
			{
				// session ID
				sessionID = getReplyValue("sessionID");
				sessionData.setSessionId(sessionID);
				// session Limit
				sessionLimit = getReplyValueInt("sessionLimit");
				sessionData.setProperty(".session_limit",""+sessionLimit);
				// time Remaining
				timeRemaining = getReplyValueInt("timeRemaining");
				sessionData.setProperty(".time_remaining",""+timeRemaining);
				// priority
				priority = getReplyValueInt("priority");
				sessionData.setProperty(".priority",""+priority);
				logger.log(INFO, 1, CLASS, RCSID,"run","Helo successful with sessionID : "+sessionID+
					   " sessionLimit : "+sessionLimit+" timeRemaining : "+timeRemaining+
					   " priority : "+priority+".");
			}
			catch(NGATPropertyException e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing HELO results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Helo failed with error : "+getErrorString()+".");
		}
	}

	/**
	 * Return the session ID created for this session.
	 * @return The session ID created for this session.
	 * @see #sessionID
	 */
	public String getSessionID()
	{
		return sessionID;
	}

	/**
	 * Return the number of seconds allocated for this override.
	 * @return A time in seconds.
	 * @see #sessionLimit
	 */
	public int getSessionLimit()
	{
		return sessionLimit;
	}
	
	/**
	 * Return the total number of seconds remaining for this service agent in the current accounting period.
	 * @return A time in seconds.
	 * @see #timeRemaining
	 */
	public int getTimeRemaining()
	{
		return timeRemaining;
	}
	
	/**
	 * Return this service id's priority.
	 * @return A priority.
	 * @see #priority
	 */
	public int getPriority()
	{
		return priority;
	}
	
	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Helo helo = null;
		File inputPropertiesFile = null;
		File outputPropertiesFile = null;
		Logger l = null;

		if(args.length != 2)
		{
			System.out.println("java org.estar.toop.Helo <input properties filename> <output properties filename>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		outputPropertiesFile = new File(args[1]);
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Helo");
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
		helo = new Helo();
		helo.setSessionData(sessionData);
		helo.run();
		if(helo.getSuccessful())
		{
			System.out.println("Session ID:"+helo.getSessionID()+".");
			System.out.println("Session Limit:"+helo.getSessionLimit()+" seconds.");
			System.out.println("Time Remaining:"+helo.getTimeRemaining()+" seconds.");
			System.out.println("Priority:"+helo.getPriority()+".");
		}
		else
		{
			System.out.println("Helo failed:"+helo.getErrorString()+".");
		}
		try
		{
			sessionData.save(outputPropertiesFile);
		}
		catch(Exception e)
		{
			System.err.println("Saving session data failed:"+e);
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Helo finished.");
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
*/
