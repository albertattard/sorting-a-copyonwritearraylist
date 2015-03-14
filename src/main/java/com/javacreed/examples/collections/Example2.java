/*
 * #%L
 * Sorting a CopyOnWriteArrayList
 * $Id:$
 * $HeadURL: https://java-creed-examples.googlecode.com/svn/collections/Sorting%20a%20CopyOnWriteArrayList/src/main/java/com/javacreed/examples/collections/Example2.java $
 * %%
 * Copyright (C) 2012 - 2014 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * Copyright 2012-2014 Java Creed.
 *
 * Licensed under the Apache License, Version 2.0 (the "<em>License</em>");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.javacreed.examples.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Example2 {

  public static void main(final String[] args) {
    final List<String> list = new CopyOnWriteArrayList<>();
    CollectionsUtil.addInOrder(list, "3");
    CollectionsUtil.addInOrder(list, "2");
    CollectionsUtil.addInOrder(list, "1");
    CollectionsUtil.addInOrder(list, "3");

    System.out.println(list);
  }
}