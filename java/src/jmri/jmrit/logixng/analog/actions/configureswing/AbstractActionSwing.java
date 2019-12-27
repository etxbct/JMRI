package jmri.jmrit.logixng.analog.actions.configureswing;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;

/**
 * Abstract class for SwingConfiguratorInterface
 */
public abstract class AbstractActionSwing implements SwingConfiguratorInterface {

    protected JPanel panel;
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel() throws IllegalArgumentException {
        createPanel(null);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object) throws IllegalArgumentException {
        createPanel(object);
        return panel;
    }
    
    protected abstract void createPanel(Base object);
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(AnalogActionManager.class).getAutoSystemName();
    }
    
}
