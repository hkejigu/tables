/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.android.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opendatakit.common.android.utilities.ODKFileUtils;

import android.util.Log;

/**
 * This is a general place for utils regarding odktables files. These are files
 * that are associated with various tables, such as html files for different
 * views, etc.
 * @author sudar.sam@gmail.com
 *
 */
public class TableFileUtils {

  private static final String TAG = TableFileUtils.class.getSimpleName();

  /** The default app name for ODK Tables */
  private static final String ODK_TABLES_APP_NAME = "tables";

  /** The name of the output folder, where files are output from the app. */
  public static final String OUTPUT_FOLDER_NAME = "output";

  /** The name of the assets folder. */
  public static final String ASSETS_FOLDER_NAME = "assets";

  /** The name of the folder where the debug objects are written. */
  public static final String DEBUG_FOLDER_NAME = "debug";

  /** Filename for the top-level configuration file */
  private static final String ODK_TABLES_CONFIG_PROPERTIES_FILENAME =
      "config.properties";

  /** Filename for a csv file used for joining files (?) */
  public static final String ODK_TABLES_JOINING_CSV_FILENAME = "temp.csv";

  /** Timeout (in ms) we specify for each http request */
  public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;

  /** The url parameter name of the tableId. */
  public static final String TABLE_ID_PARAM = "tableId";

  /** The response type expected from the server for a json object. */
  public static final String RESP_TYPE_JSON = "application/json; charset=utf-8";

  /** The name of the directory holding the tables javascript. */
  public static final String TABLES_JS_DIR = "js";
  /** The name of the directory holding the graphing files. */
  public static final String TABLES_GRAPH_DIR = "graph";
  /** The name of the base graphing file. */
  private static final String TABLES_GRAPH_BASE_FILE_NAME = "optionspane.html";

  /** The name of the file that is rendered as the custom user home screen. */
  private static final String USER_HOME_SCREEN_FILE_NAME = "index.html";

  public static final String getDefaultAppName() {
    Log.i("TableFileUtils", "appName is null on intent");
    Thread.dumpStack();
    return ODK_TABLES_APP_NAME;
  }

