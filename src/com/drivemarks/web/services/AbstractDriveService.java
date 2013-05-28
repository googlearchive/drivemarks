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

package com.drivemarks.web.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;

public abstract class AbstractDriveService {

  protected static final HttpTransport TRANSPORT = new NetHttpTransport();
  protected static final JsonFactory JSON_FACTORY = new JacksonFactory();
  protected Credential credential;
  protected Drive drive;
  
  public AbstractDriveService(Credential credential) {
    this.credential = credential;
    this.drive = new Drive.Builder(TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName("drivemarks").build();
  }

  /**
   * Build and return a Drive service object based on given request parameters.
   * @return Drive service object that is ready to make requests, 
   * or null if there was a problem.
   */
  public Drive getDriveService() {
    return this.drive;
  }
}
