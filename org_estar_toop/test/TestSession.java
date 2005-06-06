// TestSession.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/test/TestSession.java,v 1.1 2005-06-06 17:46:42 cjm Exp $
package org.estar.toop.test;

import java.io.*;
import java.util.*;

import org.estar.astrometry.*;
import org.estar.toop.*;

/**
 * This class tests TOCSession.
 * @author Chris Mottram
 * @version $Revision: 1.1 $
 */
public class TestSession
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: TestSession.java,v 1.1 2005-06-06 17:46:42 cjm Exp $";
	/**
	 * The session reference.
	 */
        protected TOCSession session = null;
	/**
	 * The session data reference. 
	 */
	protected TOCSessionData sessionData = null;
	/**
	 * Filename of properties for loading into session data.
	 */
	protected String sessionDataFilename = null;
	/**
	 * Source ID for slew (FITS OBJECT name).
	 */
	protected String sourceID = null;
	/**
	 * RA.
	 */
	protected String raString = null;
	/**
	 * Dec.
	 */
	protected String decString = null;
	/**
	 * Lower filter type string.
	 */
	protected String lowerFilterType = null;
	/**
	 * Upper filter type string.
	 */
	protected String upperFilterType = null;
	/**
	 * Chip binning.
	 */
	protected int bin = 2;
	/**
	 * Exposure length.
	 */
	protected int exposureLength = 0;
	/**
	 * Exposure count.
	 */
	protected int exposureCount = 1;

	/**
	 * Default constructor.
	 * Construct session.
	 * Construct session data.
	 */
	public TestSession()
	{
		super();
		session = new TOCSession();
		sessionData = new TOCSessionData();
		session.setSessionData(sessionData);
	}

	/**
	 * Parse arguments.
	 * @exception NumberFormatException Thrown if numeric parsing fails.
	 * @see #sourceID
	 * @see #decString
	 * @see #raString
	 * @see #lowerFilterType
	 * @see #upperFilterType
	 * @see #bin
	 * @see #exposureLength
	 * @see #exposureCount
	 */
	public void parseArguments(String args[]) throws NumberFormatException
	{
		if(args.length < 1)
		{
			help();
			System.exit(0);
		}
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-bin"))
			{
				if((i+1) < args.length)
				{
					bin = Integer.parseInt(args[i+1]);
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:bin needs a integer argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-dec"))
			{
				if((i+1) < args.length)
				{
					decString = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:Dec needs a string argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-exposure_count"))
			{
				if((i+1) < args.length)
				{
					exposureCount = Integer.parseInt(args[i+1]);
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:exposure count needs an integer argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-exposure_length"))
			{
				if((i+1) < args.length)
				{
					exposureLength = Integer.parseInt(args[i+1]);
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:exposure length needs an integer argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-help"))
			{
				help();
				System.exit(0);
			}
			else if(args[i].equals("-lower_filter"))
			{
				if((i+1) < args.length)
				{
					lowerFilterType = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:lower filter needs a string argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-ra"))
			{
				if((i+1) < args.length)
				{
					raString = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:RA needs a string argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-source"))
			{
				if((i+1) < args.length)
				{
					sourceID = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:Source ID needs a string argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-session_data"))
			{
				if((i+1) < args.length)
				{
					sessionDataFilename = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:session data needs a filename argument.");
					System.exit(3);
				}
			}
			else if(args[i].equals("-upper_filter"))
			{
				if((i+1) < args.length)
				{
					upperFilterType = args[i+1];
					i++;
				}
				else
				{
					System.err.println(this.getClass().getName()+
							   ":parseArguments:upper filter needs a string argument.");
					System.exit(3);
				}
			}
			else
			{
				System.err.println(this.getClass().getName()+":parseArguments:Unknown Argument"+
						   args[i]);
				System.exit(1);
			}
		}
	}

	/**
	 * Prints out command line arguments.
	 */
	public void help()
	{
		System.out.println("java org.estar.toop.test.TestSession -session_data <property filename>");
		System.out.println("\t-source <object name> -ra <HH:MM:SS.ss> -dec <[+|-]DD:MM:SS.ss>");
		System.out.println("\t-exposure_length <milliseconds> -exposure_count <exposure count>");
		System.out.println("\t-lower_filter <filter type> -upper_filter <filter type> -bin <n> [-help]");
	}

	/**
	 * run method.
	 * @see #session
	 * @see #sourceID
	 * @see #raString
	 * @see #decString
	 * @see #lowerFilterType
	 * @see #upperFilterType
	 * @see #bin
	 * @see #exposureLength
	 * @see #exposureCount
	 */
	public void run() throws Exception
	{
		if(sourceID == null)
			throw new Exception("No source ID specified");
		if(raString == null)
			throw new Exception("No RA specified");
		if(decString == null)
			throw new Exception("No Declination specified");
		if(lowerFilterType == null)
			throw new Exception("No lower filter type specified");
		if(upperFilterType == null)
			throw new Exception("No upper filter type specified");
		if(exposureLength < 10)
			throw new Exception("No exposure length specified/ is too small:"+exposureLength);
		// load session data?
		if(sessionDataFilename != null)
			sessionData.load(new File(sessionDataFilename));
		try
		{
			session.helo();
			session.init();
			session.slew(sourceID,raString,decString);
			session.instrRatcam(lowerFilterType,upperFilterType,bin,false,false);
			session.expose(exposureLength,exposureCount,true);
			for(int i = 0;i < session.getExposeFilenameCount(); i++)
				System.out.println(""+i+" "+session.getExposeFilename(i));
		}
		catch(Exception e)
		{
			System.err.println(this.getClass().getName()+":run failed:"+e);
			e.printStackTrace();
		}
		finally
		{
			session.quit();
		}
	}

	/**
	 * main method of test program.
	 */
	public static void main(String args[])
	{
		TestSession testSession = null;

		try
		{
			testSession = new TestSession();
			testSession.parseArguments(args);
			testSession.run();
		}
		catch(Exception e)
		{
			System.err.println("TestSession:main:"+e);
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
/*
** $Log: not supported by cvs2svn $
*/