  public static final String extractAppName() {
    Log.e("TableFileUtils", "TODO: figure out how to extract appName here");
    Thread.dumpStack();
    return ODK_TABLES_APP_NAME;
  }
  /**
   * Get all the files under the given folder, excluding those directories that
   * are the concatenation of folder and a member of excluding. If the member
   * of excluding is a directory, none of its children will be synched either.
   * <p>
   * If the folder doesn't exist it returns an empty list.
   * <p>
   * If the file exists but is not a directory, logs an error and returns an
   * empty list.
   * @param folder
   * @param excluding can be null--nothing will be excluded. Should be relative
   * to the given folder.
   * @param relativeTo the path to which the returned paths will be relative.
   * A null value makes them relative to the folder parameter. If it is non
   * null, folder must start with relativeTo, or else the files in
   * folder could not possibly be relative to relativeTo. In this case will
   * throw an IllegalArgumentException.
   * @return the relative paths of the files under the folder--i.e. the paths
   * after the folder parameter, not including the first separator
   * @throws IllegalArgumentException if relativeTo is not a substring of
   * folder.
   */
  public static List<String> getAllFilesUnderFolder(String folder,
      final Set<String> excludingNamedItemsUnderFolder) {
    final File baseFolder = new File(folder);
    String appName = ODKFileUtils.extractAppNameFromPath(baseFolder);

    // Return an empty list of the folder doesn't exist or is not a directory
    if (!baseFolder.exists()) {
      return new ArrayList<String>();
    } else if (!baseFolder.isDirectory()) {
      Log.e(TAG, "[getAllFilesUnderFolder] folder is not a directory: "
          + folder);
      return new ArrayList<String>();
    }

    // construct the set of starting directories and files to process
    File[] partials = baseFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if ( excludingNamedItemsUnderFolder == null ) {
          return true;
        } else {
          return !excludingNamedItemsUnderFolder.contains(pathname.getName());
        }
      }});

    LinkedList<File> unexploredDirs = new LinkedList<File>();
    List<File> nondirFiles = new ArrayList<File>();

    // copy the starting set into a queue of unexploredDirs
    // and a list of files to be sync'd
    for ( int i = 0 ; i < partials.length ; ++i ) {
      if ( partials[i].isDirectory() ) {
        unexploredDirs.add(partials[i]);
      } else {
        nondirFiles.add(partials[i]);
      }
    }

    while (!unexploredDirs.isEmpty()) {
      File exploring = unexploredDirs.removeFirst();
      File[] files = exploring.listFiles();
      for (File f : files) {
        if (f.isDirectory()) {
          // we'll need to explore it
          unexploredDirs.add(f);
        } else {
          // we'll add it to our list of files.
          nondirFiles.add(f);
        }
      }
    }

    List<String> relativePaths = new ArrayList<String>();
    // we want the relative path, so drop the necessary bets.
    for (File f : nondirFiles) {
      // +1 to exclude the separator.
      relativePaths.add(ODKFileUtils.asRelativePath(appName, f));
    }
    return relativePaths;
  }

  /**
   * Get the path of the file relative to the Tables app.
   * @param absolutePath
   * @return
   */
  public static String getRelativePath(String absolutePath) {
    File file = new File(absolutePath);
    String appName = ODKFileUtils.extractAppNameFromPath(file);
    return ODKFileUtils.asRelativePath(appName, file);
  }

  /**
   * Get the output folder for the given app.
   * @param appName
   * @return
   */
  public static String getOutputFolder(String appName) {
    String appFolder = ODKFileUtils.getAppFolder(appName);
    String result = appFolder + File.separator + OUTPUT_FOLDER_NAME;
    return result;
  }

  /**
   * Get the assets folder for the given app.
   * @param appName
   * @return
   */
  public static String getAssetsFolder(String appName) {
    String appFolder = ODKFileUtils.getAppFolder(appName);
    String result = appFolder + File.separator + ASSETS_FOLDER_NAME;
    return result;
  }

  /**
   * Get the path to the assets folder for the Tables app.
   * @return
   */
  public static String getTablesAssetsFolder(String appName) {
    return getAssetsFolder(appName);
  }

  /**
   * Get the path to the user-defined home screen file.
   * @param appName
   * @return
   */
  public static String getUserHomeScreenFile(String appName) {
    // We're going to assume that it is located at assets/index.html.
    String result =
        getAssetsFolder(appName) +
        File.separator +
        USER_HOME_SCREEN_FILE_NAME;
    return result;
  }

  /**
   * Get the path to the configuration file for the given app.
   * @param appName
   * @return
   */
  public static String getConfigurationFile(String appName) {
    String assetsFolder = getAssetsFolder(appName);
    String result =
        assetsFolder +
        File.separator +
        ODK_TABLES_CONFIG_PROPERTIES_FILENAME;
    return result;
  }

  /**
   * Get the path to the configuration file for ODK Tables.
   * @return
   */
  public static String getTablesConfigurationFile(String appName) {
    String result = getConfigurationFile(appName);
    return result;
  }

  /**
   * Convenience method equivalent to calling
   * {@link TableFileUtils#getUserHomeScreenFile(String)}
   * @return
   */
  public static String getTablesHomeScreenFile(String appName) {
    return getUserHomeScreenFile(appName);
  }

  /**
   * Get the path of the base file used for Tables graphing, relative to the
   * app folder.
   * @return
   */
  public static String getRelativePathToGraphFile(String appName) {
    String frameworkPath =
        ODKFileUtils.getFrameworkFolder(appName);
    String result =
        frameworkPath +
        File.separator +
        TABLES_GRAPH_BASE_FILE_NAME;
    return result;
  }

  /**
   * Get the path to the debug folder, where the control and data json for
   * debugging purposes are output. Creates the necessary directory structure.
   * @return
   */
  public static String getTablesDebugObjectFolder(String appName) {
    String outputFolder = getOutputFolder(appName);
    String result = outputFolder + File.separator + DEBUG_FOLDER_NAME;
    File debugOutputFolder = new File(result);
    debugOutputFolder.mkdirs();
    return result;
  }

  /**
   * Returns true if a user-defined home screen file exists at the correct
   * location.
   * @return
   */
  public static boolean homeScreenFileExists(String appName) {
    String path = getUserHomeScreenFile(appName);
    File homeScreenFile = new File(path);
    return homeScreenFile.exists();
  }

  /**
   * Convenience method equivalent to calling
   * {@link #homeScreenFileExists(String)}
   */
  public static boolean tablesHomeScreenFileExists(String appName) {
    return homeScreenFileExists(appName);
  }

  /**
   * Returns true if a configuration file exists at the correct
   * location.
   * @return
   */
  public static boolean configurationFileExists(String appName) {
    String path = getConfigurationFile(appName);
    File configurationFile = new File(path);
    return configurationFile.exists();
  }

  /**
   * Convenience method equivalent to calling
   * {@link #configurationFileExists(String)}
   * @return
   */
  public static boolean tablesConfigurationFileExists(String appName) {
    return configurationFileExists(appName);
  }

}