// Pr1Importer.java

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import java.lang.ArrayIndexOutOfBoundsException;

import jmri.JmriException;

/**
 * Import CV values from a "PR1" file written by PR1DOS or PR1WIN
 *
 * @author			Alex Shepherd   Copyright (C) 2003
 * @version			$Revision: 1.7 $
 */
public class Pr1Importer {
  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1Importer.class.getName());
  private static final String VERSION_KEY = "Version" ;
  private static final String CV_PREFIX = "CV" ;
  private static final int    CV_INDEX_OFFSET = 2 ;

  Properties  m_CVs ;
  boolean     m_packedValues = false ;

  public Pr1Importer( File file ) throws IOException {
    m_CVs = new Properties() ;
    FileInputStream fileStream = new FileInputStream( file);
    m_CVs.load( fileStream );

    // First check to see if the file contains a Version=x entry and if it
    // does assume it is a PR1WIN file that has packed values
    if( m_CVs.containsKey( VERSION_KEY ) ) {
      if (m_CVs.get(VERSION_KEY).equals("0"))
        m_packedValues = true;

      else
        throw new IOException("Unsupported PR1 File Version");
    }

    // Have a look at the values and see if there are any entries with values
    // greater out of the range 0..255. If they are found then also assume PR1WIN
    else {
      Set cvSet = m_CVs.entrySet();
      Iterator cvIterator = cvSet.iterator() ;

      while( cvIterator.hasNext() ) {
        Map.Entry cvEntry = (Map.Entry) cvIterator.next() ;
        String cvKey = (String)cvEntry.getKey() ;
        if( cvKey.startsWith( CV_PREFIX ) )
        {
          String cvValue = (String)cvEntry.getValue() ;
          int cvIntValue = Integer.parseInt( cvValue ) ;
          if( ( cvIntValue < 0 ) || ( cvIntValue > 255 ) )
          {
            m_packedValues = true;
            return ;
          }
        }
      }
    }
  }

  public void setCvTable( CvTableModel pCvTable ){
    Set keys = m_CVs.keySet() ;
    Iterator keyIterator = keys.iterator() ;
    while( keyIterator.hasNext()){
      String key = (String)keyIterator.next() ;
      if( key.startsWith( CV_PREFIX ) )
      {
        int Index = Integer.parseInt( key.substring( CV_INDEX_OFFSET ) ) ;

        int lowCV ;
        int highCV ;

        if( m_packedValues ){
          lowCV = Index * 4 - 3 ;
          highCV = Index * 4 ;
        }
        else{
          lowCV = Index ;
          highCV = Index ;
        }

        for( int cvNum = lowCV; cvNum <= highCV; cvNum++ ){
          if( cvNum <= CvTableModel.MAXCVNUM ){   // MAXCVNUM is the highest number, so is included
            try {
                CvValue cv = pCvTable.getCvByNumber( cvNum ) ;
                if (cv!=null) {
                    cv.setValue( getCV( cvNum ) );
                }
            }
            catch (JmriException ex) {
              log.error( "failed to getCV() " + cvNum );
            }
            catch (ArrayIndexOutOfBoundsException ex){
              log.error( "failed to getCvByNumber() " + cvNum );
            }
          }
        }
      }
    }
  }

  public int getCV( int cvNumber ) throws JmriException {
    int result ;

    if( m_packedValues ) {
      String cvKey = CV_PREFIX + ((cvNumber / 4) + 1) ;
      String cvValueStr = m_CVs.getProperty( cvKey ) ;
      if( cvValueStr == null )
        throw new JmriException( "CV not found" ) ;

      int shiftBits = ((cvNumber - 1) % 4 ) * 8 ;
      long cvValue = Long.parseLong( cvValueStr ) ;

      if( cvValue < 0 ) {
        result = (int)(((cvValue + 0x7FFFFFFF) >> shiftBits ) % 256 ) ;
        if( shiftBits > 16 )
          result += 127 ;
      } else
        result = (int)((cvValue >> shiftBits ) % 256 ) ;
    } else {
      String cvKey = CV_PREFIX + cvNumber ;
      String cvValueStr = m_CVs.getProperty(cvKey);
      if( cvValueStr == null )
        throw new JmriException( "CV not found" ) ;

      result = Integer.parseInt( cvValueStr ) ;
    }

    return result ;
  }

  public static void main(String[] args) {
    try {
      String logFile = "default.lcf";
      try {
        if (new java.io.File(logFile).canRead()) {
          org.apache.log4j.PropertyConfigurator.configure("default.lcf");
        } else {
          org.apache.log4j.BasicConfigurator.configure();
        }
      }
      catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

//      String fileName = "E:/ModelRail/pr1dos/alex.dec" ;
      String fileName = "E:/ModelRail/pr1dos/840B8601.dec" ;

      if( args.length > 0 )
        fileName = args[ 0 ] ;

      Pr1Importer cvList = new Pr1Importer( new File( fileName ) );
      CvTableModel model = new CvTableModel( null, null ) ;
      cvList.setCvTable( model );

      System.out.println( "File: " + fileName ) ;
      System.out.println( "CV#, Hex Int, Dec Int, Hex Byte, Dec Byte" ) ;
      for( int cvIndex = 1; cvIndex <= 512; cvIndex++ ){
        try {
          int cvValue = cvList.getCV(cvIndex);
          System.out.println("CV" + cvIndex + " " + Integer.toHexString(cvValue) +
                             ", " + cvValue);
        }
        catch (JmriException ex1) {
          log.debug( "CV Not Found: " + cvIndex );
        }
      }
    }
    catch (IOException ex) {
      log.debug( "IOException: ", ex );
    }
    catch (Exception ex) {
      log.debug( "Exception: ", ex );
    }

    System.exit( 0 );
  }
}