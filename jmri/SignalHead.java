// SignalHead.java

package jmri;

/**
 * Represent a single signal head. (Try saying that ten times fast!)
 * A signal may have more than one of these to represent Diverging Appoach
 * etc.
 * <P>
 * Initially, this allows access to explicit appearance information. We
 * don't call this an Aspect, as that's a composite of the appearance
 * of several heads.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.6 $
 */
public interface SignalHead extends NamedBean {

    public static final int DARK        = 0x00;
    public static final int RED         = 0x01;
    public static final int FLASHRED    = 0x02;
    public static final int YELLOW      = 0x04;
    public static final int FLASHYELLOW = 0x08;
    public static final int GREEN       = 0x10;
    public static final int FLASHGREEN  = 0x20;

    /**
     * Appearance is a bound parameter. Value values are the
     * various color contants defined in the class. As yet,
     * we have no decision as to whether these are exclusive or
     * can be or'd together.
     */
    public int getAppearance();
    public void setAppearance(int newAppearance);

}


/* @(#)SignalHead.java */
