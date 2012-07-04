package de.lmu.ifi.dbs.elki.index;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2012
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.query.DistanceDBIDResult;
import de.lmu.ifi.dbs.elki.database.query.DistanceDBIDResultIter;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNResult;
import de.lmu.ifi.dbs.elki.database.query.knn.LinearScanKNNQuery;
import de.lmu.ifi.dbs.elki.database.query.range.LinearScanRangeQuery;
import de.lmu.ifi.dbs.elki.database.query.range.RangeQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.index.tree.TreeIndexFactory;
import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.mtree.MTree;
import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.mtree.MTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.query.MetricalIndexKNNQuery;
import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.query.MetricalIndexRangeQuery;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRStarTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.query.DoubleDistanceRStarTreeKNNQuery;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.query.DoubleDistanceRStarTreeRangeQuery;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTree;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.insert.ApproximativeLeastOverlapInsertionStrategy;
import de.lmu.ifi.dbs.elki.index.vafile.PartialVAFile;
import de.lmu.ifi.dbs.elki.index.vafile.VAFile;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.ParameterException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Test case to validate some index structures for accuracy. For a known data
 * set and query point, the top 10 nearest neighbors are queried and verified.
 * 
 * Note that the internal operation of the index structure is not tested this
 * way, only whether the database object with the index still returns reasonable
 * results.
 * 
 * @author Erich Schubert
 */
public class TestIndexStructures implements JUnit4Test {
  // the following values depend on the data set used!
  String dataset = "data/testdata/unittests/hierarchical-3d2d1d.csv";

  // size of the data set
  int shoulds = 600;

  // query point
  double[] querypoint = new double[] { 0.5, 0.5, 0.5 };

  // number of kNN to query
  int k = 10;

  // the 10 next neighbors of the query point
  double[][] shouldc = new double[][] { { 0.45000428746088883, 0.484504234161508, 0.5538595167151342 }, { 0.4111050036231091, 0.429204794352013, 0.4689430202460606 }, { 0.4758477631164003, 0.6021538103067177, 0.5556807408692025 }, { 0.4163288957164025, 0.49604545242979536, 0.4054361013566713 }, { 0.5819940640461848, 0.48586944418231115, 0.6289592025558619 }, { 0.4373568207802466, 0.3468650110814596, 0.49566951629699485 }, { 0.40283109564192643, 0.6301433694690401, 0.44313571161129883 }, { 0.6545840114867083, 0.4919617658889418, 0.5905461546078652 }, { 0.6011097673869055, 0.6562921241634017, 0.44830647520493694 }, { 0.5127485678175534, 0.29708449200895504, 0.561722374659424 }, };

  // and their distances
  double[] shouldd = new double[] { 0.07510351238126374, 0.11780839322826206, 0.11882371989803064, 0.1263282354232315, 0.15347043712184602, 0.1655090505771259, 0.17208323533934652, 0.17933052146586306, 0.19319066655063877, 0.21247795391113142 };

  DoubleDistance eps = new DoubleDistance(0.21247795391113142);

