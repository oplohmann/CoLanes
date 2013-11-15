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

/**
 * String utility class containing methods not found in commons.lang.StringUtil
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public interface StringUtils {

    default public String nonBlank(String str, String label) {
        if(str == null)
            throw new NullPointerException(label + " null");
        if(str.length() == 0)
            throw new IllegalArgumentException(label + " size must not be zero");
        return str;
    }

}
