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

package org.objectscape.colanes.registry;

/**
 * Thrown in case an lane has already been registered with some
 * <code>Registry</code> with the same name.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class LaneAlreadyRegisteredException extends RuntimeException
{
    public LaneAlreadyRegisteredException() {
    }

    public LaneAlreadyRegisteredException(String message) {
        super(message);
    }

    public LaneAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public LaneAlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

    public LaneAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
