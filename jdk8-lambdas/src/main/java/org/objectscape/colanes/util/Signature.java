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
package org.objectscape.colanes.util;

import org.objectscape.colanes.CoReceiver;

/**
 * Defines the signature of an <code>CoReceiver</code>, which is defined by its
 * name, and its class. These attributes are required to obtain a reference to an
 * coReceiver if it is registered with the registry. The signature may contain
 * a direct reference to the coReceiver that can be used if it resides on the same
 * VM as the signature object.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Signature<T extends CoReceiver> implements StringUtils {

    private String name;
    private CoReceiver coReceiver;
    private Class<T> activeObjectClass;

    public Signature(String name, Class<T> activeObjectClass) {
        this.name = nonBlank(name, "name");
        this.activeObjectClass = activeObjectClass;
    }

    public Signature(String name, CoReceiver coReceiver) {
        this.name = nonBlank(name, "name");
        this.coReceiver = coReceiver;
        this.activeObjectClass = (Class<T>) coReceiver.getClass();
    }

    public String getName() {
        return name;
    }

    public Class<T> getActiveObjectClass() {
        return activeObjectClass;
    }

    public CoReceiver getCoReceiver() {
        return coReceiver;
    }
}
