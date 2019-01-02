package experian.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.log4j.Log4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Log4j
public class XMLCloner {

  private static final String SUBMISSION = "SUBMISSION";
  private static final List<String> DEFAULT_FIELDS = Arrays
      .asList("ADD", "CTY", "FST_NME", "TEL_NO", "STE", "PIN");
  private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
  private final File outputDir = new File("clones");
  private final List<String> xmlFields = new ArrayList<>();
  private final List<String> digitFields;
  private final File xmlFile;
  private int fileId;
  private final Integer nCopies;

  public XMLCloner(File file, int n, final List<String> xmlFields) {
    this.xmlFile = file;
    this.nCopies = n;
    if (xmlFields != null) {
      this.xmlFields.addAll(xmlFields);
    }
    this.xmlFields.addAll(DEFAULT_FIELDS);
    digitFields = Arrays.asList("TEL_NO", "PIN"); // field with numeric values
    deleteDirectory(outputDir);
    outputDir.mkdir();
  }

  public void startCloning() {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(new FileInputStream(this.xmlFile));
      NodeList submissionNodes = document.getElementsByTagName(SUBMISSION);
      for (int i = 0; i < nCopies; i++) {
        updateSubmissionNode(submissionNodes);
        createUpdatedXmlDoc(document);
      }
    } catch (IOException | SAXException | TransformerException | ParserConfigurationException e) {
      log.error("unable to make a copy: " + e.getMessage());
    }
  }

  private void updateSubmissionNode(NodeList submissions) {
    for (int i = 0; i < submissions.getLength(); i++) {
      Node node = submissions.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
        Element element = (Element) node;
        //modify 2 fields in every submission
        for (int j = 0; j < 2; j++) {
          Collections.shuffle(xmlFields);
          String fName = xmlFields.get(0);
          NodeList fieldNode = element.getElementsByTagName(fName);
          if (fieldNode.getLength() > 0) {
            Node feild = fieldNode.item(0);
            String dummyData = createDummyData(digitFields.contains(fName));
            feild.setTextContent(dummyData);
            log.info(fName + ": " + dummyData);
          }
        }
      }
    }
  }

  private String createDummyData(boolean isDigit) {
    Random r = new Random();
    if (isDigit) {
      long num = r.nextInt(999999999) + 8000000000L;
      return String.valueOf(num);
    }
    final String uuid = UUID.randomUUID().toString().replace("-", "");
    return uuid.substring(0, r.nextInt(uuid.length() - 9) + 9);
  }

  private void createUpdatedXmlDoc(final Document xmlDoc) throws TransformerException {
    xmlDoc.getDocumentElement().normalize();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(xmlDoc);
    StreamResult result = new StreamResult(new File(outputDir, (fileId++) + "_xml_copy.xml"));
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, result);
    log.info("XML file updated successfully");
  }

  /* Delete valid/Invalid dir if exist */
  private boolean deleteDirectory(File path) {
    if (path.exists()) {
      File[] files = path.listFiles();
      for (File xmlFile : files) {
        xmlFile.delete();
      }
    }
    return (path.delete());
  }

}
