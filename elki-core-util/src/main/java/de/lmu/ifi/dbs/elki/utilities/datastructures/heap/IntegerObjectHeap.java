/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.utilities.datastructures.heap;

import de.lmu.ifi.dbs.elki.utilities.datastructures.iterator.Iter;

/**
 * Basic in-memory heap interface, for int keys and V values.
 * 
 * @author Erich Schubert
 * @since 0.5.5
 * 
 * @apiviz.has UnsortedIter
 * @param <V> Value type
 */
public interface IntegerObjectHeap<V> {
  /**
   * Add a key-value pair to the heap
   * 
   * @param key Key
   * @param val Value
   */
  void add(int key, V val);

  /**
   * Add a key-value pair to the heap if it improves the top.
   * 
   * @param key Key
   * @param val Value
   * @param k Desired maximum size
   */
  void add(int key, V val, int k);

  /**
   * Combined operation that removes the top element, and inserts a new element
   * instead.
   * 
   * @param key Key of new element
   * @param val Value of new element
   */
  void replaceTopElement(int key, V val);

  /**
   * Get the current top key
   * 
   * @return Top key
   */
  int peekKey();

  /**
   * Get the current top value
   * 
   * @return Value
   */
  V peekValue();

  /**
   * Remove the first element
   */
  void poll();

  /**
   * Clear the heap contents.
   */
  void clear();

  /**
   * Query the size
   * 
   * @return Size
   */
  public int size();
  
  /**
   * Is the heap empty?
   * 
   * @return {@code true} when the size is 0.
   */
  public boolean isEmpty();

  /**
   * Get an unsorted iterator to inspect the heap.
   * 
   * @return Iterator
   */
  UnsortedIter<V> unsortedIter();

  /**
   * Unsorted iterator - in heap order. Does not poll the heap.
   * 
   * @author Erich Schubert
 * @param <V> Value type
   */
  public static interface UnsortedIter<V> extends Iter {
    /**
     * Get the current key
     * 
     * @return Current key
     */
    int getKey();

    /**
     * Get the current value
     * 
     * @return Current value
     */
    V getValue();
  }
}
