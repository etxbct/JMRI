package jmri.jmris.srcp.configurexml;

/**
 * @author Randall Wood Copyright (C) 2012
 */
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import jmri.jmris.srcp.JmriSRCPServerManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

public class JmriSRCPServerPreferencesPanel extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = 03_16_2015L;
    private JSpinner port;
    private JButton btnSave;
    private JButton btnCancel;
    private JmriSRCPServerPreferences preferences;
    private JFrame parentFrame = null;

    public JmriSRCPServerPreferencesPanel() {
        this.preferences = new JmriSRCPServerPreferences();
        this.preferences.apply(JmriSRCPServerManager.getJmriSRCPServerPreferences());
        initGUI();
        setGUI();
    }

    public JmriSRCPServerPreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("ServerSectionTitle")));
        add(portPanel());
        add(new JTitledSeparator(Bundle.getMessage("SRCPSectionTitle")));
        add(Box.createVerticalGlue());
    }

    private void setGUI() {
        port.setValue(preferences.getPort());
    }

    /**
     * Show the save and cancel buttons if displayed in its own frame.
     */
    public void enableSave() {
        btnSave.setVisible(true);
        btnCancel.setVisible(true);
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        int portNum;
        try {
            portNum = (Integer)port.getValue();
        } catch (NumberFormatException NFE) { //  Not a number
            portNum = 0;
        }
        if ((portNum < 1) || (portNum > 65535)) { //  Invalid port value
            javax.swing.JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("InvalidPortWarningMessage"),
                    Bundle.getMessage("InvalidPortWarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
            didSet = false;
        } else {
            preferences.setPort(portNum);
        }
        return didSet;
    }

    /**
     * Update the singleton instance of prefs, then mark (isDirty) that the
     * values have changed and needs to save to xml file.
     */
    protected void applyValues() {
        this.setValues();
    }

    protected void cancelValues() {
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JSpinner(new SpinnerNumberModel(preferences.getPort(), 1, 65535, 1));
        ((JSpinner.DefaultEditor) port.getEditor()).getTextField().setEditable(true);
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        this.port.addChangeListener((ChangeEvent e) -> {
        this.setValues();
        });
        this.port.setToolTipText(Bundle.getMessage("PortToolTip"));
        panel.add(port);
        panel.add(new JLabel(Bundle.getMessage("LabelPort")));
        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return Bundle.getMessage("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("PreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("PreferencesTabTitle");
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return Bundle.getMessage("PreferencesTooltip");
    }

    @Override
    public void savePreferences() {
        if (this.setValues()) {
            JmriSRCPServerManager.getJmriSRCPServerPreferences().apply(this.preferences);
            JmriSRCPServerManager.getJmriSRCPServerPreferences().save();
            if (this.parentFrame != null) {
                this.parentFrame.dispose();
            }
        }
    }

    @Override
    public boolean isDirty() {
        return this.preferences.compareValuesDifferent(JmriSRCPServerManager.getJmriSRCPServerPreferences())
                || JmriSRCPServerManager.getJmriSRCPServerPreferences().isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return JmriSRCPServerManager.getJmriSRCPServerPreferences().isRestartRequired();
    }

    /**
     * Indicate that the preferences are valid.
     *
     * @return true if the preferences are valid, false otherwise
     */
    public boolean isPreferencesValid(){
        return false;
    }


}
