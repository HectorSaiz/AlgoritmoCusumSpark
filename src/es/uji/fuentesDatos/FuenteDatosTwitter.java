package es.uji.fuentesDatos;

import es.uji.filter.NoRetweetFilter;
import es.uji.processor.ProcessorTweet;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Date;

/**
 * Creado by oscar on 27/06/14.
 */
public class FuenteDatosTwitter implements Runnable{
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String tokenSecret;
    private long totales;
    private boolean bufferInicializado;

    private ProcessorTweet processor;
    private String topico;
    private ZonaIntercambioEventos zonaIntercambio;

    public FuenteDatosTwitter( String topico, ZonaIntercambioEventos zonaIntercambioEventos ) {
        this.topico = topico;
        this.zonaIntercambio = zonaIntercambioEventos;
        this.totales = 0;
        this.bufferInicializado = false;
    }

    private void loadProperties() {
        consumerKey = "SEOEfyu7FuWqaaVbeL76oTyEg";
        consumerSecret = "LDIldxPfycxlsw0v8LZpzjtDbnjwhRoyZ9LPx0aEBUuZUC1bNg";
        token = "713244600-QNwvulmTZgmajdYwu0PGfhyekVUr52W0QAsvyT2O";
        tokenSecret = "EasdHyrAFGeTZyXnyx7eUomdoGPyQYOuuE9KLoDQQfl6Q";
    }


    private void createProcessor() {
        processor = new ProcessorTweet(zonaIntercambio, new NoRetweetFilter());
    }


    public long tweetsTotales () {
        return totales;
    }

    private void starListener(String topico) {
        System.out.println(topico);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.setOAuthConsumer(consumerKey, consumerSecret);
        twitterStream.setOAuthAccessToken(new AccessToken(token, tokenSecret));

        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {

                if (!bufferInicializado){
                    bufferInicializado = processor.inicializaBuffer(status);
                } else {
                    processor.processTweet(status);
                }
                totales++;
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
//                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
//                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        FilterQuery filterQuery = new FilterQuery();
//        filterQuery.track(new String[] {"#votaPP", "#TrabajarHacerCrecer", "#PP"});
        filterQuery.track(new String[]{topico});
//        filterQuery.track(new String[]{"football", "world cup", "#worldcup", "mundial"});
//        filterQuery.language(new String[] {"en"});
//        filterQuery.track(new String[]{"fuego", "llamas", "humo", "incendio"});
//        filterQuery.track(new String[]{"accidente de tráfico"});
//        filterQuery.language(new String[]{"es"});
        twitterStream.filter(filterQuery);
        try {
            Thread.sleep(2000);
//            for(int i = 0; i < 1440; i++) {
            while(true) {

                // TODO NUNCA acaba este bucle, cuando implementemos la interfaz gráfica que haya un escuchador
                // (todo) a un botón que pare el flujo de datos de twitter

//                System.out.print("Minuto " + i + " de 1440.\r");
//                System.out.print("Tweets: " + totales + " en " + new Date() + "\r");
//                System.out.print(totales + " tweets en " + tiempo() + "\r");
                Thread.sleep(1 * 1000);
            }
//            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Desconexión");
        twitterStream.cleanUp();
        twitterStream.shutdown();
        processor.stop();
    }


    @Override
    public void run() {
        System.out.println("Empieza la fiesta!!!!" + new Date());
        createProcessor();
        loadProperties();
        starListener(topico);

    }
}
