package com.matecat.converter.server.resources;

import com.matecat.converter.server.JSONResponseFactory;
import com.matecat.converter.server.MatecatConverterServer;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpRetryException;
import java.util.Base64;

import static org.junit.Assert.*;


public class ConvertToXliffResourceTest {

    private static MatecatConverterServer server;
    private static final int PORT = 8090;
    private static final String url = "http://localhost:" + PORT + "/convert";

    @Before
    public void setUp() throws Exception {
        server = new MatecatConverterServer(PORT);
        while ( !server.isStarted() )
            Thread.sleep(100);
    }

    @Test
    public void testConvertSuccess() throws Exception {

        File fileToUpload = new File(getClass().getResource("/server/test.docx").getPath());

        // Send request
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url + "/en-US/fr-FR");
        FileBody uploadFilePart = new FileBody(fileToUpload);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", uploadFilePart);
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);

        // Check OK status code
        assertEquals(200, response.getStatusLine().getStatusCode());

        // Check body
        String body = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();
        JSONObject json = (JSONObject) new JSONParser().parse(body);

        // Is success
        boolean isSuccess = (boolean) json.get(JSONResponseFactory.IS_SUCCESS);
        assertTrue(isSuccess);

        // No error message
        String error = (String) json.getOrDefault(JSONResponseFactory.ERROR_MESSAGE, "");
        assertEquals("", error);

        // Encoded document
        String encodedDoc = (String) json.get(JSONResponseFactory.DOCUMENT_CONTENT);
        assertNotSame("", encodedDoc);

        File out = new File(fileToUpload.getPath() + ".xlf");
        FileUtils.writeByteArrayToFile(out, Base64.getDecoder().decode(encodedDoc));

    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        while (!server.isStopped())
            Thread.sleep(100);
    }
}