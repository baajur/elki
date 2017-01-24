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
package de.lmu.ifi.dbs.elki.evaluation.scores.adapter;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.math.MathUtil;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arrays.IntegerArrayQuickSort;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arrays.IntegerComparator;

/**
 * Class to iterate over a number vector in decreasing order.
 * 
 * @author Erich Schubert
 * @since 0.7.0
 * 
 * @apiviz.composedOf NumberVector
 */
public class IncreasingVectorIter extends AbstractVectorIter implements IntegerComparator {
  /**
   * Constructor.
   * 
   * @param vec Vector to iterate over.
   */
  public IncreasingVectorIter(NumberVector vec) {
    super(vec);
    this.sort = MathUtil.sequence(0, vec.getDimensionality());
    IntegerArrayQuickSort.sort(sort, this);
  }

  @Override
  public int compare(int x, int y) {
    return Double.compare(vec.doubleValue(x), vec.doubleValue(y));
  }

  @Override
  public IncreasingVectorIter advance(int count) {
    pos += count;
    return this;
  }

  @Override
  public IncreasingVectorIter retract() {
    pos--;
    return this;
  }

  @Override
  public IncreasingVectorIter seek(int off) {
    pos = off;
    return this;
  }
}