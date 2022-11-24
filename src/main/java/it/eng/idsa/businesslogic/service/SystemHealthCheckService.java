package it.eng.idsa.businesslogic.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemHealthCheckService {
	private static final Logger logger = LoggerFactory.getLogger(SystemHealthCheckService.class);
	private final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
	private final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	private final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
	private final long initialUptime = runtimeMxBean.getUptime();
	private final Map<Long, Long> threadInitialCPU = getThreadCPU();
	private final Map<Long, Float> threadCPUUsage = new HashMap<>();

	@Value("${application.healthcheck.threshold.cpu}")
	private double cpuThreshold;
	@Value("${application.healthcheck.threshold.memory}")
	private double memThreshold;
	@Value("${application.healthcheck.limit.cpu}")
	private double cpuLimit;
	@Value("${application.healthcheck.limit.memory}")
	private double memLimit;


	@Scheduled(fixedDelayString = "${application.healthcheck.cron.fixedDelay}")
//	@Scheduled("$cron.timeDelay")
	public void systemHealthCheck() {
		int MB = 1024 * 1024;
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		double maxMem = memoryMXBean.getHeapMemoryUsage().getMax() / MB;

		double percentageCPUUsage = cpuHealthCheck();
		double percentageMemUsage = memoryHealthCheck();

		if (percentageCPUUsage + cpuThreshold >= cpuLimit) {
			logger.warn("WARNING: CPU Usage is close to limit - CPU Usage {}%", decimalFormat.format(percentageCPUUsage));
		}
		if (percentageMemUsage + memThreshold >= memLimit) {
			logger.warn("WARNING: Memory Usage is close to limit - Memory Usage {}%", decimalFormat.format(percentageMemUsage));
		}

		logger.info("CPU usage: {}% - Memory usage: {}% out of {}MB ", decimalFormat.format(percentageCPUUsage), decimalFormat.format(percentageMemUsage), maxMem);
	}

	public double memoryHealthCheck() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		double usedMem = memoryMXBean.getHeapMemoryUsage().getUsed();
		double maxMem = memoryMXBean.getHeapMemoryUsage().getMax();

		return (usedMem / maxMem) * 100;
	}

	public double cpuHealthCheck() {
		long upTime = runtimeMxBean.getUptime();

		Map<Long, Long> threadCurrentCPU = new HashMap<>();
		ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(false, false);

		for (ThreadInfo info : threadInfos) {
			threadCurrentCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
		}
		// CPU over all processes
		int nrCPUs = osMxBean.getAvailableProcessors();
		// elapsedTime is in ms.
		long elapsedTime = (upTime - initialUptime);
		for (ThreadInfo info : threadInfos) {
			long threadId = info.getThreadId();
			Long initialCPU = threadInitialCPU.get(threadId);
			if (initialCPU != null) {
				// elapsedCpu is in ns
				long elapsedCpu = threadCurrentCPU.get(threadId) - initialCPU;
				float cpuUsage = elapsedCpu / (elapsedTime * 1000000F * nrCPUs);
				threadCPUUsage.put(threadId, cpuUsage);
			}
		}

		Collection<Float> values = threadCPUUsage.values(); // threadCPUUsage contains cpu % per thread
		return values.stream().reduce(0f, Float::sum);
	}

	@NotNull
	private Map<Long, Long> getThreadCPU() {
		Map<Long, Long> threadCPU = new HashMap<>();
		ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(false, false);
		for (ThreadInfo info : threadInfos) {
			threadCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
		}
		return threadCPU;
	}
}
