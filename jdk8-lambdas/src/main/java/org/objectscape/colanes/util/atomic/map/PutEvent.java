/**
 * Copyright (c) 2013 Oliver Plohmann
 * http://www.objectscape.org/colanes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectscape.colanes.util.atomic.map;

/**
 * Defines a callback event that is passed on to the callback lambda in case
 * values were added to a <code>ListenableConcurrentMap</code> through a put operation
 *
 * @see org.objectscape.colanes.util.concurrent.map.ListenableConcurrentHashMap
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class PutEvent<V> extends MapValueEvent<V>
{

    /**
     * Creates a new <code>PutEvent</code> object
     */
	public PutEvent() {
		super();
	}

    /**
     * Creates a new <code>PutEvent</code> object
     *
     * @param mapName name of the map the event refers to
     * @param key of the values that were changed
     */
	public PutEvent(String mapName, Object key) {
		super(mapName, key);
	}

    /**
     * Creates a new <code>PutEvent</code> object
     *
     * @param mapName name of the map the event refers to
     * @param key of the values that were changed
     * @param putValue value that was added to the map through a put operation
     */
	public PutEvent(String mapName, Object key, V putValue, int nextInvocationCount) {
		super(mapName, key, putValue, nextInvocationCount);
	}

}
