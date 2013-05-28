/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.drivemarks.web;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.File;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.drivemarks.web.services.BookmarkService;

/**
 * Bookmarks a link on user's Drive.
 * @author jbd@google.com (Burcu Dogan)
 */
@SuppressWarnings("serial")
public class BookmarkServlet extends BaseServlet {
  
  /**
   * Inserts a new bookmark with the given title and link.
   * Redirects user to the consent page if an auth error
   * occurs during the requests.
   */
  // TODO: move to doPost
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      BookmarkService service = new BookmarkService(getCredential(req, resp));
      File file =
          service.insert(req.getParameter("title"), req.getParameter("link"));
      // handle error cases
      render(resp, "bookmarked.ftl", file);
    } catch (GoogleJsonResponseException e) {
      loginIfRequired(req, resp, e);
    }
  }
}
