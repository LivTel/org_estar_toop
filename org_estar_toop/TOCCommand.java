// TOCCommand.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCCommand.java,v 1.1 2005-06-06 14:44:55 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.util.*;
import ngat.util.logging.*;

/** 
 * TOCCommand, generic command implementation.
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.1 $
 */
class TOCCommand implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: TOCCommand.java,v 1.1 2005-06-06 14:44:55 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "TOCCommand";
	/**
	 * Class logger.
	 */
	protected Logger logger = null;
	/**
	 * Instance of the client. Used to send the command to the RCS TOCA.
	 */
	protected TOCAClient tocaClient = null;
	/**
	 * Instance of the TOC session data.
	 */
	protected TOCSessionData sessionData = null;
	/**
	 * The command string to send to the RCS TOCA using the TOCClient.
	 */
	protected String commandString = null;
	/**
	 * An error string generated if something went wrong.
	 */
	protected String errorString = null;
	/**
	 * Boolean set to whether the command was completed successfully or not.
	 */
	protected boolean successful = false;

	/**
	 * The logger instance is created. The tocaClient instance is initialised.
	 * @see #tocaClient
	 * @see #logger
	 */
	public TOCCommand() 
	{
		super();
		tocaClient = new TOCAClient();
		logger = LogManager.getLogger(this);
	}

	/**
	 * Set the session data.
	 * @param data The data.
	 * @see #sessionData
	 */
	public void setSessionData(TOCSessionData data)
	{
		sessionData = data;
	}

	/**
	 * Run method. Configures the instance of tocaClient using sessionData.
	 * Runs the client to communicate with the RCS TOCA and get the results.
	 * @see #tocaClient
	 */
	public void run()
	{
		String host = null;
		int port;

		logger.log(INFO, 1, CLASS, RCSID,"run","TOCCommand : Started.");
		try
		{
			port = sessionData.getTOCSPort();
		}
		catch(Exception e)
		{
			successful = false;
			errorString = new String(this.getClass().getName()+":run:Getting TOCS port failed:"+e);
			logger.log(INFO, 1, CLASS, RCSID,"run",errorString);
			logger.dumpStack(1,e);
			return;
		}
		tocaClient.setCommand(commandString);
		tocaClient.setHost(sessionData.getTOCSHost());
		tocaClient.setPort(port);
		logger.log(INFO, 1, CLASS, RCSID,"run","TOCCommand : Calling ("+sessionData.getTOCSHost()+","+port+
			   ") with command "+commandString+".");
		tocaClient.run();
		successful = (tocaClient.isError() == false);
		if(tocaClient.isError())
		{
			errorString = tocaClient.getErrorCode()+":"+tocaClient.getErrorMessage();
		}
	}

	/**
	 * Get the reply error string generated by this command.
	 * @return The reply string.
	 * @see #tocaClient
	 */
	public String getReplyString()
	{
		return tocaClient.getReply();
	}

	/**
	 * Returns the reply string value for the specified reply keyword, 
	 * if it exists in the list of reply keyword values.
	 * @param keyword The keyword.
	 * @return The reply keyword's string value. If the keyword does not exist, null is returned.
	 * @see #tocaClient
	 * @see TOCAClient#getReplyValue
	 */
	public String getReplyValue(String keyword)
	{
		return tocaClient.getReplyValue(keyword);
	}

	/**
	 * Returns the reply int value for the specified reply keyword, 
	 * if it exists in the list of reply keyword values.
	 * @param keyword The keyword.
	 * @return The reply keyword's int value. 
	 * @exception NGATPropertyException Thrown if the value is not a valid integer.
	 * @see #tocaClient
	 * @see TOCAClient#getReplyValueInt
	 */
	public int getReplyValueInt(String keyword) throws NGATPropertyException
	{
		return tocaClient.getReplyValueInt(keyword);
	}

	/**
	 * Returns the reply double value for the specified reply keyword, 
	 * if it exists in the list of reply keyword values.
	 * @param keyword The keyword.
	 * @return The reply keyword's double value. 
	 * @exception NGATPropertyException Thrown if the value is not a valid double.
	 * @see #tocaClient
	 * @see TOCAClient#getReplyValueDouble
	 */
	public double getReplyValueDouble(String keyword) throws NGATPropertyException
	{
		return tocaClient.getReplyValueDouble(keyword);
	}

	/**
	 * Get any error string generated by this command.
	 * @return The error string.
	 * @see #errorString
	 */
	public String getErrorString()
	{
		return errorString;
	}

	/**
	 * Get whether the command was completed successfully or not.
	 * @return A boolean, true if the command was completed successfully, flase if not.
	 * @see #successful
	 */
	public boolean getSuccessful()
	{
		return successful;
	}
}
/*
** $Log: not supported by cvs2svn $
*/
