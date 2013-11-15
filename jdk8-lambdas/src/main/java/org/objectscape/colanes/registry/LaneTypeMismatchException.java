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
 * Defines an exception that is thrown in case the type parameters of a map stored
 * in some <code>Registry</code> do not match the declared types in the lookup method.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class LaneTypeMismatchException extends RuntimeException
{
    public LaneTypeMismatchException() {
        super();
    }

    public LaneTypeMismatchException(String message) {
        super(message);
    }

    public LaneTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public LaneTypeMismatchException(Throwable cause) {
        super(cause);
    }

    protected LaneTypeMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
