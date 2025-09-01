package de.saschaufer.message_broker.app.file_storage.api.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class WrappedRequest extends HttpServletRequestWrapper {

    private final Map<String, String> headerMap = new HashMap<>();

    public WrappedRequest(final HttpServletRequest request) {
        super(request);
    }

    public void addHeader(final String name, final String value) {
        headerMap.put(name, value);
    }

    public void removeHeader(final String name) {
        headerMap.remove(name);
    }

    public boolean hasHeader(final String name) {
        return Collections.list(super.getHeaderNames()).contains(name) || headerMap.containsKey(name);
    }

    @Override
    public String getHeader(final String name) {
        if (headerMap.containsKey(name)) {
            return headerMap.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        final List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headerMap.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
        final List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }
}
