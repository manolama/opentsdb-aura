/*
 * This file is part of OpenTSDB.
 * Copyright (C) 2021  Yahoo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.opentsdb.aura.metrics.core.gorilla;

import net.opentsdb.aura.metrics.core.SegmentCollector;
import net.opentsdb.aura.metrics.core.TimeSeriesEncoderFactory;
import io.ultrabrew.metrics.MetricRegistry;

public class GorillaTimeSeriesEncoderFactory
    implements TimeSeriesEncoderFactory<GorillaTimeSeriesEncoder> {

  private boolean lossy;
  private int garbageQSize;
  private int segmentCollectionDelayMinutes;
  private MetricRegistry metricRegistry;
  private GorillaSegmentFactory segmentFactory;

  public GorillaTimeSeriesEncoderFactory(
      final boolean lossy, final int garbageQSize, final int segmentCollectionDelayMinutes,
      final MetricRegistry metricRegistry, final GorillaSegmentFactory segmentFactory) {
    this.lossy = lossy;
    this.garbageQSize = garbageQSize;
    this.segmentCollectionDelayMinutes = segmentCollectionDelayMinutes;
    this.metricRegistry = metricRegistry;
    this.segmentFactory = segmentFactory;
  }

  @Override
  public GorillaTimeSeriesEncoder create() {

    GorillaSegment segmentHandle = segmentFactory.create();
    SegmentCollector segmentCollector =
        new SegmentCollector(
            garbageQSize,
            segmentCollectionDelayMinutes,
            segmentHandle,
            metricRegistry);
    return new GorillaTimeSeriesEncoder(
        lossy, metricRegistry, segmentHandle, segmentCollector);
  }

}
