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

package kamon.system.process

import kamon.Kamon
import kamon.system.{Metric, MetricBuilder, OshiMetricBuilder}
import org.slf4j.Logger
import oshi.SystemInfo

object ULimitMetrics extends MetricBuilder("process.ulimit") with OshiMetricBuilder {

  override def build(oshi: SystemInfo, metricName: String, logger: Logger): Metric = new Metric {
    val pid = oshi.getOperatingSystem.getProcessId
    val ulimitMetric = Kamon.histogram(metricName)
    val openFilesMetric = ulimitMetric.refine(Map("component" -> "system-metrics", "limit" -> "open-files"))

    override def update(): Unit = {
      import kamon.system.host.OshiSafeRunner._

      openFilesMetric.record(runSafe(oshi.getOperatingSystem.getProcess(pid).getOpenFiles, 0L, "open-files", logger))
    }
  }
}
