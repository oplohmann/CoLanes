package org.objectscape.colanes.util.concurrent.value;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 27.05.13
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */
public class SendEvent<V> extends ValueHolderEvent<V>
{
    public SendEvent(String valueHolderName, V value) {
        super(valueHolderName, value, -1);
    }

    public SendEvent(String valueHolderName, V value, int nextRunningInvocationCount) {
        super(valueHolderName, value, nextRunningInvocationCount);
    }

    public SendEvent(SendEvent<V> event, int nextRunningInvocationCount) {
        super(event.getName(), event.getValue(), nextRunningInvocationCount);
    }
}
