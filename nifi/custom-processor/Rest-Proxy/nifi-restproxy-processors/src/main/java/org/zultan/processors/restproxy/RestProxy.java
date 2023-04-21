/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zultan.processors.restproxy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.stream.io.StreamUtils;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Tags({"rest proxy 1.0"})
@CapabilityDescription("A proxy that call a non-standard Rest API. Example, an GET endpoint that need a request body.")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class RestProxy extends AbstractProcessor {

    public static final PropertyDescriptor Rest_Api_Url = new PropertyDescriptor
            .Builder().name("Rest_Api_Url")
            .displayName("Rest Url")
            .description("The Rest API that will be invoke.")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .addValidator(StandardValidators.URL_VALIDATOR)
            //.addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor Key_Store_File = new PropertyDescriptor
            .Builder().name("Key_Store_File")
            .displayName("Key Store File")
            .description("The location of the key store file.")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor Key_Store_Pws = new PropertyDescriptor
            .Builder().name("Key_Store_Pws")
            .displayName("Key Store Password")
            .description("The password for the key store.")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_Success = new Relationship.Builder()
            .name("success")
            .description("Example relationship")
            .build();

    public static final Relationship REL_Failure = new Relationship.Builder()
            .name("failure")
            .description("Example relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(Rest_Api_Url);
        descriptors.add(Key_Store_File);
        descriptors.add(Key_Store_Pws);
        descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(REL_Success);
        relationships.add(REL_Failure);
        relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile requestFlowFile = session.get();
        if ( requestFlowFile == null ) {
            return;
        }

        // custom nifi processor implementation by zultan 2023-04-21
        String rest_api_url =  context.getProperty("Rest_Api_Url").evaluateAttributeExpressions(requestFlowFile).getValue().trim();
        String key_store_file =  context.getProperty("Key_Store_File").evaluateAttributeExpressions(requestFlowFile).getValue().trim();
        String key_store_pws =  context.getProperty("Key_Store_Pws").evaluateAttributeExpressions(requestFlowFile).getValue().trim();

        //String imo = "8029583";
        //JSONObject pl = new JSONObject();

        try {
            final byte[] byteBuffer = new byte[1024];
            session.read(requestFlowFile, new InputStreamCallback() {
                @Override
                public void process(InputStream in) throws IOException {
                    StreamUtils.fillBuffer(in, byteBuffer, false);
                }
            });

            final long len = Math.min(byteBuffer.length, requestFlowFile.getSize());
            String contentString = new String(byteBuffer, 0, (int) len);
            JSONObject req_pl = new JSONObject(contentString);

//            pl.put("vesselId", "");
//            pl.put("vesselName", "");
//            pl.put("officialNumber", "");
//            pl.put("imoNumber", imo);

            KeyStore keyStore = KeyStore.getInstance("JKS");

            //key file : /Users/zultan/downloads/mmdis-key
            //password : mmdis-key
            FileInputStream instream = new FileInputStream(new File(key_store_file));
            keyStore.load(instream, key_store_pws.toCharArray());

            // Create the SSL context
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                    .build();

            // Create the socket factory
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

            // Create the http client
            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            // Make the request
            StringEntity entity = new StringEntity(req_pl.toString());

            //https://mmdis.marine.gov.my/MMDIS_DIS_STG/spk/getVessel/
            HttpGetWithEntity e = new HttpGetWithEntity(rest_api_url);
            e.setEntity(entity);

            HttpResponse response = httpClient.execute(e);
            HttpEntity responseEntity = response.getEntity();

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            //System.out.println("Status code: " + statusCode);

            String result = EntityUtils.toString(responseEntity);
            //System.out.println(result);


            //write result to flowfile content
            final AtomicReference<String> flowfileContent= new AtomicReference<>();
            flowfileContent.set(result);

            requestFlowFile= session.write(requestFlowFile, new OutputStreamCallback() {

                @Override
                public void process(OutputStream out) throws IOException {
                    out.write(flowfileContent.get().getBytes());
                }
            });

            session.putAttribute(requestFlowFile, "rest-proxy-statuscode", String.valueOf(statusCode));
            session.putAttribute(requestFlowFile, "rest-proxy-result", result);

            session.transfer(requestFlowFile, REL_Success);
        }
        catch (Exception ex){
            session.putAttribute(requestFlowFile, "rest-err", ex.getMessage());
            session.putAttribute(requestFlowFile, "rest-proxy-statuscode", "500");
            session.transfer(requestFlowFile, REL_Failure);
            return;
        }
    }
}
