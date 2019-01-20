package kamon.system

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import oshi.SystemInfo
import oshi.hardware._
import oshi.hardware.CentralProcessor.TickType
import oshi.software.os._
import oshi.software.os.OperatingSystem.ProcessSort
import oshi.util.FormatUtil
import oshi.util.Util
import java.util

object SystemInfoTest {

  val LOG = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = { // Options: ERROR > WARN > INFO > DEBUG > TRACE

    LOG.info("Initializing System...")
    val si = new SystemInfo
    val hal = si.getHardware
    val os = si.getOperatingSystem
    System.out.println(os)
//    LOG.info("Checking computer system...")
//    printComputerSystem(hal.getComputerSystem)
//    LOG.info("Checking Processor...")
//    printProcessor(hal.getProcessor)
//    LOG.info("Checking Memory...")
//    printMemory(hal.getMemory)
    LOG.info("Checking CPU...")
    printCpu(hal.getProcessor)
//    LOG.info("Checking Processes...")
//    printProcesses(os, hal.getMemory)
//    LOG.info("Checking Sensors...")
//    printSensors(hal.getSensors)
//    LOG.info("Checking Power sources...")
//    printPowerSources(hal.getPowerSources)
//    LOG.info("Checking Disks...")
//    printDisks(hal.getDiskStores)
//    LOG.info("Checking File System...")
//    printFileSystem(os.getFileSystem)
//    LOG.info("Checking Network interfaces...")
//    printNetworkInterfaces(hal.getNetworkIFs)
//    LOG.info("Checking Network parameterss...")
//    printNetworkParameters(os.getNetworkParams)
//    // hardware: displays
//    LOG.info("Checking Displays...")
//    printDisplays(hal.getDisplays)
//    // hardware: USB devices
//    LOG.info("Checking USB Devices...")
//    printUsbDevices(hal.getUsbDevices(true))
//    LOG.info("Checking Sound Cards...")
//    printSoundCards(hal.getSoundCards)
  }

  private def printComputerSystem(computerSystem: ComputerSystem): Unit = {
    System.out.println("manufacturer: " + computerSystem.getManufacturer)
    System.out.println("model: " + computerSystem.getModel)
    System.out.println("serialnumber: " + computerSystem.getSerialNumber)
    val firmware = computerSystem.getFirmware
    System.out.println("firmware:")
    System.out.println("  manufacturer: " + firmware.getManufacturer)
    System.out.println("  name: " + firmware.getName)
    System.out.println("  description: " + firmware.getDescription)
    System.out.println("  version: " + firmware.getVersion)
    System.out.println("  release date: " + (if (firmware.getReleaseDate == null) "unknown"
    else if (firmware.getReleaseDate == null) "unknown"
    else firmware.getReleaseDate))
    val baseboard = computerSystem.getBaseboard
    System.out.println("baseboard:")
    System.out.println("  manufacturer: " + baseboard.getManufacturer)
    System.out.println("  model: " + baseboard.getModel)
    System.out.println("  version: " + baseboard.getVersion)
    System.out.println("  serialnumber: " + baseboard.getSerialNumber)
  }

  private def printProcessor(processor: CentralProcessor): Unit = {
    System.out.println(processor)
    System.out.println(" " + processor.getPhysicalPackageCount + " physical CPU package(s)")
    System.out.println(" " + processor.getPhysicalProcessorCount + " physical CPU core(s)")
    System.out.println(" " + processor.getLogicalProcessorCount + " logical CPU(s)")
    System.out.println("Identifier: " + processor.getIdentifier)
    System.out.println("ProcessorID: " + processor.getProcessorID)
  }

  private def printMemory(memory: GlobalMemory): Unit = {
    System.out.println("Memory: " + FormatUtil.formatBytes(memory.getAvailable) + "/" + FormatUtil.formatBytes(memory.getTotal))

    System.out.println("Swap used: " + FormatUtil.formatBytes(memory.getSwapUsed) + "/" + FormatUtil.formatBytes(memory.getSwapTotal))
  }

