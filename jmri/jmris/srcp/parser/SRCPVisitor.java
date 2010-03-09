//SRCPVisitor.java

package jmri.jmris.srcp.parser;

public class SRCPVisitor implements SRCPParserVisitor {


  public Object visit(SimpleNode node, Object data)
  {
    log.debug("Generic Visit " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcommand node,Object data)
  {
    log.debug("Command " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTget node, Object data)
  {
    log.debug("Get " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTset node, Object data)
  {
    log.debug("Set" +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTterm node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcheck node, Object data)
  {
    log.debug("Check " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTverify node,java.lang.Object data)
  {
    log.debug("Verify " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTreset node,java.lang.Object data)
  {
    log.debug("Reset " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTinit node,java.lang.Object data)
  {
    log.debug("Init " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTcomment node,java.lang.Object data)
  {
    log.debug("Check " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTgl node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsm node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTga node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTfb node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTtime node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTpower node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTserver node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTsession node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTlock node, Object data)
  {
    log.debug("TERM " +node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTwait_cmd node, Object data)
  {
    log.debug("Received WAIT CMD " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTbus node, Object data)
  {
    log.debug("Received Bus " + node.jjtGetValue() );
    return node.childrenAccept(this,data);
  }
  public Object visit(ASTaddress node, Object data)
  {
    log.debug("Received Address " + node.jjtGetValue() );
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

  static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPVisitor.class.getName());

}
