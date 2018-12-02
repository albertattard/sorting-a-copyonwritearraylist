Sorting a `List` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/List.html)) is a very common task and the Java Collections Framework ([Guides](http://docs.oracle.com/javase/7/docs/technotes/guides/collections/)) provides methods that perform this, together with others useful functionalities, as shown in the following code fragment.

```java
List<String> list = ...
Collections.sort(list);
```

Unfortunately the above method will not work on all instances of type `List`, such as `CopyOnWriteArrayList` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html)), as we will see in the following examples.

All code listed below is available at: [https://github.com/javacreed/sorting-a-copyonwritearraylist](https://github.com/javacreed/sorting-a-copyonwritearraylist).  Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed. The readers can download or view all code from the above link.

## Sorting CopyOnWriteArrayList

Consider the following example.

```java
package com.javacreed.examples.collections;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Example1 {

  public static void main(final String[] args) {
    final List<String> list = new CopyOnWriteArrayList<>();
    list.add("3");
    list.add("2");
    list.add("1");

    Collections.sort(list);
  }
}
```

Here we are creating a list with some items of type `String` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/lang/String.html)) and then using the `Collections.sort()` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#sort(java.util.List))) method to sort them.

This example will fail with the following exception when executed.

```
Exception in thread "main" java.lang.UnsupportedOperationException
	at java.util.concurrent.CopyOnWriteArrayList$COWIterator.set(CopyOnWriteArrayList.java:1049)
	at java.util.Collections.sort(Collections.java:159)
	at com.javacreed.examples.collections.Example1.main(Example1.java:15)
```

Unfortunately, we cannot sort the items within the `CopyOnWriteArrayList` as the `ListIterator` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html)) instance returned by this list cannot be modified.  The following code fragment shows how the `Collections.sort()` method works.

```java
  public static <T extends Comparable<? super T>> void sort(List<T> list) {
    Object[] a = list.toArray();
    Arrays.sort(a);
    ListIterator<T> i = list.listIterator();
    for (int j=0; j<a.length; j++) {
      i.next();
      i.set((T)a[j]);
    }
  }
```

The list is first converted into an array of objects and sorted.  Then it sets the elements in the correct order using the `ListIterator.set()` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html#set(E))) method.  This method is an optional one as documented in the method documentation shown next.

> Replaces the last element returned by `next()` or `previous()` with the specified element (**optional operation**).  This call can be made only if neither `remove()` nor `add(E)` have been called after the last call to next or previous. ([reference](http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html#set(E)))

In the following section we will see how we can has the elements in the list in the desired order using a different technique to the one we just tried out now.

## Ordered List

One solution to our problem is to keep the items in the correct order in the first place.  This is not always what we want as we may need to sort the same list using different order (using different `Comparator`s ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/Comparator.html))).  But as the saying goes, "_you cannot have your cake and eat it too_".  The `CopyOnWriteArrayList` returns an immutable `ListIterator`, which prevents us from using the `Collections.sort()` method.

The following utility class defines a simple method that inserts the given item in the correct order.

```java
package com.javacreed.examples.collections;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class CollectionsUtil {

  public static <T extends Comparable<T>> int addInOrder(final List<T> list, final T item) {
    final int insertAt;
    /* The index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1) */
    final int index = Collections.binarySearch(list, item);
    if (index < 0) {
      insertAt = -(index + 1);
    } else {
      insertAt = index + 1;
    }

    list.add(insertAt, item);
    return insertAt;
  }

  private CollectionsUtil() {
  }
}
```

The method `addInOrder()` shown above uses the `Collections.binarySearch()` ([Java Doc](http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#binarySearch(java.util.List,%20T))) method to locate the index of this item.  If the item (or a similar one) is already in the list (the index returned by the binary search method is positive), then simply add the new item next to the existing one.  For example, if another similar item already exists at index `3`, then the given item is added at index `4`.  On the other hand, if the item is not already in the list, the `Collections.binarySearch()` will return a negative index.  This negative index has a very important meaning as it provides the location where this item should be inserted in order to maintain the list sorted.  The insertion point is determined by the following code.

```java
      insertAt = -(index + 1);
```

The given item is added to the list in this location.  This value is also returned to indicate where the item was added, should that be required.  Following is an example of how this method can be used.

```java
package com.javacreed.examples.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Example2 {

  public static void main(final String[] args) {
    final List<String> list = new CopyOnWriteArrayList<>();
    CollectionsUtil.addInOrder(list, "3");
    CollectionsUtil.addInOrder(list, "2");
    CollectionsUtil.addInOrder(list, "1");

    /* Insert an item similar to an existing one */
    CollectionsUtil.addInOrder(list, "3");

    System.out.println(list);
  }
}
```

The above example will yield the following to the command prompt.

```
[1, 2, 3, 3]
```

Note that, irrespective of their insertion order, the items are displayed in the natural order.

## Testing

Before we conclude this article we need to test our solution to make sure that it works as expected.  The following test case does this.

```java
package com.javacreed.examples.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.junit.Test;

public class CollectionsUtilTest {

  @Test
  public void test() {
    final List<String> list = new CopyOnWriteArrayList<>();

    Assert.assertEquals(0, CollectionsUtil.addInOrder(list, "3"));
    Assert.assertEquals(0, CollectionsUtil.addInOrder(list, "2"));
    Assert.assertEquals(0, CollectionsUtil.addInOrder(list, "1"));

    Assert.assertEquals(3, CollectionsUtil.addInOrder(list, "3"));

    Assert.assertEquals(4, list.size());
    Assert.assertEquals("1", list.get(0));
    Assert.assertEquals("2", list.get(1));
    Assert.assertEquals("3", list.get(2));
    Assert.assertEquals("3", list.get(3));
  }
}
```

We test the insertion of three items.  These are added in the reverse order and thus they will always be added at location 0.  Then we add a duplicate item to make sure that this is added again in the correct location.  Finally we verify that the list contains all items and that each item is in the expected location.

## Conclusion

Due to its design, the `CopyOnWriteArrayList` cannot be sorted using traditional methods.  Furthermore, sorting them using other algorithms may prove very slow and inefficient as the `CopyOnWriteArrayList` creates a new list for every modification.  Using the approach showed in this article, we can have an ordered list with little effort.  With that said, the insertion process is slowed down further as a binary search is executed before every insert.
