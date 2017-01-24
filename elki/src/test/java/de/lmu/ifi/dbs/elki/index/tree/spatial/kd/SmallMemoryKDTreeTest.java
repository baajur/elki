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
package de.lmu.ifi.dbs.elki.index.tree.spatial.kd;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.index.AbstractIndexStructureTest;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Unit test for the k-d-tree index.
 *
 * @author Erich Schubert
 * @since 0.4.0
 */
public class SmallMemoryKDTreeTest extends AbstractIndexStructureTest {
  /**
   * Test {@link SmallMemoryKDTree} using a file based database connection.
   */
  @Test
  public void testSmallMemoryKDTree() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.Parameterizer.INDEX_ID, SmallMemoryKDTree.Factory.class);
    testExactEuclidean(spatparams, SmallMemoryKDTree.KDTreeKNNQuery.class, SmallMemoryKDTree.KDTreeRangeQuery.class);
  }
}
