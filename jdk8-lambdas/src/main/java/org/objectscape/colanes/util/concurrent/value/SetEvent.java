package org.objectscape.colanes.util.concurrent.value;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 27.05.13
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */
public class SetEvent<V> extends ValueHolderEvent<V> {

    private final V previousValue;

    private SetEvent(SetEvent<V> event) {
        super(event);
        previousValue = null;
    }

    public SetEvent(String valueHolderName, V previousValue, V value) {
        super(valueHolderName, value, -1);
        this.previousValue = previousValue;
    }

    public SetEvent(String valueHolderName, V previousValue, V value, int invocationCount) {
        super(valueHolderName, value, invocationCount);
        this.previousValue = previousValue;
    }

    public SetEvent(SetEvent<V> event, int invocationCount) {
        super(event, invocationCount);
        this.previousValue = event.getPreviousValue();
    }

    public SetEvent(SetEvent<V> event, V previousValue) {
        super(event, -1);
        this.previousValue = previousValue;
    }

    public SetEvent(SetEvent<V> event, V previousValue, int invocationCount) {
        super(event, invocationCount);
        this.previousValue = previousValue;
    }

    public V getPreviousValue() {
        return previousValue;
    }
}
