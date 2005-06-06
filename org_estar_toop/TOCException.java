// TOCException.java
// $Header: /space/home/eng/cjm/cvs/org_estar_toop/TOCException.java,v 1.1 2005-06-06 17:46:56 cjm Exp $
package org.estar.toop;

/**
 * This class extends Exception. 
 * @author Chris Mottram
 * @version $Revision: 1.1 $
 */
public class TOCException extends Exception
{
	/**
	 * Revision Control System id string, showing the version of the Class
	 */
	public final static String RCSID = new String("$Id: TOCException.java,v 1.1 2005-06-06 17:46:56 cjm Exp $");

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
	 * @param exception An exception that caused this exception to be generated.
	 */
	public TOCException(String errorString,Exception e)
	{
		super(errorString,e);
	}
}

//
// $Log: not supported by cvs2svn $
//
