package org.herac.tuxguitar.gui.editors.chord.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.herac.tuxguitar.gui.actions.Action;
import org.herac.tuxguitar.gui.editors.tab.TGChordImpl;
import org.herac.tuxguitar.song.models.TGChord;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ChordXMLReader {

  public static List<TGChord> getChords(String fileName) {
    List<TGChord> chords = new ArrayList<TGChord>();
    try {
      File file = new File(fileName);
      if (file.exists()) {
        Document doc = getDocument(file);
        loadChords(doc.getFirstChild(), chords);
      }
    } catch (Exception e) {
      LOG.error(e);
    }
    return chords;
  }
  /** The Logger for this class. */
  public static final transient Logger LOG = Logger.getLogger(ChordXMLReader.class);

  private static Document getDocument(File file) {
    Document document = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(file);
    } catch (SAXException sxe) {
      LOG.error(sxe);
    } catch (ParserConfigurationException pce) {
      LOG.error(pce);
    } catch (IOException ioe) {
      LOG.error(ioe);
    }
    return document;
  }

  /**
   * Read shortcuts from xml file
   * 
   * @param shortcutsNode
   * @return
   */
  private static void loadChords(Node chordsNode, List<TGChord> chords) {
    try {
      NodeList chordList = chordsNode.getChildNodes();
      for (int i = 0; i < chordList.getLength(); i++) {
        Node chordItem = chordList.item(i);
        if (chordItem.getNodeName().equals(ChordXML.CHORD_TAG)) {
          NamedNodeMap chordAttributes = chordItem.getAttributes();

          String name = chordAttributes.getNamedItem(
              ChordXML.CHORD_NAME_ATTRIBUTE).getNodeValue();
          String strings = chordAttributes.getNamedItem(
              ChordXML.CHORD_STRINGS_ATTRIBUTE).getNodeValue();
          String firstFret = chordAttributes.getNamedItem(
              ChordXML.CHORD_FIRST_FRET_ATTRIBUTE).getNodeValue();

          TGChord chord = new TGChordImpl(Integer.parseInt(strings));
          chord.setName(name);
          chord.setFirstFret(Integer.parseInt(firstFret));

          NodeList stringList = chordItem.getChildNodes();
          for (int j = 0; j < stringList.getLength(); j++) {
            Node stringItem = stringList.item(j);
            if (stringItem.getNodeName().equals(ChordXML.STRING_TAG)) {
              NamedNodeMap stringAttributes = stringItem.getAttributes();

              String number = stringAttributes.getNamedItem(
                  ChordXML.STRING_NUMBER_ATTRIBUTE).getNodeValue();
              String fret = stringAttributes.getNamedItem(
                  ChordXML.STRING_FRET_ATTRIBUTE).getNodeValue();

              chord.addFretValue(Integer.parseInt(number), Integer
                  .parseInt(fret));
            }
          }
          chords.add(chord);
        }
      }
    } catch (Exception e) {
      chords.clear();
      LOG.error(e);
    }
  }
}
