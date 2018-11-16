package org.jenkinsci.plugins.pivotal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * This poor plugin is now just used to trigger jobs from github
 *
 * @author stevendeal
 */
public class Pivotal {

    protected static String authToken;
    private HashMap projectStoryMap = new HashMap();

    public Pivotal(String authToken) {
        Pivotal.authToken = authToken;
        String url = "http://www.pivotaltracker.com/services/v3/projects";
        List<String> projects = fetchProjects(url);

        for (String projectid : projects) {
            projectStoryMap.putAll(fetchStories(projectid));
        }
    }

    public void postNote(String storyid, String mesg) throws UnsupportedEncodingException, IOException {
        String projectid = getProject(storyid);
        String request = "http://www.pivotaltracker.com/services/v3/projects/" + projectid + "/stories/" + storyid + "/notes";
        
        LOGGER.log(Level.FINE, "Using request url{0}", request);

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(request);
        method.setRequestEntity(new StringRequestEntity(mesg, "text/xml", "ISO-8859-1"));

        method.setRequestHeader("Content-type", "application/xml");
        method.setRequestHeader("X-TrackerToken", authToken);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            LOGGER.log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());
        }
        java.io.InputStream rstream;

        rstream = method.getResponseBodyAsStream();

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(rstream));
        String line;
        while ((line = br.readLine()) != null) {
            LOGGER.log(Level.INFO, line);
        }
        br.close();
    }

    private HashMap fetchStories(String projectid) {
        HashMap projStories = new HashMap();
        HttpClient client = new HttpClient();
        String url = "http://www.pivotaltracker.com/services/v3/projects/" + projectid + "/stories";
        GetMethod get = new GetMethod(url);
        get.setRequestHeader("Content-type", "application/xml");
        get.setRequestHeader("X-TrackerToken", authToken);
        try {
            client.executeMethod(get);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error fetching Pivotal projects/stories. Updating Pivotal will not be possible.\n");
            LOGGER.log(Level.SEVERE, "Exception: {0}", e);
        }

        try {
            java.io.InputStream stream;
            stream = get.getResponseBodyAsStream();
            SAXBuilder sb;
            sb = new SAXBuilder();
            Document d = sb.build(stream);
            Element rootNode = d.getRootElement();
            List list = rootNode.getChildren("story");
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                projStories.put(node.getChildText("id"), projectid);
            }
        } catch (JDOMException jde) {
            LOGGER.log(Level.SEVERE, jde.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return projStories;
    }

    private static List<String> fetchProjects(String url) {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        List<String> projects = new ArrayList<String>();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        get.setRequestHeader("Content-type", "application/xml");
        get.setRequestHeader("X-TrackerToken", authToken);
        try {
            client.executeMethod(get);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error fetching Pivotal projects. Updating Pivotal will not be possible.\n");
            LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
        }

        try {
            java.io.InputStream stream;
            stream = get.getResponseBodyAsStream();
            SAXBuilder sb;
            sb = new SAXBuilder();
            Document d = sb.build(stream);
            Element rootNode = d.getRootElement();
            List list = rootNode.getChildren("project");
            for (int i = 0; i < list.size(); i++) {
                Element node = (Element) list.get(i);
                projects.add(node.getChildText("id"));
            }
        } catch (JDOMException jde) {
            LOGGER.log(Level.SEVERE,jde.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage());
        }
        return projects;
    }

    public String getProject(String storyid) {
        return (String) projectStoryMap.get(storyid);
    }
    private static final Logger LOGGER = Logger.getLogger(Pivotal.class.getName());
}
