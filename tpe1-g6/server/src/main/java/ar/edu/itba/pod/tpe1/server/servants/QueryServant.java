package ar.edu.itba.pod.tpe1.server.servants;

import ar.edu.itba.pod.tpe1.query.QueryServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryServant extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryServant.class);

}
