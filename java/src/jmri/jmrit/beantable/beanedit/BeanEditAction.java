// BeanEditAction.java

package jmri.jmrit.beantable.beanedit;

import jmri.NamedBean;
import javax.swing.AbstractAction;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import jmri.util.JmriJFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;

/**
 * Provides the basic information and structure for
 * for a editing the details of a bean object
 *
 * @author	    Kevin Dickerson Copyright (C) 2011
 * @version		$Revision: 17977 $	
 */

abstract class BeanEditAction extends AbstractAction {
    
    public BeanEditAction(String s) {
        super(s);
    }
    
    public BeanEditAction() {
        super("Bean Edit");
    }
    
    jmri.NamedBean bean;
    
    public void setBean(jmri.NamedBean bean){
        this.bean = bean;
    }
    
    /**
    *  Call to create all the different tabs that will be added
    *  to the frame
    */
    protected void createPanels(){
        bei.add(basicDetails());
    }

    JTextField userNameField = new JTextField(20);
    JTextArea commentField = new JTextArea(3,30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);
    
    /**
    *  Creates a generic panel that holds the basic bean information
    *  System Name, User Name and Comment
    */
    EditBeanItem basicDetails(){
    
        ArrayList<Item> items = new ArrayList<Item>();
    
        items.add(new Item(new JLabel(bean.getSystemName()), Bundle.getMessage("ColumnSystemName"), null));
        
        EditBeanItem basic = new EditBeanItem();
        basic.setName("Basic");
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));

        items.add(new Item(userNameField, Bundle.getMessage("ColumnUserName"), null));
        
        items.add(new Item(commentFieldScroller, Bundle.getMessage("ColumnComment"), null));

        basic.setSaveItem(new AbstractAction(){
                public void actionPerformed(ActionEvent e) {
                    if(bean.getUserName()==null && !userNameField.getText().equals("")){
                        renameBean(userNameField.getText());
                    } else if(bean.getUserName()!=null && !bean.getUserName().equals(userNameField.getText())){
                        if(userNameField.getText().equals("")){
                            removeName();
                        } else {
                            renameBean(userNameField.getText());
                        }
                    }
                    bean.setComment(commentField.getText());
            }
        });
        basic.setResetItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                userNameField.setText(bean.getUserName());
                commentField.setText(bean.getComment());
            }
        });
        addToPanel(basic, items);
        return basic;
    }
    
    abstract protected String helpTarget();
    
    protected ArrayList<EditBeanItem> bei = new ArrayList<EditBeanItem>(5);
    JmriJFrame f;
    
    protected Component selectedTab = null;
    private JTabbedPane detailsTab = new JTabbedPane();
    
    public void setSelectedComponent(Component c){
        selectedTab=c;
    }
    
    public void actionPerformed(ActionEvent e) {
        if(bean==null){
            log.error("No bean set so unable to edit a null bean");  //IN18N
            return;
        }
        if(f==null){
            f = new JmriJFrame("Edit " + getBeanType() + " " + bean.getDisplayName(), false,false);
            f.addHelpMenu(helpTarget(), true);
            java.awt.Container containerPanel = f.getContentPane();
            createPanels();
            //JTabbedPane detailsTab = new JTabbedPane();
            for(EditBeanItem bi:bei){
                detailsTab.addTab(bi.getName(), bi);
            }
            
            containerPanel.add(detailsTab, BorderLayout.CENTER);
            JPanel buttons = new JPanel();
            JButton applyBut = new JButton(Bundle.getMessage("ButtonApply"));
            applyBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });
            JButton okBut = new JButton(Bundle.getMessage("ButtonOK"));
            okBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    save();
                    f.dispose();
                }
            });
            JButton cancelBut = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelBut.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    f.dispose();
                }
            });
            buttons.add(applyBut);
            buttons.add(okBut);
            buttons.add(cancelBut);
            containerPanel.add(buttons, BorderLayout.SOUTH);
        }
        for(EditBeanItem bi:bei){
            bi.resetField();
        }
        if(selectedTab!=null)
            detailsTab.setSelectedComponent(selectedTab);
        f.pack();
		f.setVisible(true);
	}
    
    /**
    *  Sets out the panel based upon the items passed in via the ArrayList
    */
    protected void addToPanel(JPanel panel, ArrayList<Item> items){
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cD = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.insets = new Insets(2, 0, 0, 15);
        cR.insets = new Insets(0, 10, 15, 15);
        cD.insets = new Insets(2, 0, 0, 0);
        cD.anchor = GridBagConstraints.NORTHWEST;
        cL.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;
        JPanel p = new JPanel();
        
        for(Item it:items){
            if(it.getDescription()!=null && it.getComponent()!=null){
                JLabel decript = new JLabel(it.getDescription() + ":", JLabel.LEFT);
                cL.gridx = 0;
                cL.gridy = y;
                cL.ipadx = 3;
                
                gbLayout.setConstraints(decript, cL);
                p.setLayout(gbLayout);
                p.add(decript, cL);
                
                cD.gridx = 1;
                cD.gridy = y;

                gbLayout.setConstraints(it.getComponent(), cD);

                p.add(it.getComponent(), cD);
                
                cR.gridx = 2;
                cR.gridwidth = 1;
                cR.anchor = GridBagConstraints.WEST;
            
            } else {
                cR.anchor = GridBagConstraints.CENTER;
                cR.gridx = 0;
                cR.gridwidth = 3;
            }
            cR.gridy = y;
            if(it.getHelp()!=null){
                JTextPane help = new JTextPane();
                help.setText(it.getHelp());
                gbLayout.setConstraints(help, cR);
                formatTextAreaAsLabel(help);
                p.add(help, cR);
            }
            y++;
        }

        panel.add(p);
    }
    
    void formatTextAreaAsLabel(JTextPane pane) {
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }
    
    public void save(){
        for(EditBeanItem bi:bei){
            bi.saveItem();
        }
    }
    
    static boolean validateNumericalInput(String text){
        if (text.length()!=0){
           try{
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }
    
    jmri.NamedBeanHandleManager nbMan = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    
    abstract protected String getBeanType();
    abstract protected NamedBean getBySystemName(String name);
    abstract protected NamedBean getByUserName(String name);
    
    /**
    * Generic method to change the user name of a Bean
    */
    public void renameBean(String _newName){
        NamedBean nBean = bean;
        String oldName = nBean.getUserName();

        String value = _newName;
        
        if(value.equals(oldName)){
            //name not changed.
            return;
        }
        else {
            NamedBean nB = getByUserName(value);
            if (nB != null) {
                log.error("User name is not unique " + value); // NOI18N
                String msg;
                msg = java.text.MessageFormat.format(Bundle.getMessage("WarningUserName"),
                        new Object[] { ("" + value) });
                JOptionPane.showMessageDialog(null, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        nBean.setUserName(value);
        if(!value.equals("")){
            if(oldName==null || oldName.equals("")){
                if(!nbMan.inUse(nBean.getSystemName(), nBean))
                    return;
                String msg = java.text.MessageFormat.format(Bundle.getMessage("UpdateToUserName"),
                        new Object[] { getBeanType(),value,nBean.getSystemName() });
                int optionPane = JOptionPane.showConfirmDialog(null,
                    msg, Bundle.getMessage("UpdateToUserNameTitle"), 
                    JOptionPane.YES_NO_OPTION);
                if(optionPane == JOptionPane.YES_OPTION){
                    //This will update the bean reference from the systemName to the userName
                    try {
                        nbMan.updateBeanFromSystemToUser(nBean);
                    } catch (jmri.JmriException ex){
                        //We should never get an exception here as we already check that the username is not valid
                    }
                }
                
            } else {
                nbMan.renameBean(oldName, value, nBean);
            }
            
        }
        else {
            //This will update the bean reference from the old userName to the SystemName
            nbMan.updateBeanFromUserToSystem(nBean);
        }
    }
    
    /**
    * Generic method to remove the user name from a bean.
    */
    public void removeName(){
        String msg = java.text.MessageFormat.format(Bundle.getMessage("UpdateToSystemName"),
                new Object[] { getBeanType()});
        int optionPane = JOptionPane.showConfirmDialog(null,
            msg, Bundle.getMessage("UpdateToSystemNameTitle"), 
            JOptionPane.YES_NO_OPTION);
        if(optionPane == JOptionPane.YES_OPTION){
            nbMan.updateBeanFromUserToSystem(bean);
        }
        bean.setUserName(null);
    }
    
    /**
    * Class used to define the items used with in a panel
    */
    protected static class Item {
        String help;
        String description;
        JComponent component;
        /**
        * Create the item structure to be added.
        * If the component and description are null, then the help text
        * will be displayed across the width of the panel.
        * @component Optional Contains the item to be edited
        * @description Optional Contains the text for the label that will be to the left of the component
        * @help Optional Contains the help or hint text, that will be displayed to the right of the component
        */
        public Item (JComponent component, String description, String help){
            this.component = component;
            this.description = description;
            this.help = help;
        }
        
        String getDescription(){
            return description;
        }
    
        String getHelp(){
            return help;
        }
        
        JComponent getComponent(){
            return component;
        }
    
    
    }
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BeanEditAction.class.getName());
}