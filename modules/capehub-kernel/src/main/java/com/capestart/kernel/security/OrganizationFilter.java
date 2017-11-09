/**
 *  Copyright 2017 The Regents is CapeStart at Nagercoil
 *
 */
package com.capestart.kernel.security;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capestart.security.api.Organization;
import com.capestart.security.api.OrganizationDirectoryService;
import com.capestart.security.api.SecurityService;
import com.capestart.util.NotFoundException;

/**
 * Inspect's request URL's and sets the organization for the request
 * @author CS39
 *
 */
public class OrganizationFilter implements Filter {

	/** The logger */
	private static final Logger logger = LoggerFactory.getLogger(OrganizationFilter.class);
	
	/** The security service */
	protected SecurityService securityService = null;
	
	/** The organization directory to use when resolving organizations. This may be null. */
	protected OrganizationDirectoryService organizationDirectory = null;

	/**
	 * Sets a reference to the organization directory service
	 * @param organizationDirectory
	 * 		the organization directory
	 */
	public void setOrganizationDirectoryService(OrganizationDirectoryService organizationDirectory) {
		this.organizationDirectory = organizationDirectory;
	}

	/**
	 * Set's the reference to the security service
	 * @param securityService
	 * 		the security service
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		URL url = new URL(httpRequest.getRequestURL().toString());
		Organization org = null;
		try {
			try {
				org = organizationDirectory.getOrganization(url);
			} catch(NotFoundException e) {
				logger.trace("No organization mapped to {}", url);
		        List<Organization> orgs = organizationDirectory.getOrganizations();
		        if (orgs.size() == 1) {
		          org = orgs.get(0);
		          logger.trace("Defaulting organization to {}", org);
		        } else {
		          logger.warn("No organization is mapped to handle {}", url);
		        }
			}
			// If an organization was found, move on. Otherwise return a 404
		    if (org != null) {
		       securityService.setOrganization(org);
		       chain.doFilter(request, response);
		    } else {
		       httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "No organization is mapped to handle " + url);
		    }
		} finally {
			securityService.setOrganization(null);
			securityService.setUser(null);
		}
	}

	@Override
	public void destroy() {
		
	}
}
