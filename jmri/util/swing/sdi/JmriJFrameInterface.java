// JmriJFrameInterface.java

package jmri.util.swing.sdi;

/**
 * Display a JmriPanel in a JFrame of its own.
 *
 * Dispose() of the panel is invoked when the containing window is fully closed
 * via a listener installed here.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.2 $
 */

public class JmriJFrameInterface implements jmri.util.swing.WindowInterface {

    public void show(final jmri.util.swing.JmriPanel child, 
                        jmri.util.swing.JmriAbstractAction act,
                        Hint hint) {
            // create frame
            jmri.util.JmriJFrame frame = new jmri.util.JmriJFrame();
            
            // add gui object, responsible for own layout
            frame.add(child);
            
            // add help menu if needed
            if (child.getHelpTarget() != null)
                frame.addHelpMenu(child.getHelpTarget(), true);

            // set title if available
            if (child.getTitle() != null)
                frame.setTitle(child.getTitle());

            // arrange to run dispose on close
            frame.addWindowListener( new java.awt.event.WindowAdapter(){
                jmri.util.swing.JmriPanel c;
                { c = child; }
                public void windowClosed(java.awt.event.WindowEvent e) {
                    c.dispose();
                }
            });
           
            // pack and show
            frame.pack();
            frame.setVisible(true);
        }

    public void show(final jmri.util.swing.JmriPanel child, 
                        jmri.util.swing.JmriAbstractAction act) {
            
                show(child, act, Hint.DEFAULT);
            }

    /** 
     * Create new windows on each request
     */
    public boolean multipleInstances() { return true; }

}