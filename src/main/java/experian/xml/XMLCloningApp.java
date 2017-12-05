package experian.xml;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.List;
import lombok.extern.log4j.Log4j;

@Log4j
public class XMLCloningApp {

  @Parameter(names = {"-x",
      "--xml"}, description = "XML File path", required = true, converter = FileConverter.class)
  private static File xmlFile;
  @Parameter(names = {"-n", "--ncopy"}, description = "n copies of xml")
  private static int xmlCopies = 10;
  @Parameter(names = {"-f", "--field"}, description = "Field name to edit")
  private static List<String> xmlFields;

  @Parameter(names = "--help", description = "see options help", help = true)
  private static boolean help;

  public static void main(String[] args) {
    XMLCloningApp mainBioCatchBack = new XMLCloningApp();
    JCommander jCommander = JCommander.newBuilder()
        .addObject(mainBioCatchBack)
        .build();
    jCommander.parse(args);
    if (help) {
      jCommander.usage();
      return;
    }

    XMLCloner cloner = new XMLCloner(xmlFile, xmlCopies, xmlFields);
    cloner.startCloning();
  }
}
