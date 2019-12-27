package jmri.jmrit.logixng.digital.expressions;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Locale;
import java.util.TimerTask;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This expression is a timer.
 * It can be of these types:
 * * Wait some time and then return 'true' once. Timer is restarted upon reset.
 * * Wait some time and then return 'true' until reset.
 * * Wait some time and then return 'true' once. Once evaluate() is called, the timer is reset and starts again.
 * * Wait some time and then return 'true'. Wait some time and then return 'false'. Once evaluate() is called, the timer is reset and starts again.
 * <P>
 * The timer is reset when listerners are registered, which will happen when
 * setEnabled(true) is called on the LogixNG, the ConditionalNG or any male
 * socket, if this timer is enabled and all its parents are enabled and if the
 * LogixNG is activated.
 * @author Daniel Bergqvist Copyright 2018
 */
public class Timer extends AbstractDigitalExpression {

    private java.util.Timer _timer;
    private TimerTask _timerTask;
    private TimerType _timerType = TimerType.WAIT_ONCE_TRIG_ONCE;
    private boolean _listenersAreRegistered = false;
    private final AtomicReference<TimerStatus> _timerStatusRef = new AtomicReference<>(TimerStatus.NOT_STARTED);
    private boolean _onOrOff = false;
    private long _delayOff = 0;     // Time in milliseconds
    private long _delayOn = 0;      // Time in milliseconds
    
    
    public Timer(String sys, String user) {
        super(sys, user);
        _timer = new java.util.Timer("LogixNG ExpressionTimer timer thread", true);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    public void setTimerType(TimerType timerType) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setTimerType must not be called when listeners are registered");
            log.error("setTimerType must not be called when listeners are registered", e);
            throw e;
        }
        _timerType = timerType;
    }
    
    public TimerType getTimerType() {
        return _timerType;
    }
    
    public void setTimerDelay(long delayOff, long delayOn) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setTimerDelay must not be called when listeners are registered");
            log.error("setTimerDelay must not be called when listeners are registered", e);
            throw e;
        }
        _delayOff = delayOff;
        _delayOn = delayOn;
    }
    
    public long getTimerDelayOff() {
        return _delayOff;
    }
    
    public long getTimerDelayOn() {
        return _delayOn;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
//        System.out.format("Timer.evaluate(). _timerStatus: %s%n", _timerStatusRef.get().name());
        boolean result = false;
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    _timerStatusRef.set(TimerStatus.STARTED);
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    _timerStatusRef.set(TimerStatus.WAIT_FOR_RESET);
                    result = true;
                }
                break;
                
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    _timerStatusRef.set(TimerStatus.STARTED);
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    // Don't set _timerStatus to WAIT_FOR_RESET since we want
                    // to keep returning true until reset()
                    result = true;
                }
                break;
                
            case REPEAT_SINGLE_DELAY:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
//                    _timerStatusRef.set(TimerStatus.STARTED);
                    startTimer();
                    result = true;
                }
                break;
                
            case REPEAT_DOUBLE_DELAY:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    _onOrOff = ! _onOrOff;
                    startTimer();
                }
                result = _onOrOff;
                break;
                
            default:
                throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
        }
        
//        System.out.format("Timer.evaluate(): result: %b%n", result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        stopTimer();
        _onOrOff = false;
        _timerStatusRef.set(TimerStatus.NOT_STARTED);
        getConditionalNG().execute();
    }
    
    private TimerTask getNewTimerTask() {
        final jmri.jmrit.logixng.ConditionalNG c = getConditionalNG();
        
        if (c == null) throw new NullPointerException("getConditionalNG() returns null");
        
        return new TimerTask() {
            @Override
            public void run() {
                _timerStatusRef.set(TimerStatus.FINISHED);
                c.execute();
            }
        };
    }
    
    private void scheduleTimer(long delay) {
        synchronized(this) {
            if (_timerTask != null) _timerTask.cancel();

            _timerTask = getNewTimerTask();
            _timer.schedule(_timerTask, delay);
/*            
            try {
                if (_timerTask != null) _timerTask.cancel();
                
                _timerTask = getNewTimerTask();
                _timer.schedule(_timerTask, delay);
            } catch (IllegalStateException e) {
                _timerTask.cancel();
                _timer.cancel();
                _timerTask = getNewTimerTask();
                _timer = new java.util.Timer("LogixNG ExpressionTimer timer thread", true);
                _timer.schedule(_timerTask, _delayOff);
            }
*/            
        }
    }
    
    private void startTimer() {
        _timerStatusRef.set(TimerStatus.STARTED);
        
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                // fall through
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                // fall through
            case REPEAT_SINGLE_DELAY:
                scheduleTimer(_delayOff);
                break;
                
            case REPEAT_DOUBLE_DELAY:
                scheduleTimer(_onOrOff ? _delayOn : _delayOff);
                break;
                
            default:
                throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
        }
    }
    
    private void stopTimer() {
        synchronized(this) {
            if (_timerTask != null) _timerTask.cancel();
        }
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Timer_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigOnce", _delayOff);
                
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigUntilReset", _delayOff);
                
            case REPEAT_SINGLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatSingleDelay", _delayOff);
                
            case REPEAT_DOUBLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatDoubleDelay", _delayOff, _delayOn);
                
            default:
                throw new RuntimeException("Unknown value of _timerType: "+_timerType.name());
        }
//        return Bundle.getMessage(locale, "Timer_Long");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
            // We need to reset the timer
            reset();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        stopTimer();
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        synchronized(this) {
            if (_timerTask != null) _timerTask.cancel();
        }
    }
    
    
    
    public enum TimerType {
        WAIT_ONCE_TRIG_ONCE(Bundle.getMessage("TimerType_WaitOnceTrigOnce"), Bundle.getMessage("TimerType_Explanation_WaitOnceTrigOnce")),
        WAIT_ONCE_TRIG_UNTIL_RESET(Bundle.getMessage("TimerType_WaitOnceTrigUntilReset"), Bundle.getMessage("TimerType_Explanation_WaitOnceTrigUntilReset")),
        REPEAT_SINGLE_DELAY(Bundle.getMessage("TimerType_RepeatSingleDelay"), Bundle.getMessage("TimerType_Explanation_RepeatSingleDelay")),
        REPEAT_DOUBLE_DELAY(Bundle.getMessage("TimerType_RepeatDoubleDelay"), Bundle.getMessage("TimerType_Explanation_RepeatDoubleDelay"));
        
        private final String _text;
        private final String _explanation;
        
        private TimerType(String text, String explanation) {
            this._text = text;
            this._explanation = explanation;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
        public String getExplanation() {
            return _explanation;
        }
        
    }
    
    
    private enum TimerStatus {
        NOT_STARTED,
        STARTED,
        FINISHED,
        WAIT_FOR_RESET,
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(Timer.class);
    
}
