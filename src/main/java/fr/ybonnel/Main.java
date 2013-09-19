package fr.ybonnel;

import fr.ybonnel.crawler.LanyrdCrawler;
import fr.ybonnel.modele.Schedule;
import fr.ybonnel.simpleweb4j.exception.HttpErrorException;
import fr.ybonnel.simpleweb4j.handlers.Response;
import fr.ybonnel.simpleweb4j.handlers.Route;
import fr.ybonnel.simpleweb4j.handlers.RouteParameters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static fr.ybonnel.simpleweb4j.SimpleWeb4j.*;

/**
 * Main class.
 */
public class Main {


    /**
     * Start the server.
     * @param port http port to listen.
     * @param waitStop true to wait the stop.
     */
    public static void startServer(int port, boolean waitStop) throws IOException {

        // Set the http port.
        setPort(port);
        // Set the path to static resources.
        setPublicResourcesPath("/fr/ybonnel/public");

        get(new Route<Void, List<Schedule>>("/schedules", Void.class) {
            @Override
            public Response<List<Schedule>> handle(Void param, RouteParameters routeParams) throws HttpErrorException {
                try {
                    return new Response<>(LanyrdCrawler.getInstance().crawl());
                }
                catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
                    throw new HttpErrorException(500, e);
                }
            }
        });

        // Start the server.
        start(waitStop);
    }

    /**
     * @return port to use
     */
    private static int getPort() {
        // Heroku
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }

        // Cloudbees
        String cloudbeesPort = System.getProperty("app.port");
        if (cloudbeesPort != null) {
            return Integer.parseInt(cloudbeesPort);
        }

        // Default port;
        return 9999;
    }

    public static void main(String[] args) throws IOException {
        // For main, we want to wait the stop.
        startServer(getPort(), true);
    }
}
