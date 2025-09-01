package de.saschaufer.message_broker.app.file_storage.api.filter;

import de.saschaufer.message_broker.common.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class MDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain) throws ServletException, IOException {

        final WrappedRequest wrappedRequest = new WrappedRequest(request);

        if (!wrappedRequest.hasHeader(Constants.Http.Header.CORRELATION_ID)) {
            wrappedRequest.addHeader(Constants.Http.Header.CORRELATION_ID, UUID.randomUUID().toString());
        }

        final String correlationId = wrappedRequest.getHeader(Constants.Http.Header.CORRELATION_ID);

        MDC.put(Constants.Logging.CORRELATION_ID, correlationId);

        response.addHeader(Constants.Http.Header.CORRELATION_ID, correlationId);

        filterChain.doFilter(wrappedRequest, response);
    }
}
