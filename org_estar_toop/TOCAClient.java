// TOCAClient.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCAClient.java,v 1.3 2005-06-07 16:08:40 cjm Exp $
package org.estar.toop;

import java.io.*;
import java.util.*;

import ngat.net.*;
import ngat.util.*;
import ngat.util.logging.*;

/** 
 * Handles responses to commands sent via "Target of Opportunity Control Protocol" (TOCP).
 * @author Steve Fraser, Chris Mottram
 * @version $Revision: 1.3 $
 */
class TOCAClient implements Logging, Runnable
{
	/**
	 * Revision control system version id.
	 */
	public final static String RCSID = "$Id: TOCAClient.java,v 1.3 2005-06-07 16:08:40 cjm Exp $";
	/**
	 * Classname for logging.
	 */
	public static final String CLASS = "TOCAClient";
	/**
	 * True if the command generated an error.
	 */
	private volatile boolean error = false;    
	/**
	 * TOCS Server IP Address.
	 */
	private String host;    
	/**
	 * TOCS Server port.
	 */
	private int port;
	/**
	 * Command to send.
	 */
	private String command;
	/**
	 * Error message from TOCS server.
	 */
	private String errorMessage = null;
	/**
	 * Error code string from TOCS server.
	 */
	private String errorCode = null;
	/**
	 * Response from TOCS server.
	 */
	private String replyString;
	/**
	 * The TelnetConnection to use to connect to the TOCS server.
	 */
	private TelnetConnection tc;
	/**
	 * Class logger.
	 */
	private Logger logger = null;
	/**
	 * Properties filled with keyword-values parsed from a reply from the command.
	 */
	private NGATProperties replyProperties = null;

	/**
	 * Default constructor.
	 * The logger instance is also created. The replyProperties instance is initialised.
	 * @see #logger
	 * @see #replyProperties
	 */
	public TOCAClient() 
	{
		super();
		replyProperties = new NGATProperties();
		logger = LogManager.getLogger(this);
	}

	/**
	 * Create a TOCAClient using the supplied parameters.
	 * The logger instance is also created. The replyProperties instance is initialised.
	 * @param command The command string to send.
	 * @param host The TOCS Server IP Address.
	 * @param port The TOCS Server port.
	 * @see #TOCAClient()
	 * @see #logger
	 * @see #replyProperties
	 */
	public TOCAClient(String command, String host, int port) 
	{
		this();
		this.command = command;
		this.host = host;
		this.port = port;
	}

	/**
	 * Set the command the client sends to the RCS TOCA.
	 * @param s The command.
	 * @see #command
	 */
	public void setCommand(String s)
	{
		command = s;
	}
  
	/**
	 * Set the RCS TOCA host the client connects to.
	 * @param s The host name.
	 * @see #host
	 */
	public void setHost(String s)
	{
		host = s;
	}
  
	/**
	 * Set the RCS TOCA port the client connects to.
	 * @param i The port number.
	 * @see #port
	 */
	public void setPort(int i)
	{
		port = i;
	}
  
