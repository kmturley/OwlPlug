package com.owlplug.core.tasks.plugins.discovery.fileformats;

import com.owlplug.core.model.Plugin;
import com.owlplug.core.model.VST2Plugin;
import java.io.File;
import org.apache.commons.io.FilenameUtils;

public class OsxVst3File extends PluginFile {

  /**
   * Type checking function against current format for the given file.
   * @param file - file to test
   * @return true if the file matches the current file format
   */
  public static boolean formatCheck(File file) {
    return file.getAbsolutePath().endsWith(".vst3") 
        && file.isDirectory();
    
  }
  
  public OsxVst3File(File file) {
    super(file);
  }

  @Override
  public Plugin toPlugin() {
    
    String pluginName = FilenameUtils.removeExtension(this.getPluginFile().getName());
    String pluginPath = this.getPluginFile().getAbsolutePath().replace("\\", "/");
    Plugin plugin = new VST2Plugin(pluginName, pluginPath);
    
    return plugin;
    
  }
}