  private def printCpu(processor: CentralProcessor): Unit = {
    System.out.println("Uptime: " + FormatUtil.formatElapsedSecs(processor.getSystemUptime))
    System.out.println("Context Switches/Interrupts: " + processor.getContextSwitches + " / " + processor.getInterrupts)
    val prevTicks = processor.getSystemCpuLoadTicks
    processor.getProcessorCpuLoadTicks
    System.out.println("CPU, IOWait, and IRQ ticks @ 0 sec:" + prevTicks)
    // Wait a second...
    Util.sleep(1000)
    val ticks = processor.getSystemCpuLoadTicks
    System.out.println("CPU, IOWait, and IRQ ticks @ 1 sec:" + util.Arrays.toString(ticks))

    val user = ticks(TickType.USER.getIndex) - prevTicks(TickType.USER.getIndex)
    val nice = ticks(TickType.NICE.getIndex) - prevTicks(TickType.NICE.getIndex)
    val sys = ticks(TickType.SYSTEM.getIndex) - prevTicks(TickType.SYSTEM.getIndex)
    val idle = ticks(TickType.IDLE.getIndex) - prevTicks(TickType.IDLE.getIndex)
    val iowait = ticks(TickType.IOWAIT.getIndex) - prevTicks(TickType.IOWAIT.getIndex)
    val irq = ticks(TickType.IRQ.getIndex) - prevTicks(TickType.IRQ.getIndex)
    val softirq = ticks(TickType.SOFTIRQ.getIndex) - prevTicks(TickType.SOFTIRQ.getIndex)
    val steal = ticks(TickType.STEAL.getIndex) - prevTicks(TickType.STEAL.getIndex)
    val totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal

    println("User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%%n"
      .format(100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu, 100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu))

    println("CPU load: %.1f%% (counting ticks)%n".format(processor.getSystemCpuLoadBetweenTicks * 100))
    println("CPU load: %.1f%% (OS MXBean)%n".format(processor.getSystemCpuLoad * 100))

    val loadAverage = processor.getSystemLoadAverage(3)
    System.out.println("CPU load averages:" + (if (loadAverage(0) < 0) " N/A"
    else " %.2f".format(loadAverage(0))) + (if (loadAverage(1) < 0) " N/A"
    else " %.2f".format(loadAverage(1))) + (if (loadAverage(2) < 0) " N/A"
    else " %.2f".format(loadAverage(2))))
    // per core CPU
    val procCpu = new StringBuilder("CPU load per processor:")
    val load = processor.getProcessorCpuLoadBetweenTicks
    for (avg <- load) {
      procCpu.append(" %.1f%%".format( avg * 100))
    }
    System.out.println(procCpu.toString)
  }

  private def printProcesses(os: OperatingSystem, memory: GlobalMemory): Unit = {
    System.out.println("Processes: " + os.getProcessCount + ", Threads: " + os.getThreadCount)
    // Sort by highest CPU
    val procs = os.getProcesses(5, ProcessSort.CPU)
    System.out.println("   PID  %CPU %MEM       VSZ       RSS Name")
  }

  private def printSensors(sensors: Sensors): Unit = {
    System.out.println("Sensors:")
    println(" CPU Temperature: %.1f°C%n".format(sensors.getCpuTemperature))
    System.out.println(" Fan Speeds: " + util.Arrays.toString(sensors.getFanSpeeds))
    println(" CPU Voltage: %.1fV%n".format(sensors.getCpuVoltage))
  }

  private def printPowerSources(powerSources: Array[PowerSource]): Unit = {
    val sb = new StringBuilder("Power: ")
    if (powerSources.length == 0) sb.append("Unknown")
    else {
      val timeRemaining = powerSources(0).getTimeRemaining
      if (timeRemaining < -1d) sb.append("Charging")
      else if (timeRemaining < 0d) sb.append("Calculating time remaining")
      else sb.append("%d:%02d remaining".format((timeRemaining / 3600).toInt, (timeRemaining / 60).toInt % 60))
    }
    for (pSource <- powerSources) {
      sb.append("%n %s @ %.1f%%".format(pSource.getName, pSource.getRemainingCapacity * 100d))
    }
    System.out.println(sb.toString)
  }

