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

import kamon.system.process.{ProcessCpuMetrics, ULimitMetrics}
import kamon.system.{Metric, withNamedThread}
import org.slf4j.Logger

class OshiMetricsUpdater(logger: Logger) extends Runnable {

  val metrics: Seq[Metric] =
    Seq(
      CpuMetrics.register(),
      FileSystemMetrics.register(),
      LoadAverageMetrics.register(),
      MemoryMetrics.register(),
      NetworkMetrics.register(),
      ProcessCpuMetrics.register(),
      ULimitMetrics.register()
    ).flatten


  override def run(): Unit = withNamedThread("sigar-metric-updater"){
    metrics.foreach(_.update())
  }
}

object OshiSafeRunner {

  def runSafe[T](thunk: ⇒ T, defaultValue: ⇒ T, metricName: String, logger: Logger): T = {
    try thunk catch {
      case e: Exception ⇒
        logger.warn(s"Couldn't get the metric [$metricName]. Due to [${e.getMessage}]")
        defaultValue
    }
  }
}
