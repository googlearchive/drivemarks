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
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.Arrays;

/**
 * A service to bookmark an URL.
 * @author jbd@google.com (Burcu Dogan)
 */
public class BookmarkService extends AbstractDriveService {

  /**
   * Constructs a new {@code BookmarkService} object with
   * given user credentials.
   * @param credential
   */
  public BookmarkService(Credential credential) {
    super(credential);
  }

  /**
   * Inserts a shortcut file into drivemarks folder with the
   * given title and link.
   * @param title
   * @param link
   * @return Inserted {@code File} object.
   * @throws IOException
   */
  public File insert(String title, String link) throws IOException {
    Drive driveService = getDriveService();
    File folder = createOrGetFolder("drivemarks");
    // insert bookmark file
    File file = new File();
    file.setTitle(title);
    file.setDescription(link);
    file.setMimeType(MIMETYPE_DRIVEMARK);
    file.setParents(
        Arrays.asList(new ParentReference().setId(folder.getId())));
    return driveService.files().insert(file).execute();
  }
  
  /**
   * Retrieves or creates the folder with the given
   * name on the root level.
   * @param title
   * @return Retrieved or inserted folder.
   * @throws IOException
   */
  public File createOrGetFolder(String title) throws IOException {
    Drive driveService = getDriveService();
    FileList list =
        driveService.files().list().setQ(QUERY_DRIVEMARKS_FOLDER).execute();
    File drivemarksDir = null;
    if (list == null || list.getItems().size() == 0) {
      // create directory
      File newDir = new File();
      newDir.setTitle(title);
      newDir.setMimeType(MIMETYPE_FOLDER);
      newDir.setParents(Arrays.asList(new ParentReference().setId("root")));
      drivemarksDir = driveService.files().insert(newDir).execute();
    } else {
      drivemarksDir = list.getItems().get(0);
    }
    return drivemarksDir;
  }
  
  public final static String QUERY_DRIVEMARKS_FOLDER =
      "trashed = false and title = 'drivemarks' and mimeType='application/vnd.google-apps.folder'";
  public final static String MIMETYPE_DRIVEMARK =
      "application/vnd.google-apps.drive-sdk";
  public final static String MIMETYPE_FOLDER =
      "application/vnd.google-apps.folder";

}
