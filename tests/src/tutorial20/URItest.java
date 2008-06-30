package tutorial20;

import java.net.URISyntaxException;

import org.gridlab.gat.URI;

public class URItest {

    /**
     * @param args
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException {
        java.net.URI javaURI = new java.net.URI(args[0]);
        System.out.println("Java path: " + javaURI.getPath());
        System.out.println("resolved : " + javaURI.normalize());

//        URI uri = new URI(args[0]);
//        System.out.println("URI 1: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setHost(args[1]);
//        System.out.println("URI 4: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setScheme(args[1]);
//        System.out.println("URI 2: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setUserInfo(args[1]);
//        System.out.println("URI 3: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setPort(5);
//        System.out.println("URI 5: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setPort(-1);
//        System.out.println("URI 6: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setPath(args[1]);
//        System.out.println("URI 7: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setQuery(args[1]);
//        System.out.println("URI 8: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());
//        uri = uri.setFragment(args[1]);
//        System.out.println("URI 9: " + uri);
//        System.out.println(" host: " + uri.getHost());
//        System.out.println(" path: " + uri.getPath());

    }

}
