/**
 * Copyright 2013 Twitter, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package es.uji.cusumSpark;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.net.SocketPermission;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FilterStreamExample {

    public static void run() throws InterruptedException {
        String consumerKey = "SEOEfyu7FuWqaaVbeL76oTyEg";
        String consumerSecret = "LDIldxPfycxlsw0v8LZpzjtDbnjwhRoyZ9LPx0aEBUuZUC1bNg";
        String token = "713244600-QNwvulmTZgmajdYwu0PGfhyekVUr52W0QAsvyT2O";
        String secret = "EasdHyrAFGeTZyXnyx7eUomdoGPyQYOuuE9KLoDQQfl6Q";
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        // add some track terms
        endpoint.trackTerms(Lists.newArrayList("twitterapi", "#love"));

        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        // Authentication auth = new BasicAuth(username, password);

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Establish a connection
        client.connect();

        // Do whatever needs to be done with messages
        int count = 0;
        String lastDate = "";
        for (int msgRead = 0; msgRead < 1000; msgRead++) {
            String msg = queue.take();
            //System.out.println(msg);
            if ( msg.substring(2,7).equals("limit") ){
                String extra = msg.substring(18, (msg.length()-4));
                count += Integer.parseInt(extra);
            } else if ( !msg.substring(109,111).equals("RT") ){
                String date = msg.substring(15,45);
                if (lastDate.equals(date)){
                    count++;
                } else {
                    if (!lastDate.equals("")){
                        System.out.println(count);
                        int ini = Integer.parseInt(msg.substring(32,34));
                        int fin = Integer.parseInt(lastDate.substring(17,19));
                        if (msg.charAt(30) != lastDate.charAt(15)){
                            fin += 60;
                        }
                        for (int i = 1; i<(ini-fin); i++){
                            System.out.println(0);
                        }
                    }
                    count = 1;
                }
                lastDate = date;
            }
        }

        client.stop();

    }

    public static void main(String[] args) {
        try {
            FilterStreamExample.run();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
}
