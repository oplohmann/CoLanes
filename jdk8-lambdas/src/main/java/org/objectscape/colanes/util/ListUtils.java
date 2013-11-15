package org.objectscape.colanes.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 08.06.13
 * Time: 09:49
 * To change this template use File | Settings | File Templates.
 */
public interface ListUtils {

    default public <T> ImmutableList<T> immutable(List<T> list) {
        return new ImmutableList<>(list);
    }

    default public <T> ImmutableList<T> immutableOrNull(List<T> listOrNull) {
        if(listOrNull == null)
            return null;
        return new ImmutableList<>(listOrNull);
    }

    default public <T> ImmutableList<T> immutableList(T object) {
        return new ImmutableList<>(object);
    }

    default public <T> ImmutableSet<T> immutableSet(Set<T> set) {
        return new ImmutableSet<>(set);
    }

    default public <T> List<T> list(T object) {
        List<T> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    default public <T> Set<T> set(T object) {
        Set<T> set = new HashSet<>();
        set.add(object);
        return set;
    }

}
