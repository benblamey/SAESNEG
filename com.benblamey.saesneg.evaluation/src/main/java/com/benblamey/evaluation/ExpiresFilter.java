package com.benblamey.evaluation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ExpiresFilter implements Filter {

    // add a five years expiry
    private static final Integer month = 5;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (month > -1) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.MONTH, month);

            // HTTP header date format: Thu, 01 Dec 1994 16:00:00 GMT
            String o = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz")
                    .format(c.getTime());
            ((HttpServletResponse) response).setHeader("Expires", o);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
