/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.system.host

import kamon.Kamon
import kamon.metric.MeasurementUnit
import kamon.system.{Metric, MetricBuilder, OshiMetricBuilder}
import org.slf4j.Logger
import oshi.SystemInfo

/**
 *  System memory usage metrics, as reported by Oshi:
 *    - used: Total used system memory.
 *    - free: Total free system memory (e.g. Linux plus cached).
 *    - swap-used: Total used system swap.
 *    - swap-free: Total free system swap.
 */
object MemoryMetrics extends MetricBuilder("host.memory") with OshiMetricBuilder {

  override def build(oshi: SystemInfo, metricName: String, logger: Logger): Metric = new Metric {
    val memoryUsageMetric = Kamon.histogram(metricName, MeasurementUnit.information.bytes)
    val swapUsageMetric = Kamon.histogram("host.swap", MeasurementUnit.information.bytes)

    val usedMetric      = memoryUsageMetric.refine(Map("component" -> "system-metrics", "mode" -> "used"))
    val freeMetric      = memoryUsageMetric.refine(Map("component" -> "system-metrics", "mode" -> "free"))
    val totalMetric     = memoryUsageMetric.refine(Map("component" -> "system-metrics", "mode" -> "total"))

    val swapUsedMetric  = swapUsageMetric.refine(Map("component" -> "system-metrics", "mode" -> "used"))
    val swapFreeMetric  = swapUsageMetric.refine(Map("component" -> "system-metrics", "mode" -> "free"))

    override def update(): Unit = {
      import OshiSafeRunner._

      def mem = {
        val mem = oshi.getHardware.getMemory
        (mem.getAvailable, mem.getTotal)
      }

      def swap = {
        val swap = oshi.getHardware.getMemory
        (swap.getSwapUsed, swap.getSwapTotal)
      }

      val (memAvailable, memTotal) = runSafe(mem, (0L, 0L), "memory", logger)
      val (memSwapUsed, memSwapTotal) = runSafe(swap, (0L, 0L), "swap", logger)

      usedMetric.record(memTotal - memAvailable)
      freeMetric.record(memAvailable)
      totalMetric.record(memTotal)
      swapUsedMetric.record(memSwapUsed)
      swapFreeMetric.record(memSwapTotal - memSwapUsed)
    }
  }
}
