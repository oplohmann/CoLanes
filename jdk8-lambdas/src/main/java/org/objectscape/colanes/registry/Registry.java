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

import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.util.Signature;
import org.objectscape.colanes.util.atomic.map.ListenableAtomicMap;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentHashMap;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static scala.concurrent.stm.japi.STM.atomic;
import static scala.concurrent.stm.japi.STM.newMap;

/**
 * Defines an registry for lanes to live in. An registry provides
 * a registry where lanes can register themselves by their names for lookup
 * by other lanes. In addition, it holds references to maps shared by active
 * objects that can be looked up by their names. Various kinds of maps exist like the
 * basic <code>ConcurrentMap</code>, the <code>ListenableConcurrentMap</code>, that allows
 * users to register event notification on put, and remove and ScalaSTM maps where the
 * user can invoke several map operations from within a atomic block.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Registry {

    /**
     * The default registry that is used in case no user-defined specific
     * registry is used.
     */
    private static final Registry DEFAULT_REGISTRY = new Registry();

    /**
     * Map of user-defined environments that can be looked up by their names.
     */
    private static final Map<String, Registry> environmentMap = newMap();

    /**
     * Map of lanes known to the registry by the names they were
     * registered.
     */
    private final Map<String, CoReceiver> activeObjectMapsByName = newMap();

    /**
     * Map of lanes of which a single instance is registered with the registry
     */
    private final Map<Class<? extends CoReceiver>, CoReceiver> singletonActiveObjectsByClass = newMap();

    /**
     * Map of ConcurrentMaps registered to the registry by their names
     */
    private final Map<String, ConcurrentMap<?, ?>> concurrentMapsByName = newMap();

    /**
     * Map of ListenableConcurrentMap registered to the registry by their names
     */
    private final Map<String, ListenableConcurrentMap<?, ?>> ListenableConcurrentMapsByName = newMap();

    /**
     * Map of ListenableAtomicMap registered to the registry by their names
     */
    private final Map<String, ListenableAtomicMap<?, ?>> ListenableAtomicMapsByName = newMap();

    /**
     * Map of Scala-STM maps registered to the registry by their names
     */
    private final Map<String, Map<?, ?>> StmMapsByName = newMap();

    /**
     * Return the default registry that is always guaranteed to exist
     *
     * @return the default registry
     */
    public static Registry getDefault() {
        return DEFAULT_REGISTRY;
    }

    /**
     * Return an registry with a given name. If it does not exist, it is created and returned.
     * Always returns the same instance that initially was created when first looked up.
     *
     * @param environmentName name of the registry to be returned
     * @return the registry with the name <tt>environmentName</tt>
     */
    public static Registry get(String environmentName) {
        return atomic(()-> {
            if(environmentName == null)
                throw new NullPointerException("environmentName must not be null");
            if(environmentName.length() == 0)
                throw new IllegalArgumentException("environmentName must not be empty string");
            Registry registry = environmentMap.get(environmentName);
            if(registry == null) {
                registry = new Registry();
                environmentMap.put(environmentName, registry);
            }
            return registry;
        });
    }

    /**
     * Register an lane with an registry. Assumes that the lane's <code>getName()</code> does
     * not return null. Otherwise throws a NullPointerException. Throws an LaneAlreadyRegisteredException
     * in case an lane with the same name is already registered.
     *
     * @param activeObject the lane to be registered with an registry
     * @return the registered lane for convenience
     */
    public <T extends CoReceiver> T register(T activeObject)
    {
        return atomic(()-> {
            String name = activeObject.getNameNotNull();
            if(activeObjectMapsByName.containsKey(name))
                throw new LaneAlreadyRegisteredException("lane with name " + name + " already registered");
            activeObjectMapsByName.put(name, activeObject);
            return activeObject;
        });
    }

    /**
     * Register an lane with an registry, which is a singleton for its class. Assumes that the active
     * object's <code>getName()</code> does not return null. Otherwise throws a NullPointerException. Throws an
     * LaneAlreadyRegisteredException in case an lane with the same name is already registered.
     *
     * Note: The lane registered using this method can only be retrieved from the registry when using
     * <code>getSingletonActiveObject()</code>
     *
     * @param activeObject lane to be registered as a singleton
     * @return the registered lane for convenience
     */
    public <T extends CoReceiver> T registerSingleton(T activeObject)
    {
        return atomic(()->
        {
            Class<? extends CoReceiver> activeObjectClass = activeObject.getClass();
            if(singletonActiveObjectsByClass.containsKey(activeObjectClass))
                throw new LaneAlreadyRegisteredException("lane of class " + activeObject.getClass() + " already registered");

            singletonActiveObjectsByClass.put(activeObjectClass, activeObject);
            return activeObject;
        });
    }

    /**
     * Deregister an lane that once was registered using as a singleton using <code>registerSingleton</code>.
     *
     * @param activeObjectClass the class of the singleton lane
     * @return true if a singleton lane
     */
    public boolean deregisterSingleton(Class<? extends CoReceiver> activeObjectClass)
    {
        return atomic(()->
        {
            return singletonActiveObjectsByClass.remove(activeObjectClass) != null;
        });
    }

    /**
     * Deregister an lane from the registry using its name <tt>activeObjectName</tt>
     *
     * @param activeObjectName name of the lane
     * @return true if an lane with the given name was registered with the registry, otherwise false
     */
    public boolean deregister(String activeObjectName)
    {
        return atomic(()-> {
            return activeObjectMapsByName.remove(activeObjectName) != null;
        });
    }

    /**
     * Deregister an coReceiver from the registry. Throws a <code>NullPointerException</code> if the
     * <code>getName()</code> method of the coReceiver returns null.
     *
     * @param coReceiver the coReceiver to be deregistered
     * @return true if the <tt>coReceiver</tt> was registered with the registry, otherwise false
     */
    public boolean deregister(CoReceiver coReceiver)
    {
       return deregister(coReceiver.getNameNotNull());
    }

    /**
     * Return an lane that has been registered as a singleton object using the
     * <code>registerSingleton()</code> method.
     *
     * @param activeObjectClass class of the singleton lane
     * @return the single instance lane of class <code>activeObjectClass</code>
     */
    public <T extends CoReceiver> T getSingletonActiveObject(Class<T> activeObjectClass)
    {
        return atomic(() -> {
            CoReceiver coReceiver = singletonActiveObjectsByClass.get(activeObjectClass);
            if (coReceiver == null)
                return null;
            if (!coReceiver.getClass().isAssignableFrom(activeObjectClass))
                throw new LaneTypeMismatchException("coReceiver with name of type " + coReceiver.getClass().getName() + " and not of expected type " + activeObjectClass.getName());
            return (T) coReceiver;
        });
    }

    /**
     * Return an lane given its <code>Signature</code> or null if no such lane
     * registered with the registry. Throws an <code>LaneTypeMismatchException</code>
     * in case the registered lane does not match the type defined in <tt>signature</tt>
     *
     * @param signature of the lane
     * @return the found lane or null if none found for the given <code>Signature</code>
     */
    public <T extends CoReceiver> T getActiveObject(Signature<T> signature)
    {
        return getActiveObject(signature.getName(), signature.getActiveObjectClass());
    }

    /**
     * Same as <tt>getCoReceiver</tt>, but does not throw an <code>LaneTypeMismatchException</code>
     * in case the registered lane does not match the type defined in <tt>signature</tt> and returns
     * null instead.
     *
     * @param signature
     * @return the found lane or null if none found for the given <code>Signature</code>
     */
    public <T extends CoReceiver> T getActiveObjectOrNull(Signature<T> signature)
    {
        if(signature == null)
            return null;
        return getActiveObjectOrNull(signature.getName(), signature.getActiveObjectClass());
    }

    /**
     * Return the lane registered with the <tt>name</tt> or null if there isn't one.
     * This method also returns null in case the class of the registered lane does not match
     * <tt>activeObjectClass</tt>
     *
     * @param name of the lane to be retrieved
     * @param activeObjectClass class of the lane to be retrieved
     * @return the lane with the given name
     */
    public <T extends CoReceiver> T getActiveObject(String name, Class<T> activeObjectClass)
    {
        return atomic(()-> {
            CoReceiver coReceiver = activeObjectMapsByName.get(name);
            if(coReceiver == null)
                return null;
            if(!coReceiver.getClass().isAssignableFrom(activeObjectClass))
                throw new LaneTypeMismatchException("coReceiver with name of type " + coReceiver.getClass().getName() + " and not of expected type " + activeObjectClass.getName());
            return (T) coReceiver;
        });
    }

    /**
     * Return the lane registered with the <tt>name</tt> or null if there isn't one.
     * This method also returns null in case the class of the registered lane does not match
     * <tt>activeObjectClass</tt>
     *
     * @param name of the lane to be retrieved
     * @param activeObjectClass class of the lane to be retrieved
     * @return the lane if any reistered with the <tt>name</tt>, otherwise null
     */
    public <T extends CoReceiver> T getActiveObjectOrNull(String name, Class<T> activeObjectClass)
    {
        return atomic(()-> {
            CoReceiver coReceiver = activeObjectMapsByName.get(name);
            if(coReceiver == null)
                return null;
            if(!coReceiver.getClass().isAssignableFrom(activeObjectClass))
                return null;
            return (T) coReceiver;
        });
    }

    /**
     * Clear all the registered maps and lanes of the registry. Invoking this methods
     * results in the registry to become completely empty. So this method should be handled with
     * care.
     */
    public void clear() {
        atomic(()-> {
            activeObjectMapsByName.clear();
            singletonActiveObjectsByClass.clear();
            concurrentMapsByName.clear();
            ListenableConcurrentMapsByName.clear();
            StmMapsByName.clear();
        });
    }

    /**
     * Remove a <code>ConcurrentMap</code> from the registry registered with the <tt>name</tt>.
     * returns the removed map if it was registered with the registry, otherwise null.
     *
     * @param name of the <code>ConcurrentMap</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the removed map if any was registered with the registry, otherwise null
     */
    public <K, V> ConcurrentMap<K, V> removeConcurrentMapNamed(String name)
    {
        return atomic(()-> {
            ConcurrentMap<?, ?> map = concurrentMapsByName.remove(name);
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ConcurrentMap<K, V>) map;
        });
    }

    /**
     * Return a <code>ConcurrentMap</code> registered with the <tt>name</tt> from the
     * registry. Returns null if no <code>ConcurrentMap</code> with the given <tt>name</tt>
     * is registered with the registry.
     *
     * @param name of the <code>ConcurrentMap</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the <code>ConcurrentMap</code> registered with the <tt>name</tt> or null if none
     */
    public <K, V> ConcurrentMap<K, V> getConcurrentMapNamed(String name)
    {
        return atomic(()-> {
            ConcurrentMap<?, ?> map = concurrentMapsByName.get(name);
            if(map == null) {
                ConcurrentMap<K, V> newMap = new ConcurrentHashMap<>();
                ConcurrentMap<K, V> previousMap = (ConcurrentMap<K, V>) concurrentMapsByName.put(name, newMap);
                if(previousMap != null)
                    return previousMap;
                return newMap;
            }
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ConcurrentMap<K, V>) map;
        });
    }

    /**
     * Remove a <code>ListenableConcurrentMap</code> from the registry registered with the <tt>name</tt>.
     * Returns the removed map if it was registered with the registry, otherwise null.
     *
     * @param name of the <code>ListenableConcurrentMap</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the removed map if any was registered with the registry, otherwise null
     */
    public <K, V> ListenableConcurrentMap<K, V> removeListenableConcurrentMapNamed(String name)
    {
        return atomic(()-> {
            ListenableConcurrentMap<?, ?> map = ListenableConcurrentMapsByName.remove(name);
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ListenableConcurrentMap<K, V>) map;
        });
    }

    public <K, V> ListenableAtomicMap<K, V> removeListenableAtomicMapNamed(String name)
    {
        return atomic(()-> {
            ListenableAtomicMap<?, ?> map = ListenableAtomicMapsByName.remove(name);
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ListenableAtomicMap<K, V>) map;
        });
    }

    /**
     * Return a <code>ListenableConcurrentMap</code> registered with the <tt>name</tt> from the
     * registry. Returns null if no <code>ListenableConcurrentMap</code> with the given <tt>name</tt>
     * is registered with the registry.
     *
     * @param name of the <code>ListenableConcurrentMap</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the <code>ListenableConcurrentMap</code> registered with the <tt>name</tt> or null if none
     */
    public <K, V> ListenableConcurrentMap<K, V> getListenableConcurrentMapNamed(String name)
    {
        return atomic(()-> {
            ListenableConcurrentMap<?, ?> map = ListenableConcurrentMapsByName.get(name);
            if(map == null) {
                ListenableConcurrentMap<K, V> newMap = new ListenableConcurrentHashMap<>(name);
                ListenableConcurrentMap<K, V> previousMap = (ListenableConcurrentMap<K, V>) ListenableConcurrentMapsByName.put(name, newMap);
                if(previousMap != null)
                    return previousMap;
                return newMap;
            }
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ListenableConcurrentMap<K, V>) map;
        });
    }

    public <K, V> ListenableAtomicMap<K, V> getListenableAtomicMapNamed(String name)
    {
        return atomic(()-> {
            ListenableAtomicMap<?, ?> map = ListenableAtomicMapsByName.get(name);
            if(map == null) {
                ListenableAtomicMap<K, V> newMap = new ListenableAtomicMap<>(name);
                ListenableAtomicMapsByName.put(name, newMap);
                return newMap;
            }
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (ListenableAtomicMap<K, V>) map;
        });
    }

    /**
     * Return a ScalaSTM Map registered with the <tt>name</tt> from the registry. Returns null if
     * no <code>ScalaSTM Map</code> with the given <tt>name</tt> is registered with the registry.
     *
     * Note: a <code>ScalaSTM Map</code> must always be accessed from within an atomic block using
     * scala.concurrent.stm.japi.STM.atomic with a lambda expression as the argument.
     *
     * @param name of the <code>ListenableConcurrentMap</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the ScalaSTM map registered with the <tt>name</tt> or null if none
     */
    public <K, V> Map<K, V> getAtomicMapNamed(String name) {
        return atomic(()-> {
            Map<?, ?> map = StmMapsByName.get(name);
            if(map == null) {
                Map<K, V> newMap = newMap();
                StmMapsByName.put(name, newMap);
                return newMap;
            }
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (Map<K, V>) map;
        });
    }

    /**
     * Remove a <code>ScalaSTM Map</code> from the registry registered with the <tt>name</tt>.
     * returns the removed map if it was registered with the registry, otherwise null.
     *
     * @param name of the <code>ScalaSTM Map</code>
     * @param <K> type of map key
     * @param <V> type of map value
     * @return the removed map if any was registered with the registry, otherwise null
     */
    public <K, V> Map<K, V> removeStmMapNamed(String name) {
        return atomic(()-> {
            Map<?, ?> map = StmMapsByName.remove(name);
            // TODO - test whether convertable as ClassCastException cannot be survived
            return (Map<K, V>) map;
        });
    }
}
