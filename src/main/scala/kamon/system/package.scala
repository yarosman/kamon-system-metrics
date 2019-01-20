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

package kamon

import java.lang.management.ManagementFactory

import org.hyperic.sigar.Sigar
import org.slf4j.Logger
import oshi.SystemInfo

package object system {
  private lazy val sigar = new Sigar()
  private lazy val oshi = new SystemInfo()
  private lazy val pid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toLong

  private val filterName = SystemMetrics.FilterName
  private val logger = SystemMetrics.logger

  abstract class MetricBuilder(val metricName: String) extends Builder {

    override def register(): Option[Metric] = {
      if (Kamon.filter(filterName, metricName)) {
        this match {
          case s: SigarMetricBuilder => Some(s.build(sigar, metricName, logger))
          case o: OshiMetricBuilder => Some(o.build(oshi, metricName, logger))
          case jmx: JmxMetricBuilder => Some(jmx.build(metricName, logger))
          case custom: CustomMetricBuilder => Some(custom.build(pid, metricName, logger))
          case _ => None
        }
      }
      else
        None
    }
  }

  sealed trait Builder {
    def register(): Option[Metric] = None
  }

  trait OshiMetricBuilder extends Builder {
    def build(oshi: SystemInfo, metricName: String, logger: Logger): Metric
  }

  trait SigarMetricBuilder extends Builder {
    def build(sigar: Sigar, metricName: String, logger: Logger): Metric
  }

  trait JmxMetricBuilder extends Builder {
    def build(metricPrefix: String, logger: Logger): Metric
  }

  trait CustomMetricBuilder extends Builder {
    def build(pid: Long, metricPrefix: String, logger: Logger): Metric
  }

  trait Metric {
    def update(): Unit
  }

  def withNamedThread[A](name:String)(thunk: => A):A = {
    val oldName = Thread.currentThread().getName
    Thread.currentThread().setName(name)
    try thunk finally {
      Thread.currentThread().setName(oldName)
    }
  }
}
