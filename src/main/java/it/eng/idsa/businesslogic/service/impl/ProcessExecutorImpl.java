package it.eng.idsa.businesslogic.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.ProcessExecutor;

@Service
public class ProcessExecutorImpl implements ProcessExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);

	@Override
	public String executeProcess(List<String> cmdList) {
		StringBuilder result = new StringBuilder(80);
		try {
			ProcessBuilder pb = new ProcessBuilder().redirectErrorStream(true);
			pb.command(cmdList);
			logger.info("Executing process...{}", String.join(" ", cmdList));
			Process process = pb.start();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				while (true) {
					String line = in.readLine();
					if (line == null)
						break;
					result.append(line).append(System.lineSeparator());
				}
			}
			logger.info("Process returned response {}", result.toString());
			return result.toString().trim();
		}
		catch (Exception e) {
			logger.error("Error while executing process", e);
		}
		return null;
	}
}
