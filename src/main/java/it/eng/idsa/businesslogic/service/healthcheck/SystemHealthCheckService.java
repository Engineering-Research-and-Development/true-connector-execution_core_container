package it.eng.idsa.businesslogic.service.healthcheck;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthCheckService {
	private static final Logger logger = LoggerFactory.getLogger(SystemHealthCheckService.class);
	private final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
	private final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
	private final OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();
	private final long initialUptime = runtimeMxBean.getUptime();
	private final Map<Long, Long> threadInitialCPU = getThreadCPU();
	private final Map<Long, Float> threadCPUUsage = new HashMap<>();

	@Autowired
	private HealthCheckConfiguration healthCheckConfiguration;
	
	@Scheduled(fixedDelayString = "${application.healthcheck.resourcemanager.cron.fixedDelay}")
	public void systemHealthCheck() {
		int MB = 1024 * 1024;
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		double maxMem = memoryMXBean.getHeapMemoryUsage().getMax() / MB;

		double percentageCPUUsage = cpuHealthCheck();
		double percentageMemUsage = memoryHealthCheck();

		double cpuThreshold = healthCheckConfiguration.getThreshold().getCpu();
		if (percentageCPUUsage >= cpuThreshold) {
			logger.warn("WARNING: CPU Usage is close to limit - CPU Usage {}%", decimalFormat.format(percentageCPUUsage));
		}
		double memThreshold = healthCheckConfiguration.getThreshold().getMemory();
		if (percentageMemUsage >= memThreshold) {
			logger.warn("WARNING: Memory Usage is close to limit - Memory Usage {}%", decimalFormat.format(percentageMemUsage));
		}

		logger.info("CPU usage: {}% - Memory usage: {}% out of {}MB ", 
				decimalFormat.format(percentageCPUUsage), decimalFormat.format(percentageMemUsage), maxMem);
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
