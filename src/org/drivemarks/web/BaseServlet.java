/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.drivemarks.web;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drivemarks.web.models.State;

/**
 * Abstract servlet that sets up credentials and provides some convenience
 * methods.
 * @author jbd@google.com (Burcu Dogan)
 */
@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet {

  protected static final HttpTransport TRANSPORT = new NetHttpTransport();
  protected static final JsonFactory JSON_FACTORY = new JacksonFactory();
  protected static GoogleClientSecrets googleClientSecrets;
  protected CredentialManager credentialManager;
  protected Configuration fileMarkerCfg;

  /**
   * Initializes the servlet.
   */
  @Override
  public void init() throws ServletException {
    super.init();
    // init credential manager
    credentialManager = new CredentialManager(
        getClientSecrets(), TRANSPORT, JSON_FACTORY);
    fileMarkerCfg = new Configuration();//create a FreeMarker configuration object
    fileMarkerCfg.setServletContextForTemplateLoading(getServletContext(),
        "WEB-INF/templates"); 
    fileMarkerCfg.setDefaultEncoding("utf-8");
  }

  /**
   * Redirects to OAuth2 consent page if user is not logged in.
   * @param req   Request object.
   * @param resp  Response object.
   * @throws IOException 
   */
  protected void loginIfRequired(HttpServletRequest req,
      HttpServletResponse resp, GoogleJsonResponseException e) throws IOException {
    if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
      StringBuffer requestURL = req.getRequestURL();
      if (req.getQueryString() != null) {
          requestURL.append("?").append(req.getQueryString());
      }
      req.getSession().setAttribute(KEY_SESSION_REDIRECT, requestURL.toString());
      resp.sendRedirect("/login");
    }
  }

  /**
   * If OAuth2 redirect callback is invoked and there is a code query param,
   * retrieve user credentials and profile. Then, redirect to the home page.
   * @param req   Request object.
   * @param resp  Response object.
   * @throws IOException
   */
  protected boolean handleCallbackIfRequired(HttpServletRequest req,
      HttpServletResponse resp) throws IOException {
    String code = req.getParameter("code");
    if (code != null) {
      // retrieve new credentials with code
      Credential credential = credentialManager.retrieve(code);
      // request userinfo
      Oauth2 service = getOauth2Service(credential);
      Userinfo about = service.userinfo().get().execute();
      String id = about.getId();
      credentialManager.save(id, credential);
      req.getSession().setAttribute(KEY_SESSION_USERID, id);

      String redirect = (String) req.getSession().getAttribute(KEY_SESSION_REDIRECT);
      req.getSession().setAttribute(KEY_SESSION_REDIRECT, null);
      if (redirect != null) {
        resp.sendRedirect(redirect);
      } else {
        resp.sendRedirect("?");
      }
      return true;
    }
    return false;
  }

  /**
   * Returns the credentials of the user in the session. If user is not in the
   * session, returns null.
   * @param req   Request object.
   * @param resp  Response object.
   * @return      Credential object of the user in session or null.
   */
  protected Credential getCredential(HttpServletRequest req,
      HttpServletResponse resp) {
    String userId = (String) req.getSession().getAttribute(KEY_SESSION_USERID);
    if (userId != null) {
      return credentialManager.get(userId);
    }
    return null;
  }

  /**
   * Deletes the credentials of the user in the session permanently and removes
   * the user from the session.
   * @param req   Request object.
   * @param resp  Response object.
   */
  protected void deleteCredential(HttpServletRequest req,
      HttpServletResponse resp) {
    String userId = (String) req.getSession().getAttribute(KEY_SESSION_USERID);
    if (userId != null) {
      credentialManager.delete(userId);
      req.getSession().removeAttribute(KEY_SESSION_USERID);
    }
  }

  /**
   * Build and return an Oauth2 service object based on given request parameters.
   * @param credential User credentials.
   * @return Drive service object that is ready to make requests, or null if
   *         there was a problem.
   */
  protected Oauth2 getOauth2Service(Credential credential) {
    return new Oauth2.Builder(TRANSPORT, JSON_FACTORY, credential).build();
  }

  /**
   * Returns true if one of the required query parameters is missing.
   * @param req
   * @param resp
   * @param paramNames
   * @return
   * @throws IOException
   */
  protected boolean requireQueryParams(HttpServletRequest req,
      HttpServletResponse resp, String... paramNames) throws IOException {
    for (String paramName: paramNames) {
      if (req.getParameter(paramName) == null) {
        resp.sendError(400, "Missing parameter: " + paramName);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Renders a template.
   * @param resp
   * @param templateName
   * @param root
   * @throws IOException
   */
  protected void render(
      HttpServletResponse resp, String templateName, Map<String, Object> root)
      throws IOException {
    Template t = fileMarkerCfg.getTemplate(templateName);
    resp.setContentType("text/html; charset=utf-8");
    Writer out = resp.getWriter();
    try {
      t.process(root, out);
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets the state from request parameters.
   * @param req
   * @return The {@code State} object serialized from the
   * query parameter `state`.
   */
  protected State getState(HttpServletRequest req) {
    if (req.getParameter("state") != null) {
      return new State(req.getParameter("state"));
    }
    return null;
  }

  /**
   * Reads client_secrets.json and creates a GoogleClientSecrets object.
   * @return A GoogleClientsSecrets object.
   */
  private GoogleClientSecrets getClientSecrets() {
    if (googleClientSecrets == null) {
      InputStream stream =
          getServletContext().getResourceAsStream(CLIENT_SECRETS_FILE_PATH);
      try {
        googleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, stream);
      } catch (IOException e) {
        throw new RuntimeException("No client_secrets.json found");
      }
    }
     return googleClientSecrets;
  }
  
  public static final String KEY_SESSION_USERID = "user_id";
  public static final String KEY_SESSION_REDIRECT = "redirect";
  public static final String DEFAULT_MIMETYPE = "text/plain";
  public static final String CLIENT_SECRETS_FILE_PATH
      = "/WEB-INF/client_secrets.json";
}
