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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drivemarks.web.models.State;
import org.drivemarks.web.services.GetService;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.File;

/**
 * Opens a bookmark and redirects user to the bookmark URL.
 * @author jbd@google.com (Burcu Dogan)
 */
@SuppressWarnings("serial")
public class RedirectServlet extends BaseServlet {
  /**
   * Redirects user to the file's URL. Redirects to consent
   * page if there are no users in the session.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    State state = getState(req);
    // each valid open with action contains a state
    // parameter, ignore the request if there are no
    // state parameter is set.
    if (state == null) {
      resp.sendError(400);
      return;
    }
    String fileId = state.firstFileId();
    try {
      File file = new GetService(getCredential(req, resp)).get(fileId);
      resp.sendRedirect(file.getDescription());
    } catch (GoogleJsonResponseException e) {
      loginIfRequired(req, resp, e);
    }
  }
}