  private def printDisks(diskStores: Array[HWDiskStore]): Unit = {
    System.out.println("Disks:")
    for (disk <- diskStores) {
      val readwrite = disk.getReads > 0 || disk.getWrites > 0
      println(" %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms%n".format(disk.getName, disk.getModel, disk.getSerial, if (disk.getSize > 0) FormatUtil.formatBytesDecimal(disk.getSize)
      else "?", if (readwrite) disk.getReads
      else "?", if (readwrite) FormatUtil.formatBytes(disk.getReadBytes)
      else "?", if (readwrite) disk.getWrites
      else "?", if (readwrite) FormatUtil.formatBytes(disk.getWriteBytes)
      else "?", if (readwrite) disk.getTransferTime
      else "?"))
      val partitions = disk.getPartitions
//      if (partitions == null) { // TODO Remove when all OS's implemented
//        continue //todo: continue is not supported
//      }
      for (part <- partitions) {
        println(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s%n"
          .format(part.getIdentification, part.getName, part.getType, part.getMajor, part.getMinor, FormatUtil.formatBytesDecimal(part.getSize), if (part.getMountPoint.isEmpty) "" else " @ " + part.getMountPoint))
      }
    }
  }

  private def printFileSystem(fileSystem: FileSystem): Unit = {
    System.out.println("File System:")
    println(" File Descriptors: %d/%d%n".format(fileSystem.getOpenFileDescriptors, fileSystem.getMaxFileDescriptors))
    val fsArray = fileSystem.getFileStores
    for (fs <- fsArray) {
      val usable = fs.getUsableSpace
      val total = fs.getTotalSpace
      println((" %s (%s) [%s] %s of %s free (%.1f%%), %s of %s files free (%.1f%%) is %s " + (if (fs.getLogicalVolume != null && fs.getLogicalVolume.length > 0) "[%s]"
      else "%s") + " and is mounted at %s%n").format(fs.getName, if (fs.getDescription.isEmpty) "file system" else fs.getDescription, fs.getType, FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace), 100d * usable / total, fs.getFreeInodes, fs.getTotalInodes, 100d * fs.getFreeInodes / fs.getTotalInodes, fs.getVolume, fs.getLogicalVolume, fs.getMount))
    }
  }

  private def printNetworkInterfaces(networkIFs: Array[NetworkIF]): Unit = {
    System.out.println("Network interfaces:")
    for (net <- networkIFs) {
      println(" Name: %s (%s)%n".format(net.getName, net.getDisplayName))
      println("   MAC Address: %s %n".format(net.getMacaddr))
      println("   MTU: %s, Speed: %s %n".format(net.getMTU, FormatUtil.formatValue(net.getSpeed, "bps")))
      println("   IPv4: %s %n".format( net.getIPv4addr))
      println("   IPv6: %s %n".format( net.getIPv6addr))
      val hasData = net.getBytesRecv > 0 || net.getBytesSent > 0 || net.getPacketsRecv > 0 || net.getPacketsSent > 0
      println("   Traffic: received %s/%s%s; transmitted %s/%s%s %n".format( if (hasData) net.getPacketsRecv + " packets"
      else "?", if (hasData) FormatUtil.formatBytes(net.getBytesRecv)
      else "?", if (hasData) " (" + net.getInErrors + " err)"
      else "", if (hasData) net.getPacketsSent + " packets"
      else "?", if (hasData) FormatUtil.formatBytes(net.getBytesSent)
      else "?", if (hasData) " (" + net.getOutErrors + " err)"
      else ""))
    }
  }

  private def printNetworkParameters(networkParams: NetworkParams): Unit = {
    System.out.println("Network parameters:")
    println(" Host name: ", networkParams.getHostName)
    println(" Domain name: ", networkParams.getDomainName)
    println(" DNS servers: ", networkParams.getDnsServers.mkString(","))
    println(" IPv4 Gateway: ", networkParams.getIpv4DefaultGateway)
    println(" IPv6 Gateway: ", networkParams.getIpv6DefaultGateway)
  }

  private def printDisplays(displays: Array[Display]): Unit = {
    System.out.println("Displays:")
    var i = 0
    for (display <- displays) {
      System.out.println(" Display " + i + ":")
      System.out.println(display.toString)
      i += 1
    }
  }

  private def printUsbDevices(usbDevices: Array[UsbDevice]): Unit = {
    System.out.println("USB Devices:")
    for (usbDevice <- usbDevices) {
      System.out.println(usbDevice.toString)
    }
  }

  private def printSoundCards(cards: Array[SoundCard]): Unit = {
    System.out.println("Sound Cards:")
    for (card <- cards) {
      System.out.println(card.toString)
    }
  }

}
