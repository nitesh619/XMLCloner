package experian.xml;

import com.beust.jcommander.IStringConverter;
import java.io.File;

public class FileConverter implements IStringConverter<File> {
  @Override
  public File convert(String filePath) {
    return new File(filePath);
  }
}
