// TOCException.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCException.java,v 1.3 2005-06-07 13:48:00 cjm Exp $
package org.estar.toop;

import java.io.*;

/**
 * This class extends Exception. 
 * @author Chris Mottram
 * @version $Revision: 1.3 $
 */
public class TOCException extends Exception
{
	/**
	 * Revision Control System id string, showing the version of the Class
	 */
	public final static String RCSID = new String("$Id: TOCException.java,v 1.3 2005-06-07 13:48:00 cjm Exp $");
	/**
	 * An exception that caused this exception to be generated.
	 */
	private Exception exception = null;

	/**
	 * Constructor for the exception.
	 * @param errorString The error string.
	 */
	public TOCException(String errorString)
	{
		super(errorString);
	}

	/**
	 * Constructor for the exception.
	 * @param errorString The error string.
	 * @param e An exception that caused this exception to be generated.
	 * @see #exception
	 */
	public TOCException(String errorString,Exception e)
	{
		super(errorString,e);
		exception = e;
	}

	/**
	 * Retrieve method to return exception that generated this exception.
	 * @return An exception, or null.
	 * @see #exception
	 */
	public Exception getException()
	{
		return exception;
	}

	/**
	 * Overridden toString method, that calls super toString and adds exception's toString to it.
	 * @see #exception
	 */
	public String toString()
	{
		StringBuffer sb = null;

		sb = new StringBuffer();
		sb.append(super.toString());
		if(exception != null)
			sb.append(exception.toString());
		return sb.toString();
	}

	/**
	 * Overridden printStackTrace, that prints the creating exceptions stack if it is non-null.
	 * NB Logger.dumpStack uses this method.
	 * @param s The writer to write to.
	 */
	public void printStackTrace(PrintWriter s)
	{
		super.printStackTrace(s);
		if(exception != null)
			exception.printStackTrace(s);
	}

	/**
	 * Overridden printStackTrace, that prints the creating exceptions stack if it is non-null.
	 * NB printStackTrace() uses this method.
	 * @param s The stream to write to.
	 */
	public void printStackTrace(PrintStream s)
	{
		super.printStackTrace(s);
		if(exception != null)
			exception.printStackTrace(s);
	}
}

//
// $Log: not supported by cvs2svn $
// Revision 1.2  2005/06/07 13:36:40  cjm
// Removed v1.4 specific code (Exception's constructor Exception(String,Exception)).
// Added own exception wrapping code, overridden toString and printStackTrace.
//
// Revision 1.1  2005/06/06 17:46:56  cjm
// Initial revision
//
//
