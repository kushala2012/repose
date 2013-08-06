package com.rackspace.papi.filter;

import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class RequestTracer {

    private final boolean trace;
    private long accumulatedTime;
    private long requestStart;
    private static final Logger LOG = LoggerFactory.getLogger(PowerFilterChainBuilderImpl.class);

    public RequestTracer(boolean trace) {
        this.trace = trace;
        requestStart = new Date().getTime();
    }

    public long traceEnter() {
        if (!trace) {
            return 0;
        }

        return new Date().getTime() - requestStart;
    }

    public void traceExit(MutableHttpServletResponse response, String filterName, long myStart) {
        long totalRequestTime = new Date().getTime() - requestStart;

        if(totalRequestTime>30000)  {
            LOG.error("response time took too long: "+totalRequestTime);
        }
        if (!trace) {
            return;
        }

        long myTime = totalRequestTime - myStart - accumulatedTime;
        accumulatedTime += myTime;
        response.addHeader("X-" + filterName + "-Time", myTime + "ms");
    }
}
