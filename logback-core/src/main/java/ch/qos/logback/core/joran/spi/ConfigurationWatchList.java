/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;


import ch.qos.logback.core.spi.ContextAwareBase;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


import java.util.*;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;


/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

  URL mainURL;
   //adding WatchService watcher,and Map
  private  WatchService watcher = initialWacher();
  private  Map<String,Boolean> keys;
  
  List<File> fileWatchList = new ArrayList<File>();
  //List<Long> lastModifiedList = new ArrayList<Long>();
  
  
 ///add code: initialWacher
  private  WatchService initialWacher()
  {
    try{
        this.keys = new HashMap<String,Boolean>();
        return FileSystems.getDefault().newWatchService();
    }
    catch(Exception e)
    {
        System.out.println("initial watcher error in ConfigurationWatchList.java");
        return null;
    }
  }
  
   ///add code  register dir path to watcher,put key to map table
  private void register(Path dir,String file_absolute_path) throws IOException {
    WatchKey key = dir.register(watcher,ENTRY_MODIFY);
    keys.put(file_absolute_path,true);
   
  }
  /// add code: set dir to Wacher
  private void setDirToWatcher(Path dir,String file_absolute_path)
  {
    try
    {
        register(dir,file_absolute_path);
    }
    catch(Exception e)
    {
        System.out.println("register watcher key  error in ConfigurationWatchList.java");
    }
    
  }
  
  
  
  
  public void clear() {
    this.mainURL = null;
    //lastModifiedList.clear();
     ///add code : close watcher
    try{
      watcher.close();
      watcher = FileSystems.getDefault().newWatchService();
    }
    catch(Exception e)
    {
      System.out.println("watcher  close error in ConfigurationWatchList.java");
    }
    ///
    
    
    
    fileWatchList.clear();
  }

  /**
   * The mainURL for the configuration file. Null values are allowed.
   * @param mainURL
   */
  public void setMainURL(URL mainURL) {
    // main url can be null
    this.mainURL = mainURL;
    if (mainURL != null)
      addAsFileToWatch(mainURL);
  }

  private void addAsFileToWatch(URL url) {
    File file = convertToFile(url);
    if (file != null) {
      fileWatchList.add(file);
      //lastModifiedList.add(file.lastModified());
       ////add code: adding dir to watcher
      String[] file_absolutePath = file.getAbsolutePath().split("\\\\");
      String s = "";
	  for(int i = 0; i < file_absolutePath.length-1;i++)
			s +=file_absolutePath[i]+"/";
      Path dir = Paths.get(s);
      setDirToWatcher(dir,file_absolutePath[file_absolutePath.length-1]);
      
      
      
    }
  }

  public void addToWatchList(URL url) {
    addAsFileToWatch(url);
  }

  public URL getMainURL() {
    return mainURL;
  }

  public List<File> getCopyOfFileWatchList() {
    return new ArrayList<File>(fileWatchList);
  }

  public boolean changeDetected() {
    /////adding  code
    WatchKey key = null;
    while(true) {
       //key = watcher.poll();
      try {
        key = watcher.take();
      }
      catch(Exception e)
      {
        System.out.println("Wacher.take() error");
      }
      if (key == null)
      {
        return false;
      }
      for (WatchEvent<?> event : key.pollEvents()) {
        final Path changed = (Path) event.context();
        if (keys.get(changed.toString())) {
          return true;
        }
      }
    }
    ////// end adding code  
      
      
      
      
    /*
    int len = fileWatchList.size();
    for (int i = 0; i < len; i++) {
      long lastModified = lastModifiedList.get(i);
      File file = fileWatchList.get(i);
      if (lastModified != file.lastModified()) {
        return true;
      }
    }
    return false;
    */
    //return (lastModified != fileToScan.lastModified() && lastModified != SENTINEL);
  }

  @SuppressWarnings("deprecation")
  File convertToFile(URL url) {
    String protocol = url.getProtocol();
    if ("file".equals(protocol)) {
      return new File(URLDecoder.decode(url.getFile()));
    } else {
      addInfo("URL [" + url + "] is not of type file");
      return null;
    }
  }

}
