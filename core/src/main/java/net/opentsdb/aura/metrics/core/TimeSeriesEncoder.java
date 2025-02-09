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

package net.opentsdb.aura.metrics.core;

public interface TimeSeriesEncoder extends LazyStatsCollector {

  long createSegment(int segmentTime);

  void openSegment(long segmentAddress);

  void addDataPoint(int timestamp, double value);

  void read(TSDataConsumer consumer);

  /**
   * Decodes the segments, sorts and removes the duplicate data points. Subsequent data points will override the previous values.
   * It offsets the timestamp from the segment time and uses that as the index in the array to store the values.
   * So, the length of the array should be equal to the number of seconds in the segment. For two hour segment, the array should be of length 7200.
   *
   * @param valueBuffer values array of size == seconds in the segment.
   * @return count of unique data points
   */
  int readAndDedupe(double[] valueBuffer);

  int getSegmentTime();

  int getNumDataPoints();

  void freeSegment();

  void collectSegment(long segmentAddress);

  void freeCollectedSegments();

  boolean segmentIsDirty();

  boolean segmentHasOutOfOrderOrDuplicates();

  void markSegmentFlushed();

  /**
   * Determines how many bytes are needed to serialize the current segment. If
   * the segment doesn't have any data (and that shouldn't happen as we should
   * not have created a segment if we don't have data to write).
   *
   * @return A positive value reflecting the number of bytes to serialize.
   */
  int serializationLength();

  /**
   * Serializes the segment into the given byte buffer. For gorilla the serialized
   * data is as follows:
   * <ul>
   *     <li><b>1b</b> - Encoding type</li>
   *     <li><b>1 to 2b</b> - The number of data points. The MSB is a flag that
   *     when set, means there are 2 bytes for the length and if not set the
   *     length is only one byte. Make sure to mask this bit on reading.</li>
   *     <li><b>nb</b> - The gorilla encoded data. Note that Aura uses a long
   *     array and some bits/bytes from the final long may not be used. Those
   *     are dropped in this serialization and must be padded on read.</li>
   * </ul>
   * <b>NOTE</b> - The serialization does not include the data length or
   * references to segment timestamp or last time, leading or trailing zeros,
   * etc. The length and segment time must be handled by the flusher.
   *
   * @param buffer The non-null buffer to write to.
   * @param offset The starting offset to write to.
   * @param length The length of data we will be serializing.
   *               From {@link #serializationLength()}
   */
  void serialize(byte[] buffer, int offset, int length);
}
