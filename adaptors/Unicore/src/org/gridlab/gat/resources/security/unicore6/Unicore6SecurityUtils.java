package org.gridlab.gat.resources.security.unicore6;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.security.AssertionSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supply methods for unicore6 related security issues.
 * 
 * @author Andreas Bender
 */
public class Unicore6SecurityUtils {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Unicore6SecurityUtils.class);

	/**
	 * Saves an assertion in user.home/.hila2/saml-assertions/. The name of the assertion file is a unique name of the
	 * assertion issuer and stored in the {@link AssertionSecurityContext}.
	 * 
	 * @param context
	 *            the {@link GATContext}
	 * @throws GATInvocationException
	 *             is thrown when no {@link AssertionSecurityContext} is found in the {@link GATContext}
	 */
	public static void saveAssertion(GATContext context) throws GATInvocationException {

		List<SecurityContext> securityContexts = context.getSecurityContexts();

		AssertionSecurityContext assertionContext = null;

		for (SecurityContext securityContext : securityContexts) {
			if (securityContext instanceof AssertionSecurityContext) {
				assertionContext = (AssertionSecurityContext) securityContext;
			}
		}

		if (null == assertionContext) {
			throw new GATInvocationException("No assertion security context found in GATContext.");
		}

		LOGGER.debug("User name: " + assertionContext.getUsername());
		LOGGER.debug("Assertion: " + assertionContext.getAssertion());

		String filePath = System.getProperty("user.home") + File.separator + ".hila2" + File.separator
				+ "saml-assertions" + File.separator + assertionContext.getUsername();

		LOGGER.debug("Assertion path: " + filePath);

		FileWriter writer = null;
		try {
			writer = new FileWriter(filePath);
			writer.append(assertionContext.getAssertion());
			writer.flush();

		} catch (IOException e) {
			throw new GATInvocationException("Can not write assertion to disk.", e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				throw new GATInvocationException("Can not close FileWriter.", e);
			}
		}
	}
	// TODO implement validation of assertion (is null, check username,assertion validity,...)
}
