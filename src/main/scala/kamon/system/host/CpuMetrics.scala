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
import kamon.metric.Histogram
import kamon.system.{Metric, MetricBuilder, OshiMetricBuilder}
import org.slf4j.Logger
import oshi.SystemInfo

/**
 *  Cpu usage metrics, as reported by Sigar:
 *    - user: Total percentage of system cpu user time.
 *    - system: Total percentage of system cpu kernel time.
 *    - wait: Total percentage of system cpu io wait time.
 *    - idle:  Total percentage of system cpu idle time
 *    - stolen: Total percentage of system cpu involuntary wait time. @see [[https://www.datadoghq.com/2013/08/understanding-aws-stolen-cpu-and-how-it-affects-your-apps/ "Understanding Stolen Cpu"]]
 *    - combined:  Total percentage of user + system + nice + wait CPU usage.
  */
object CpuMetrics extends MetricBuilder("host.cpu") with OshiMetricBuilder {

  val emptyCpuData = Array.fill(7)(0L)

  override def build(oshi: SystemInfo, metricName: String, logger: Logger): Metric = new Metric {
    val cpuMetric = new CpuMetric(metricName)

    def update(): Unit = {
      import OshiSafeRunner._

      def cpuTicks= oshi.getHardware.getProcessor.getSystemCpuLoadTicks

      val Array(user, nice, sys, idle, wait, irq, softIrq, stolen) = runSafe(cpuTicks, emptyCpuData, metricName, logger)

      val total = user + nice + sys + idle + wait + irq + softIrq + stolen

      cpuMetric.forMode("user").record(100L * user / total)
      cpuMetric.forMode("system").record(100L * sys / total)
      cpuMetric.forMode("wait").record(100L * wait / total)
      cpuMetric.forMode("idle").record(100L * idle / total)
      cpuMetric.forMode("stolen").record(100L * stolen / total)
      cpuMetric.forMode("combined").record(100L * (user + sys + nice + wait) / total)
    }
  }

  class CpuMetric(metricName: String) {
    val cpuMetric = Kamon.histogram(metricName)

    def forMode(mode: String): Histogram =
      cpuMetric.refine(Map("component" -> "system-metrics", "mode" -> mode))
  }
}