  /**
   * Test exact query, also to validate the test is correct.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testExact() {
    ListParameterization params = new ListParameterization();
    testFileBasedDatabaseConnection(params, LinearScanKNNQuery.class, LinearScanRangeQuery.class);
  }

  /**
   * Test {@link MTree} using a file based database connection.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testMetrical() {
    ListParameterization metparams = new ListParameterization();
    metparams.addParameter(StaticArrayDatabase.INDEX_ID, MTreeFactory.class);
    metparams.addParameter(TreeIndexFactory.PAGE_SIZE_ID, 100);
    testFileBasedDatabaseConnection(metparams, MetricalIndexKNNQuery.class, MetricalIndexRangeQuery.class);
  }

  /**
   * Test {@link RStarTree} using a file based database connection.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testRStarTree() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.INDEX_ID, RStarTreeFactory.class);
    spatparams.addParameter(TreeIndexFactory.PAGE_SIZE_ID, 300);
    testFileBasedDatabaseConnection(spatparams, DoubleDistanceRStarTreeKNNQuery.class, DoubleDistanceRStarTreeRangeQuery.class);
  }

  /**
   * Test {@link VAFile} using a file based database connection.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testVAFile() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.INDEX_ID, VAFile.Factory.class);
    spatparams.addParameter(VAFile.Factory.PARTITIONS_ID, 4);
    testFileBasedDatabaseConnection(spatparams, VAFile.VAFileKNNQuery.class, VAFile.VAFileRangeQuery.class);
  }

  /**
   * Test {@link PartialVAFile} using a file based database connection.
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testPartialVAFile() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.INDEX_ID, PartialVAFile.Factory.class);
    spatparams.addParameter(PartialVAFile.Factory.PARTITIONS_ID, 4);
    testFileBasedDatabaseConnection(spatparams, PartialVAFile.PartialVAFileKNNQuery.class, PartialVAFile.PartialVAFileRangeQuery.class);
  }

  /**
   * Test {@link RStarTree} using a file based database connection. With "fast"
   * mode enabled on an extreme level (since this should only reduce
   * performance, not accuracy!)
   * 
   * @throws ParameterException on errors.
   */
  @Test
  public void testRStarTreeFast() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.INDEX_ID, RStarTreeFactory.class);
    spatparams.addParameter(AbstractRStarTreeFactory.INSERTION_STRATEGY_ID, ApproximativeLeastOverlapInsertionStrategy.class);
    spatparams.addParameter(ApproximativeLeastOverlapInsertionStrategy.Parameterizer.INSERTION_CANDIDATES_ID, 1);
    spatparams.addParameter(TreeIndexFactory.PAGE_SIZE_ID, 300);
    testFileBasedDatabaseConnection(spatparams, DoubleDistanceRStarTreeKNNQuery.class, DoubleDistanceRStarTreeRangeQuery.class);
  }

  /**
   * Test {@link XTree} using a file based database connection.
   * 
   * @throws ParameterException
   */
  // @Test
  // public void testXTree() {
  // ListParameterization xtreeparams = new ListParameterization();
  // xtreeparams.addParameter(StaticArrayDatabase.INDEX_ID,
  // experimentalcode.shared.index.xtree.XTreeFactory.class);
  // xtreeparams.addParameter(TreeIndexFactory.PAGE_SIZE_ID, 300);
  // testFileBasedDatabaseConnection(xtreeparams,
  // DoubleDistanceRStarTreeKNNQuery.class,
  // DoubleDistanceRStarTreeRangeQuery.class);
  // }

  /**
   * Actual test routine.
   * 
   * @param inputparams
   * @throws ParameterException
   */
  void testFileBasedDatabaseConnection(ListParameterization inputparams, Class<?> expectKNNQuery, Class<?> expectRangeQuery) {
    inputparams.addParameter(FileBasedDatabaseConnection.INPUT_ID, dataset);

    // get database
    Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, inputparams);
    db.initialize();
    Relation<DoubleVector> rep = db.getRelation(TypeUtil.DOUBLE_VECTOR_FIELD);
    DistanceQuery<DoubleVector, DoubleDistance> dist = db.getDistanceQuery(rep, EuclideanDistanceFunction.STATIC);

    // verify data set size.
    assertTrue(rep.size() == shoulds);

    {
      // get the 10 next neighbors
      DoubleVector dv = new DoubleVector(querypoint);
      KNNQuery<DoubleVector, DoubleDistance> knnq = db.getKNNQuery(dist, k);
      assertTrue("Returned knn query is not of expected class.", expectKNNQuery.isAssignableFrom(knnq.getClass()));
      KNNResult<DoubleDistance> ids = knnq.getKNNForObject(dv, k);
      assertEquals("Result size does not match expectation!", shouldd.length, ids.size());

      // verify that the neighbors match.
      int i = 0;
      for(DistanceDBIDResultIter<DoubleDistance> res = ids.iter(); res.valid(); res.advance(), i++) {
        // Verify distance
        assertEquals("Expected distance doesn't match.", shouldd[i], res.getDistance().doubleValue());
        // verify vector
        DoubleVector c = rep.get(res);
        DoubleVector c2 = new DoubleVector(shouldc[i]);
        assertEquals("Expected vector doesn't match: " + c.toString(), 0.0, dist.distance(c, c2).doubleValue(), 0.00001);
      }
    }
    {
      // Do a range query
      DoubleVector dv = new DoubleVector(querypoint);
      RangeQuery<DoubleVector, DoubleDistance> rangeq = db.getRangeQuery(dist, eps);
      assertTrue("Returned range query is not of expected class.", expectRangeQuery.isAssignableFrom(rangeq.getClass()));
      DistanceDBIDResult<DoubleDistance> ids = rangeq.getRangeForObject(dv, eps);
      assertEquals("Result size does not match expectation!", shouldd.length, ids.size());

      // verify that the neighbors match.
      int i = 0;
      for(DistanceDBIDResultIter<DoubleDistance> res = ids.iter(); res.valid(); res.advance(), i++) {
        // Verify distance
        assertEquals("Expected distance doesn't match.", shouldd[i], res.getDistance().doubleValue());
        // verify vector
        DoubleVector c = rep.get(res);
        DoubleVector c2 = new DoubleVector(shouldc[i]);
        assertEquals("Expected vector doesn't match: " + c.toString(), 0.0, dist.distance(c, c2).doubleValue(), 0.00001);
      }
    }
  }
}