package it.eng.idsa.businesslogic.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import it.eng.idsa.businesslogic.service.ProcessExecutor;

@Configuration
public class CertificationCheck {

	private static final Logger logger = LoggerFactory.getLogger(CertificationCheck.class);

	@Autowired
	BuildProperties buildProperties;

	@Autowired
	private ProcessExecutor processExecutor;
	@Value("${application.targetDirectory}")
	Path targetDirectory;

	@PostConstruct
	public void checkIfVerionsIsCertified() {

		if (StringUtils.containsIgnoreCase(buildProperties.getVersion(), "SNAPSHOT")) {
			logger.info("Skipping version certification check, since development version is being used.");
		} else {
			String rootImageName = "rdlabengpa/ids_execution_core_container:v";
			List<String> startCmdList = getStartCmdList();
			List<String> cmdList = new ArrayList<String>(startCmdList);
			cmdList.add("echo | cosign verify --insecure-ignore-tlog --key " + targetDirectory.resolve("trueconn.pub") + " " + rootImageName
					+ buildProperties.getVersion());

			String getCosignVerification = processExecutor.executeProcess(cmdList);

			boolean containsError = StringUtils.containsIgnoreCase(getCosignVerification, "error");

			if (containsError) {
				logger.warn("WARNING: You're using uncertified version of ECC!");
			} else {
				logger.info("Using certified version: " + buildProperties.getVersion());
			}
		}
	}

	private List<String> getStartCmdList() {
		List<String> startCmdList = new ArrayList<String>();
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.info("Running on windows");
			startCmdList.add("cmd.exe");
			startCmdList.add("/c");
		} else {
			startCmdList.add("/bin/sh");
			startCmdList.add("-c");
		}
		return startCmdList;
	}
}
