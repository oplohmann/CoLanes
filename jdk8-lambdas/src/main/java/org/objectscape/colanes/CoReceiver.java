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

package org.objectscape.colanes;

import org.objectscape.colanes.util.Signature;

/**
 * Interface every CoReceiver must implement if it makes use of some <code>Registry</code>.
 * Otherwise, it is used as a marker interface marking the object as a CoReceiver.
 *
 * CoReceiver is the term in LambdaLanes used for an active object. A CoReceiver is a Runner that
 * runs (co)ncurrent and is (co)-operative, hence the name.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public interface CoReceiver
{
    /**
     * Answer the unique name of the corunner. The unique name is required if the corunner
     * will be registered with an <code>Registry</code>. If not defined in the
     * implementing class, return a default composed of the class name and the
     * object hash.
     *
     * @return corunner name
     */
    default public String getName() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * Answer the unique name of the corunner. Throw a NullPointerException in case the
     * getter for the name of implementing class returns null.
     *
     * @return corunner name
     */
    default public String getNameNotNull() {
        String name = getName();
        if(name == null)
            throw new NullPointerException("corunner name null");
        return name;
    }

    /**
     * Returns the signature of a corunner. The name of the corunner may be null.
     * The <code>Signature</code> allows some corunner to identify the corunner that invoked a method on
     * it. Using the <code>Signature</code> some corunner can get hold of the sending corunner in order
     * to send a reply back.
     *
     * @return corunner Signature
     */
    default public Signature getSignature() {
        return new Signature(getName(), this);
    }

    /**
     * Same as <code>getSignature()</code> but throws a NullPointerException in case <code>getName()</code> returns null.
     *
     * @return corunner Signature
     */
    default public Signature getNamedSignature() {
        return new Signature(getNameNotNull(), this);
    }

    default public CoLane newNamedLambdaLane() {
        return new CoLane(getNameNotNull());
    }

}
