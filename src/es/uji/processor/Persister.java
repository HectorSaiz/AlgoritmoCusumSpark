package es.uji.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uji.filter.Filter;
import es.uji.filter.NoRetweetFilter;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import twitter4j.Status;

/**
 * Created by oscar on 29/06/14.
 */
public class Persister implements Processor {
    private String dataBase, table;
    private Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Filter filter;

    public Persister(String table) {
        this.table = table;
        filter = new NoRetweetFilter();
        createClient();
    }

    public Persister(Filter filter, String dataBase, String table) {
        this.dataBase = dataBase;
        this.table = table;
        this.filter = filter;
        createClient();
    }

//    private void search() {
//        GetResponse response = client.prepareGet("wordismic", "lovetweets", "1")
//                .setOperationThreaded(false)
//                .execute()
//                .actionGet();
//        System.out.println(response.getId());
//    }

    private void createClient() {
        Node node = NodeBuilder.nodeBuilder().node();
        client = node.client();
    }

//    public void closeClient()  {
//        client.close();
//    }

//    public void processTweet(final String tweet) {
//        try {
//            Status status = TwitterObjectFactory.createStatus(tweet);
//            System.out.println(status.getText());
//            String id = status.getId()+"";
//            System.out.println();
//            IndexResponse indexResponse = client.prepareIndex("twitter", "tweet", id)
//                    .setSource(tweet)
//                    .execute()
//                    .actionGet();
//        } catch (TwitterException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean processTweet(Status status) {
        if(filter.filter(status)) {
            String jsonStatus = null;
            try {
                jsonStatus = objectMapper.writeValueAsString(status);
                String id = status.getId() + "";
                IndexResponse indexResponse = client.prepareIndex(dataBase, table, id)
                        .setSource(jsonStatus)
                        .execute()
                        .actionGet();

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (ActionRequestValidationException e) {
                System.out.println("no campos");
            }
            return true;
        }
        return false;
//        else System.out.println(status);
//            System.out.println("The status did not pass the filter");
    }

    @Override
    public void stop() {
        client.close();
    }
}
