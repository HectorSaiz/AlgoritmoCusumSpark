package es.uji.fuentesDatos;

import java.util.Date;

import es.uji.filter.NoRetweetFilter;
import es.uji.processor.Persister;
import es.uji.processor.Processor;
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
    private long totales = 0;
    private long totalesAnterior = 0;
    private long inicio = System.currentTimeMillis();
    private long tiempoTotal = 0;
    private long segAnterior = 0;
//    private static final Date startDate = new GregorianCalendar(2014, Calendar.JULY, 11, 14, 0, 0).getTime();


    //    private Processor processor = new FiveMinutesCycle(new NoRetweetFilter());
//    private Processor processor = new Persister(new NoRetweetFilter());
    private Processor processor;
    private String [] args;
    private ZonaIntercambioEventos zonaIntercambio;

    public FuenteDatosTwitter( String [] args, ZonaIntercambioEventos zonaIntercambioEventos ) {
        this.args = args;
        this.zonaIntercambio = zonaIntercambioEventos;
    }

    private void loadProperties() {
        consumerKey = "SEOEfyu7FuWqaaVbeL76oTyEg";
        consumerSecret = "LDIldxPfycxlsw0v8LZpzjtDbnjwhRoyZ9LPx0aEBUuZUC1bNg";
        token = "713244600-QNwvulmTZgmajdYwu0PGfhyekVUr52W0QAsvyT2O";
        tokenSecret = "EasdHyrAFGeTZyXnyx7eUomdoGPyQYOuuE9KLoDQQfl6Q";
    }


//    public static void main(String[] args) {
//        String[] topics = new String[args.length-1];
//        for(int i = 1; i < args.length; i++)
//            topics[i-1] = args[i];
////        while(true) {
////            if(checkTime()) break;
////            try {
////                System.out.print("Comprobando... " + new Date() + "\r");
////                Thread.sleep(1 * 60 * 1000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////        }
//        System.out.println("Empieza la fiesta!!!!" + new Date());
//        FuenteDatosTwitter fuenteDatosTwitter = new FuenteDatosTwitter();
//        fuenteDatosTwitter.createProcessor(args[0]);
//        fuenteDatosTwitter.loadProperties();
//        fuenteDatosTwitter.starListener(topics);
//    }

    private void createProcessor(String table) {
//        processor = new Persister(new NoRetweetFilter(), table);
        processor = new Persister(new NoRetweetFilter(), table);
    }

//    private static boolean checkTime() {
//        if(System.currentTimeMillis() > startDate.getTime())
//            return true;
//        else return false;
//    }

    private void starListener(String[] topics) {
        for(String topic: topics)
            System.out.println(topic);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.setOAuthConsumer(consumerKey,consumerSecret);
        twitterStream.setOAuthAccessToken(new AccessToken(token, tokenSecret));

        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                Tarea t;
                double cantidadEventos;
                if(processor.processTweet(status)) {
                    totales++;
                    if (tiempoTotal == 0 ) {
                        segAnterior = status.getCreatedAt().getTime();
                        totalesAnterior = 0;
                    }
                    tiempoTotal = status.getCreatedAt().getTime();
                    if ( (tiempoTotal - segAnterior) >= 1000 ){
//                        System.out.println("Fecha: " + status.getCreatedAt() + " Nº Tweets: " + (totales-totalesAnterior));
                        cantidadEventos = totales-totalesAnterior;
                        t = new Tarea( cantidadEventos, false);
                        zonaIntercambio.insertaTarea( t );
                        segAnterior = status.getCreatedAt().getTime();
                        totalesAnterior = totales;
                    }
                }
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
        filterQuery.track(topics);
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

//    private String tiempo() {
//        long ahora = System.currentTimeMillis();
//        long segundosTotales = (ahora - inicio)/1000;
//        long dias = segundosTotales / (60*60*24);
//        long horas = segundosTotales / (60*60) - (dias*24);
//        long minutos = segundosTotales / 60 - (dias*24 + horas*60);
//        long segundos = segundosTotales - (dias*24 + horas*60 + minutos*60);
//        return dias + " dias " + horas + "h. " + minutos + "m. " + segundos + "s.";
//    }

    @Override
    public void run() {

        String[] topics = new String[args.length-1];
        for(int i = 1; i < args.length; i++)
            topics[i-1] = args[i];

        System.out.println("Empieza la fiesta!!!!" + new Date());
        createProcessor(args[0]);
        loadProperties();
        starListener(topics);

    }
}
