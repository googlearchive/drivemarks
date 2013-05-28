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

package org.drivemarks.web.services;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.model.File;

/**
 * A service to retrieve metadata about a file.
 * @author jbd@google.com (Burcu Dogan)
 */
public class GetService extends AbstractDriveService {

  /**
   * Constructs a new {@code GetService} instance.
   * @param credential User credentials.
   */
  public GetService(Credential credential) {
    super(credential);
  }

  /**
   * Gets metadata of the given file.
   * @param fileId The ID of the file to get metadata for.
   * @return A {@code File} object.
   * @throws IOException
   */
  public File get(String fileId) throws IOException {
    return getDriveService().files().get(fileId).execute();
  }

}
