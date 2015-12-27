Sorting a <code>List</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/List.html" target="_blank">Java Doc</a>) is a very common task and the Java Collections Framework (<a href="http://docs.oracle.com/javase/7/docs/technotes/guides/collections/" target="_blank">Guides</a>) provides methods that perform this, together with others useful functionalities, as shown in the following code fragment.


<pre>
List&lt;String&gt; list = ...
Collections.sort(list);
</pre>


Unfortunately the above method will not work on all instances of type <code>List</code>, such as <code>CopyOnWriteArrayList</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html" target="_blank">Java Doc</a>), as we will see in the following examples.


All code listed below is available at: <a href="https://github.com/javacreed/sorting-a-copyonwritearraylist" target="_blank">https://github.com/javacreed/sorting-a-copyonwritearraylist</a>.  Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed. The readers can download or view all code from the above link.


<h2>Sorting CopyOnWriteArrayList</h2>


Consider the following example.

<pre>
package com.javacreed.examples.collections;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Example1 {

  public static void main(final String[] args) {
    final List&lt;String&gt; list = new CopyOnWriteArrayList&lt;&gt;();
    list.add("3");
    list.add("2");
    list.add("1");

    Collections.sort(list);
  }
}
</pre>


Here we are creating a list with some items of type <code>String</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/lang/String.html" target="_blank">Java Doc</a>) and then using the <code>Collections.sort()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#sort(java.util.List)" target="_blank">Java Doc</a>) method to sort them.


This example will fail with the following exception when executed.


<pre>
Exception in thread "main" java.lang.UnsupportedOperationException
	at java.util.concurrent.CopyOnWriteArrayList$COWIterator.set(CopyOnWriteArrayList.java:1049)
	at java.util.Collections.sort(Collections.java:159)
	at com.javacreed.examples.collections.Example1.main(Example1.java:15)
</pre>


Unfortunately, we cannot sort the items within the <code>CopyOnWriteArrayList</code> as the <code>ListIterator</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html" target="_blank">Java Doc</a>) instance returned by this list cannot be modified.  The following code fragment shows how the <code>Collections.sort()</code> method works.


<pre>
  public static &lt;T extends Comparable&lt;? super T&gt;&gt; void sort(List&lt;T&gt; list) {
    Object[] a = list.toArray();
    Arrays.sort(a);
    ListIterator&lt;T&gt; i = list.listIterator();
    for (int j=0; j&lt;a.length; j++) {
      i.next();
      <span class="highlight">i.set((T)a[j]);</span>
    }
  }
</pre>


The list is first converted into an array of objects and sorted.  Then it sets the elements in the correct order using the <code>ListIterator.set()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html#set(E)" target="_blank">Java Doc</a>) method.  This method is an optional one as documented in the method documentation shown next.


<blockquote cite="http://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html#set(E)">
Replaces the last element returned by <code>next()</code> or <code>previous()</code> with the specified element (<strong>optional operation</strong>).  This call can be made only if neither <code>remove()</code> nor <code>add(E)</code> have been called after the last call to next or previous.
</blockquote>


In the following section we will see how we can has the elements in the list in the desired order using a different technique to the one we just tried out now.


<h2>Ordered List</h2>


One solution to our problem is to keep the items in the correct order inthe first place.  This is not always what we want as we may need to sort the same list using different order (using different <code>Comparator</code>s (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/Comparator.html" target="_blank">Java Doc</a>)).  But as the saying goes, "<em>you cannot have your cake and eat it too</em>".  The <code>CopyOnWriteArrayList</code> returns an immutable <code>ListIterator</code>, which prevents us from using the <code>Collections.sort()</code> method.


The following utility class defines a simple method that inserts the given item in the correct order.


<pre>
package com.javacreed.examples.collections;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class CollectionsUtil {

  public static &lt;T extends Comparable&lt;T&gt;&gt; int addInOrder(final List&lt;T&gt; list, final T item) {
    final int insertAt;
    <span class="comments">// The index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1)</span>
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
</pre>


The method <code>addInOrder()</code> shown above uses the <code>Collections.binarySearch()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#binarySearch(java.util.List, T)" target="_blank">Java Doc</a>) method to locate the index of this item.  If the item (or a similar one) is already in the list (the index returned by the binary search method is positive), then simply add the new item next to the existing one.  For example, if another similar item already exists at index <code>3</code>, then the given item is added at index <code>4</code>.  On the other hand, if the item is not already in the list, the <code>Collections.binarySearch()</code> will return a negative index.  This negative index has a very important meaning as it provides the location where this item should be inserted in order to maintain the list sorted.  The insertion point is determined by the following code.


<pre>
      insertAt = -(index + 1);
</pre>


The given item is added to the list in this location.  This value is also returned to indicate where the item was added, should that be required.  Following is an example of how this method can be used.


<pre>
package com.javacreed.examples.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Example2 {

  public static void main(final String[] args) {
    final List&lt;String&gt; list = new CopyOnWriteArrayList&lt;&gt;();
    CollectionsUtil.addInOrder(list, "3");
    CollectionsUtil.addInOrder(list, "2");
    CollectionsUtil.addInOrder(list, "1");

    <span class="comments">//Insert an item similar to an existing one</span>
    CollectionsUtil.addInOrder(list, "3");

    System.out.println(list);
  }
}
</pre>


The above example will yield the following to the command prompt.


<pre>
[1, 2, 3, 3]
</pre>


Note that, irrespective of their insertion order, the items are displayed in the natural order.


<h2>Testing</h2>


Before we conclude this article we need to test our solution to make sure that it works as expected.  The following test case does this.


<pre>
package com.javacreed.examples.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.junit.Test;

public class CollectionsUtilTest {

  @Test
  public void test() {
    final List&lt;String&gt; list = new CopyOnWriteArrayList&lt;&gt;();

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
</pre>


We test the insertion of three items.  These are added in the reverse order and thus they will always be added at location 0.  Then we add a duplicate item to make sure that this is added again in the correct location.  Finally we verify that the list contains all items and that each item is in the expected location.


<h2>Conclusion</h2>


Due to its design, the <code>CopyOnWriteArrayList</code> cannot be sorted using traditional methods.  Furthermore, sorting them using other algorithms may prove very slow and inefficient as the <code>CopyOnWriteArrayList</code> creates a new list for every modification.  Using the approach showed in this article, we can have an ordered list with little effort.  With that said, the insertion process is slowed down further as a binary search is executed before every insert.
