// Arc.java
// $Header$
package org.estar.toop;

import java.io.*;
import java.util.*;
import java.text.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Arc command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision$
 */
public class Arc extends TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id$";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "Arc";
	/**
	 * The command name.
	 */
	public static final String COMMAND_NAME = "ARC";
	/**
	 * The lamp to use for the arc.
	 */
	protected String lampName = null;
	/**
	 * Return value from the ARC command.
	 * A list of returned arc exposure image filenames.
	 */
	protected List filenameList = null;

	/**
	 * Default constructor.
	 */
	public Arc() 
	{
		super();
	}

	/**
	 * Set the name of the lamp used to take the ARC.
	 * @param s The lamp name.
	 * @see #lampName
	 */
	public void setLampName(String s)
	{
		lampName = s;
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
	 * @see #lampName
	 * @see #successful
	 * @see #errorString
	 * @see #filenameList
	 */
	public void run()
	{
		String filename = null;
		boolean done;
		int index;

		commandString = new String(COMMAND_NAME+" "+sessionData.getSessionId()+" "+lampName);

		super.run();
		// results
		if(getSuccessful())
		{
			try
			{
				// file<n> from n=1. 
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
			}
			catch(Exception e)
			{
				successful = false;
				errorString = new String(this.getClass().getName()+
							 ":run:Parsing ARC results failed:"+e);
				logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
				logger.dumpStack(1,e);
				return;
			}
		}
		else
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","Arc failed with error : "+getErrorString()+".");
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
	 * @return A FITS image filename. This filename exists on the occ / instrument machine, not the proxy machine.
	 * @see #filenameList
	 */
	public String getFilename(int i)
	{
		return (String)(filenameList.get(i));
	}

	/**
	 * Test main program.
	 * @param args The argument list.
	 */
	public static void main(String args[])
	{
		TOCSessionData sessionData = null;
		Arc arc = null;
		File inputPropertiesFile = null;
		Logger l = null;
		String lampName = null;
		
		if(args.length != 2)
		{
			System.out.println("java org.estar.toop.Arc <input properties filename> <lamp name>");
			System.exit(1);
		}
		inputPropertiesFile = new File(args[0]);
		lampName = args[1];
		// setup logger
		ConsoleLogHandler console = new ConsoleLogHandler(new BasicLogFormatter(150));
		console.setLogLevel(ALL);

		l = LogManager.getLogger("org.estar.toop.TOCAClient");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.TOCCommand");
		l.setLogLevel(ALL);	
		l.addHandler(console);
		l = LogManager.getLogger("org.estar.toop.Arc");
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
		arc = new Arc();
		arc.setSessionData(sessionData);
		arc.setLampName(lampName);
		arc.run();
		if(arc.getSuccessful())
		{
			for(int i = 0; i < arc.getFilenameCount(); i++)
			{
				System.out.println("Filename "+i+":"+arc.getFilename(i)+".");
			}
		}
		else
		{
			System.out.println("Arc failed:"+arc.getErrorString()+".");
		}
		System.out.println("Arc finished.");
		System.exit(0);
	}
}
