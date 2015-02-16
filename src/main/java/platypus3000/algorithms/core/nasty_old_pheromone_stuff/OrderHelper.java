package platypus3000.algorithms.core.nasty_old_pheromone_stuff;

import java.util.*;

/**
* Created by m on 21.07.14.
*/
public class OrderHelper {

    private OrderHelper() { }

    public static <T extends Comparable<T>> List<T> getMaximalElements(Collection<T> set) {
        return getEqualElements(set, Collections.max(set));
        /*
        //This implementation might be faster, but I am too lazy to test it. They are both in O(n) anyways
        Set<T> maximalElements = new HashSet<T>();
        T someMaxElement = null;
        for(T element : set) {
            if(someMaxElement == null || someMaxElement.compareTo(element) == 0) {
                maximalElements.add(element);
                someMaxElement = element;
            }
            if(someMaxElement.compareTo(element) < 0) {
                maximalElements.clear();
                maximalElements.add(element);
                someMaxElement = element;
            }
        }
        return maximalElements;*/
    }

    public static <T> List<T> getMaximalElements(Collection<T> set, Comparator<T> comparator) {
        return getEqualElements(set, Collections.max(set, comparator), comparator);
    }

    public static <T extends Comparable<T>> List<T> getMinimalElements(Collection<T> set) {
        return getEqualElements(set, Collections.min(set));
    }

    public static <T> List<T> getMinimalElements(Collection<T> set, Comparator<T> comparator) {
        return getEqualElements(set, Collections.min(set, comparator), comparator);
    }

    public static <T> List<T> getEqualElements(Collection<T> set, T element, Comparator<T> comparator) {
        List<T> equalElements = new ArrayList<T>();
        for(T e : set)
            if(comparator.compare(element, e) == 0)
                equalElements.add(e);
        return equalElements;
    }

    public static <T extends Comparable<T>> List<T> getEqualElements(Collection<T> set, T element) {
        List<T> equalElements = new ArrayList<T>();
        for(T e : set)
            if(element.compareTo(e) == 0)
                equalElements.add(e);
        return equalElements;
    }


}
