//SRCPClientVisitor.java

package jmri.jmrix.srcp.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.SRCPBusConnectionMemo;

/* This class provides an interface between the JavaTree/JavaCC 
 * parser for the SRCP protocol and the JMRI front end.
 * @author Paul Bender Copyright (C) 2011
 * @version $Revision$
 */

public class SRCPClientVisitor implements jmri.jmrix.srcp.parser.SRCPClientParserVisitor {


  public Object visit(SimpleNode node, Object data)
  {
    log.debug("Generic Visit " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
 public Object visit(ASTcommandresponse node,Object data)
  {
    log.debug("Command Response " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
 
 public Object visit(ASThandshakeresponse node,Object data)
  {
    log.debug("Handshake Response " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
 
  public Object visit(ASTgo node,Object data)
  {
    log.debug("Go " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTgl node, Object data)
  {
    log.debug("GL " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsm node, Object data)
  {
    log.debug("SM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTga node, Object data)
  {
    log.debug("GA" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTfb node, Object data)
  {
    log.debug("FB " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtime node, Object data)
  {
    log.debug("TIME " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTpower node, Object data)
  {
    log.debug("POWER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTlock node, Object data)
  {
    log.debug("LOCK " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserver node, Object data)
  {
    log.debug("SERVER " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsession node, Object data)
  {
    log.debug("SESION " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTbus node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
 /* public Object visit(ASTaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }*/
  public Object visit(ASTnonzeroaddress node, Object data)
  {
    log.debug("Received NonZeroAddress " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroaddress node, Object data)
  {
    log.debug("Received ZeroAddress " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTport node, Object data)
  {
    log.debug("Received Port " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdevicegroup node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTonoff node, Object data)
  {
    log.debug("Received ON/OFF " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdescription node, Object data)
  {
    log.debug("Description " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTdelay node, Object data)
  {
    log.debug("Delay " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTzeroone node, Object data)
  {
    log.debug("ZeroOne " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserviceversion node, Object data)
  {
    log.debug("Service Version " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTconnectionmode node, Object data)
  {
    log.debug("Connection Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }


  public Object visit(ASTinforesponse node, Object data)
  {
    log.debug("Information Response " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTinfo node, Object data)
  {
    log.debug("Info Response " +node.jjtGetValue() );
    int bus=Integer.parseInt(((String)((SimpleNode)node.jjtGetChild(0)).jjtGetValue()));
    SRCPBusConnectionMemo busMemo=((SRCPSystemConnectionMemo)data).getMemo(bus);
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTok node, Object data)
  {
    log.debug("Ok Response " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTerror node, Object data)
  {
    log.debug("Information Response " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTtimeout node, Object data)
  {
    log.debug("Timeout " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTcvno node, Object data)
  {
    log.debug("CV Number " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTprogmode node, Object data)
  {
    log.debug("Programming Mode Production" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcv node, Object data)
  {
    log.debug("CV Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcvbit node, Object data)
  {
    log.debug("CVBIT Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreg node, Object data)
  {
    log.debug("REG Programming Mode " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  public Object visit(ASTtimestamp node, Object data)
  {
    log.debug("Timestamp Node " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }

  static Logger log = LoggerFactory.getLogger(SRCPClientVisitor.class.getName());

}
