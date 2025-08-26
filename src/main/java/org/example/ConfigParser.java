package org.example;

import org.example.model.AppConfig;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Parses the Configuration.xml file.
 */
class ConfigParser {
    public static AppConfig parse(File configFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            doc.getDocumentElement().normalize();

            int threads = Integer.parseInt(doc.getElementsByTagName("Thread").item(0).getTextContent().trim());
            int mode = Integer.parseInt(doc.getElementsByTagName("Mode").item(0).getTextContent().trim());

            System.out.println("⚙️ Configuration loaded: Threads=" + threads + ", Mode=" + mode);
            return new AppConfig(threads, mode);
        } catch (Exception e) {
            System.err.println("❌ Failed to parse Configuration.xml: " + e.getMessage());
            return null;
        }
    }
}
