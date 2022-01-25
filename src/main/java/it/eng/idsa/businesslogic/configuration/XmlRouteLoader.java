package it.eng.idsa.businesslogic.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Component that loads Camel routes from XML files located in a directory specified in
 * application.properties at application start.
 */
@Component
public class XmlRouteLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(XmlRouteLoader.class);
	
    public XmlRouteLoader(Unmarshaller unmarshaller, CamelRouteLoader camelRouteLoader,
			ResourcePatternResolver patternResolver, @Value("${camel.xml-routes.directory:#null}") String directory) {
		super();
		this.unmarshaller = unmarshaller;
		this.camelRouteLoader = camelRouteLoader;
		this.patternResolver = patternResolver;
		this.directory = directory;
	}

	/**
     * Unmarshaller for reading route definitions from XML.
     */
    private final Unmarshaller unmarshaller;

    /**
     * Loader for adding routes to the camel context.
     */
    private final CamelRouteLoader camelRouteLoader;

    /**
     * Resolver for finding classpath resources with paths matching a given pattern.
     */
    private final ResourcePatternResolver patternResolver;

    /**
     * Directory where the XML routes are located.
     */
    @Value("${camel.xml-routes.directory:#null}")
    private String directory;

    @PostConstruct
    public void loadRoutes() {
        try {
            Objects.requireNonNull(directory);
            if (logger.isDebugEnabled()) {
            	logger.debug("Loading Camel routes from: {}", directory);
            }
            loadRoutes(directory);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
            	logger.error("Failed to load Camel routes. [exception=({})].", e.getMessage());
            }
            throw new IllegalStateException("Failed to load Camel routes.", e);
        }
    }

    private void loadRoutes(final String directoryPath) throws Exception {
        if (directoryPath.startsWith("classpath")) {
            loadRoutesFromClasspath(directoryPath);
        } else {
            loadRoutes(openFile(directoryPath));
        }
    }
    
    private File openFile(final String path) throws FileNotFoundException {
        final var base = new File(path);
        if (!base.exists()) {
            throw new FileNotFoundException("File '" + path + "' does not exist.");
        }

        return base;
    }


    private void loadRoutesFromClasspath(final String directoryPath) throws Exception {
        assert directoryPath.startsWith("classpath");
        loadRoutes(patternResolver.getResources(getPatternForPath(directoryPath)));
    }

    private void loadRoutes(final Resource[] files) throws Exception {
        for (var file: files) {
            try (var inputStream = file.getInputStream()) {
                loadRoutesFromInputStream(inputStream);
            }
        }
    }

    private void loadRoutes(final File file) throws Exception {
        Objects.requireNonNull(file);

        if (file.isDirectory()) {
            for (var subFile : getContainedFiles(file)) {
                loadRoutes(subFile);
            }
        } else {
            try (var inputStream = new FileInputStream(file)) {
                loadRoutesFromInputStream(inputStream);
            }
        }
    }
    
    private File[] getContainedFiles(final File directory) {
        assert directory.isDirectory();
        final var out = directory.listFiles();
        return out == null ? new File[]{} : out;
    }

    private void loadRoutesFromInputStream(final InputStream inputStream) throws Exception {
        try {
            camelRouteLoader.addRouteToContext(toRoutesDef(inputStream).getRoutes());
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
            	logger.error("Failed to read route files. [exception=({})]", e.getMessage());
            }
            throw e;
        } catch (JAXBException e) {
            if (logger.isErrorEnabled()) {
            	logger.error("Failed to parse route files. [exception=({})]", e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
            	logger.error("Failed to add routes to context. [exception=({})]", e.getMessage());
            }
            throw e;
        }
    }

    private RoutesDefinition toRoutesDef(final InputStream stream) throws JAXBException {
        return (RoutesDefinition) unmarshaller.unmarshal(stream);
    }

    /**
     * Adds routes to the current camel context.
     */
    @Component
    private static class CamelRouteLoader {
    	
		/**
         * The Camel context.
         */
    	@Autowired
        private CamelContext context;

        public void addRouteToContext(final List<RouteDefinition> routes) throws Exception {
            for (final var route : routes) {
                addRouteToContext(route);
            }
        }

        public void addRouteToContext(final RouteDefinition route) throws Exception {
                addToContext(route);
        }

        private void addToContext(final RouteDefinition route) throws Exception {
            context.adapt(ModelCamelContext.class).addRouteDefinition(route);

            if (logger.isDebugEnabled()) {
            	logger.debug("Loaded route from XML file: {}", route.getRouteId());
            }
        }
    }

    private String getPatternForPath(final String path) {
        return path.endsWith("/") ? path.concat("**/*.xml") : path.concat("/**/*.xml");
    }
}
