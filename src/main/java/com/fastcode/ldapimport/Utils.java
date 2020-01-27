package com.fastcode.ldapimport;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	public static Timestamp getDateFromLDAP(String strDate, String format) {
		Date date = null;
		DateFormat formatter = new SimpleDateFormat(format);
		
		try {
			date = formatter.parse(strDate);
		}
		catch (ParseException pe) {
			System.out.println("Error: " + pe.toString());
		}
		return new Timestamp(date.getTime());
	}
	
	public static java.sql.Date convertUtilToSql(java.util.Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }

}
