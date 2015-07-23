package es.uji.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.uji.filter.Filter;
import es.uji.filter.NoRetweetFilter;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import twitter4j.Status;

/**
 * Created by oscar on 29/06/14.
 */
public class Persister implements Processor {
    //    private final String tweet = "{\"created_at\":\"Sun Jun 29 16:18:17 +0000 2014\",\"id\":483283322282340352,\"id_str\":\"483283322282340352\",\"text\":\"RT @OnlyU0115: 140629 \\uc778\\ucc9c\\uacf5\\ud56d \\uc785\\uad6d #\\uc625\\ucf00\\uc774 \\ubbfc\\uc900\\uc544 \\ub098 \\ub178\\ub7fd \\ub4e3\\uace0\\uc774\\uca84~!!!&gt;_&lt;   \\uc73c\\uc73d ..!!!! \\uc624\\ub298 \\ub0b4 \\uc2ec\\uc7a5\\uc5d0 \\ubb34\\ub9ac\\uac00....!!!!!!! http:\\/\\/t.co\\/KtQseN38bU\",\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\\" rel=\\\"nofollow\\\"\\u003eTwitter Web Client\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":199758235,\"id_str\":\"199758235\",\"name\":\"Jie \\uacb0\\uacb0 \\u2764\",\"screen_name\":\"ShingJie\",\"location\":\"Taiwan Kaohsiung\",\"url\":\"https:\\/\\/www.youtube.com\\/user\\/ShingJie0717\",\"description\":\"TAIWAN HOTTEST \\u2665 Dont Stop Love Cant Stop Support 2PM \\u2665\",\"protected\":false,\"verified\":false,\"followers_count\":83,\"friends_count\":473,\"listed_count\":2,\"favourites_count\":291,\"statuses_count\":7162,\"created_at\":\"Thu Oct 07 17:23:49 +0000 2010\",\"utc_offset\":28800,\"time_zone\":\"Taipei\",\"geo_enabled\":true,\"lang\":\"zh-tw\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"010208\",\"profile_background_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_background_images\\/563217645\\/xaa085e5988d61cb879829de4e9d4ce8.png\",\"profile_background_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_background_images\\/563217645\\/xaa085e5988d61cb879829de4e9d4ce8.png\",\"profile_background_tile\":true,\"profile_link_color\":\"A614DB\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"980B83\",\"profile_text_color\":\"4D0B7F\",\"profile_use_background_image\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/449167062623997953\\/hVuEoSE8_normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/449167062623997953\\/hVuEoSE8_normal.png\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/199758235\\/1398246507\",\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Sat Jun 28 21:28:59 +0000 2014\",\"id\":482999123277119488,\"id_str\":\"482999123277119488\",\"text\":\"140629 \\uc778\\ucc9c\\uacf5\\ud56d \\uc785\\uad6d #\\uc625\\ucf00\\uc774 \\ubbfc\\uc900\\uc544 \\ub098 \\ub178\\ub7fd \\ub4e3\\uace0\\uc774\\uca84~!!!&gt;_&lt;   \\uc73c\\uc73d ..!!!! \\uc624\\ub298 \\ub0b4 \\uc2ec\\uc7a5\\uc5d0 \\ubb34\\ub9ac\\uac00....!!!!!!! http:\\/\\/t.co\\/KtQseN38bU\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.echofon.com\\/\\\" rel=\\\"nofollow\\\"\\u003eEchofon\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":2280090912,\"id_str\":\"2280090912\",\"name\":\"OnlyU0115.com\",\"screen_name\":\"OnlyU0115\",\"location\":\"JUN. K 1st SOLO  L\\u2764\\ufe0fVE & H\\u274cTE\",\"url\":\"http:\\/\\/onlyu0115.com\",\"description\":\"OnlyU OnlyMe OnlyJUN. K~\\u2665\",\"protected\":false,\"verified\":false,\"followers_count\":2534,\"friends_count\":1,\"listed_count\":35,\"favourites_count\":4,\"statuses_count\":659,\"created_at\":\"Tue Jan 07 05:35:57 +0000 2014\",\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"lang\":\"ko\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/442333873624465408\\/9d-qGdhw_normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/442333873624465408\\/9d-qGdhw_normal.png\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/2280090912\\/1389174787\",\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":359,\"favorite_count\":144,\"entities\":{\"hashtags\":[{\"text\":\"\\uc625\\ucf00\\uc774\",\"indices\":[15,19]}],\"trends\":[],\"urls\":[],\"user_mentions\":[],\"symbols\":[],\"media\":[{\"id\":482999122752843776,\"id_str\":\"482999122752843776\",\"indices\":[83,105],\"media_url\":\"http:\\/\\/pbs.twimg.com\\/media\\/BrP1JKKCMAA8Tap.jpg\",\"media_url_https\":\"https:\\/\\/pbs.twimg.com\\/media\\/BrP1JKKCMAA8Tap.jpg\",\"url\":\"http:\\/\\/t.co\\/KtQseN38bU\",\"display_url\":\"pic.twitter.com\\/KtQseN38bU\",\"expanded_url\":\"http:\\/\\/twitter.com\\/OnlyU0115\\/status\\/482999123277119488\\/photo\\/1\",\"type\":\"photo\",\"sizes\":{\"medium\":{\"w\":600,\"h\":600,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"large\":{\"w\":1024,\"h\":1024,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":340,\"resize\":\"fit\"}}}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"low\",\"lang\":\"ko\"},\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[{\"text\":\"\\uc625\\ucf00\\uc774\",\"indices\":[30,34]}],\"trends\":[],\"urls\":[],\"user_mentions\":[{\"screen_name\":\"OnlyU0115\",\"name\":\"OnlyU0115.com\",\"id\":2280090912,\"id_str\":\"2280090912\",\"indices\":[3,13]}],\"symbols\":[],\"media\":[{\"id\":482999122752843776,\"id_str\":\"482999122752843776\",\"indices\":[98,120],\"media_url\":\"http:\\/\\/pbs.twimg.com\\/media\\/BrP1JKKCMAA8Tap.jpg\",\"media_url_https\":\"https:\\/\\/pbs.twimg.com\\/media\\/BrP1JKKCMAA8Tap.jpg\",\"url\":\"http:\\/\\/t.co\\/KtQseN38bU\",\"display_url\":\"pic.twitter.com\\/KtQseN38bU\",\"expanded_url\":\"http:\\/\\/twitter.com\\/OnlyU0115\\/status\\/482999123277119488\\/photo\\/1\",\"type\":\"photo\",\"sizes\":{\"medium\":{\"w\":600,\"h\":600,\"resize\":\"fit\"},\"thumb\":{\"w\":150,\"h\":150,\"resize\":\"crop\"},\"large\":{\"w\":1024,\"h\":1024,\"resize\":\"fit\"},\"small\":{\"w\":340,\"h\":340,\"resize\":\"fit\"}},\"source_status_id\":482999123277119488,\"source_status_id_str\":\"482999123277119488\"}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"ko\"}\n";
    private String table;
    private Client client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Filter filter;

    public Persister(String table) {
        this.table = table;
        filter = new NoRetweetFilter();
        createClient();
    }

    public Persister(Filter filter, String table) {
        this.table = table;
        this.filter = filter;
        createClient();
    }


    private void createClient() {
        Node node = NodeBuilder.nodeBuilder().node();
        client = node.client();
    }


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
//                System.out.println(status.getUser().getLocation());
                String id = status.getId() + "";
                IndexResponse indexResponse = client.prepareIndex("love", table, id)
//                IndexResponse indexResponse = client.prepareIndex("incendio", "espanya", id)
                        .setSource(jsonStatus)
                        .execute()
                        .actionGet();
//                System.out.println(status.getText());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
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