	/**
	 * Called to send the command. This method which delegates to the TelnetConnection
	 * will block until the reply is received from the server or connection fails for some reason.
	 * A single line command is sent and a single line reply is expected. The connection will be 
	 * closed by this client after receiving this line, any extra lines are lost.
	 * @see #host
	 * @see #port
	 * @see #tc
	 * @see #logger
	 * @see #setError
	 * @see #replyString
	 * @see #parseError
	 * @see #parseReply
	 */	
	public void run()
	{
		try
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Connecting to "+host+":"+port);
			tc = new TelnetConnection(host, port);
			try
			{
				tc.open();
				logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Opened connection");
			}
			catch (Exception e)
			{
				setError(true, "Failed to open connection to TOCS: "+e);
				return;
			}
			tc.sendLine(command);
			logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Sent ["+command+"]");
			try
			{
				replyString = tc.readLine();
				logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Reply ["+replyString+"]");
				if (replyString == null || replyString.equals(""))
				{
					setError(true, "Null reply from TOCS");
					return;
				}
				replyString = replyString.trim();
				if (replyString.startsWith("ERROR"))
				{
					parseError();
					return;
				}
				parseReply();
			}
			catch (Exception e)
			{
				setError(true, "Failed to read TOCS response: "+e);
				return;
			}	
			setError(false, "Command accepted by TOCS");
		}
		catch (Exception e)
		{
			setError(true, "Failed to read TOCS response: "+e);
			return;		
		}
		finally
		{
			logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Closing connection");
			try
			{
				tc.close();
			}
			catch (Exception e)
			{
				// We dont really care..
				logger.log(INFO, 1, CLASS, RCSID,"run","TOCAClient::Error closing connection:"+e);
				logger.dumpStack(1,e);
			}	 	  
		}
	}
    
	/** 
	 * Sets the current error state and message.
	 * @param error Whether an error has occured.
	 * @param errorMessage The text of any error message.
	 * @see #error
	 * @see #errorMessage
	 * @see #errorCode
	 */
	private void setError(boolean error, String errorMessage)
	{
		this.error = error;
		this.errorMessage = errorMessage;
		this.errorCode = "INTERNAL_ERROR";
	}
	
	/** 
	 * Sets the current error state and message, from the contents of the reply string.
	 * The reply string should contain something like:
	 * <pre>
	 * ERROR NOT_OPERATIONAL
	 * </pre>
	 * or
	 * <pre>
	 * ERROR ABORTED Code=607001, message=Overridden by higher priority service
	 * </pre>
	 * The first space-separated word is put into the error code, the rest into the error string.
	 * @exception IllegalArgumentException Thrown if replyString is not formatted correctly.
	 * @see #error
	 * @see #errorMessage
	 * @see #errorCode
	 * @see #replyString
	 */
	private void parseError() throws IllegalArgumentException
	{
		String s = null;
		int sindex;

		if(replyString.startsWith("ERROR") == false)
		{
			throw new IllegalArgumentException(this.getClass().getName()+
							   ":parseError:reply string not an error:"+replyString);
		}
		this.error = true;
		// remove leading ERROR plus space
		s = replyString.substring(6);
		// find space after error code.
		sindex = s.indexOf(' ');
		if(sindex > -1)
			this.errorCode = s.substring(0,sindex);
		this.errorMessage = s.substring(sindex+1);
	}

	/**
	 * Returns True if there is an error.
	 * @return A boolean, true if an error occured, false if all was OK.
	 * @see #error
	 */
	public boolean isError()
	{
		return error;
	}
    
	/**
	 * Returns the current error code or null.
	 * @return A string.
	 * @see #errorCode
	 */
	public String getErrorCode()
	{
		return errorCode;
	}
    
	/**
	 * Returns the current error message or null.
	 * @return A string.
	 * @see #errorMessage
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
    
	/** 
	 * Sets the current replyProperties from the contents of the reply string.
	 * The string should be something like:
	 * <pre>
	 * OK sessionID=004951, sessionLimit=3600, timeRemaining=4612, priority=2
	 * </pre>
	 * The results should be parsed into keyword value pairs in replyProperties of the form:
	 * <ul>
	 * <li>sessionID, 004951
	 * <li>sessionLimit, 3600
	 * <li>timeRemaining, 4612
	 * <li>priority, 2
	 * </ul>
	 * @exception IllegalArgumentException Thrown if the replyString is not formatted correctly.
	 * @see #logger
	 * @see #replyString
	 * @see #replyProperties
	 */
	private void parseReply() throws IllegalArgumentException
	{
		String s = null;
		StringTokenizer st = null;
		String keywordValueString = null;
		int equalsIndex;
		String keyword = null;
		String valueString = null;

		this.error = false;
		if(replyString.startsWith("OK") == false)
		{
			throw new IllegalArgumentException(this.getClass().getName()+
							   ":parseReply:reply string not OK:"+replyString);
		}
		// check for blank OK string, otherwise substring causes StringIndexOutOfBoundsException.
		if(replyString.equals("OK "))
		{
			logger.log(INFO, 1, CLASS, RCSID,"parseReply",
				   "TOCAClient::Reply string was empty.");
			return;
		}
		// reset reply properties to empty.
		replyProperties = new NGATProperties();
		// remove OK from replyString and put into s.
		s = replyString.substring(3);
		// tokenize s by ',' separated values.
		st = new StringTokenizer(s,",");
		while (st.hasMoreTokens())
		{
			keywordValueString = st.nextToken();
			// get rid of whitespace.
			keywordValueString = keywordValueString.trim();
			equalsIndex = keywordValueString.indexOf("=");
			if(equalsIndex > -1)
			{
				keyword = keywordValueString.substring(0,equalsIndex);
				valueString = keywordValueString.substring(equalsIndex+1);
				logger.log(INFO, 1, CLASS, RCSID,"parseReply","TOCAClient::Reply Keyword ["+keyword+
					   "] has value ["+valueString+"]");
				replyProperties.setProperty(keyword,valueString);
			}
			else
			{
				logger.log(INFO, 1, CLASS, RCSID,"parseReply",
					   "TOCAClient::Reply Keyword/Value string ["+keywordValueString+
					   "] has no comma, must not be keyword/value?");
			}
		}
	}

	/**
	 * Returns the command reply.
	 * @return The reply string from the server.
	 * @see #replyString
	 */
	public String getReply()
	{
		return replyString;
	}

	/**
	 * Returns the string value for the specified keyword, if it exists in the list of keyword values.
	 * @param keyword The keyword.
	 * @return The keyword's string value. If the keyword does not exist, null is returned.
	 * @see #replyProperties
	 */
	public String getReplyValue(String keyword)
	{
		return replyProperties.getProperty(keyword);
	}

	/**
	 * Returns the int value for the specified keyword, if it exists in the list of keyword values.
	 * @param keyword The keyword.
	 * @return The keyword's int value. 
	 * @exception NGATPropertyException Thrown if the value is not a valid integer.
	 * @see #replyProperties
	 */
	public int getReplyValueInt(String keyword) throws NGATPropertyException
	{
		return replyProperties.getInt(keyword);
	}

	/**
	 * Returns the double value for the specified keyword, if it exists in the list of keyword values.
	 * @param keyword The keyword.
	 * @return The keyword's double value. 
	 * @exception NGATPropertyException Thrown if the value is not a valid double.
	 * @see #replyProperties
	 */
	public double getReplyValueDouble(String keyword) throws NGATPropertyException
	{
		return replyProperties.getDouble(keyword);
	}
}
/*
** $Log: not supported by cvs2svn $
** Revision 1.2  2005/06/07 13:27:51  cjm
** Comment fix.
**
** Revision 1.1  2005/06/06 14:45:00  cjm
** Initial revision
**
*/
