package de.saschaufer.message_broker.app.agent_http.management;

import de.saschaufer.message_broker.app.agent_http.config.ApplicationProperties;
import lombok.Getter;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Component
public class UserAgent {
    private final String appName;
    private final String appVersion;
    private final String port;
    private final String hostName;
    private final String hostIp;
    private final String userAgent;

    public UserAgent(final BuildProperties buildProperties, final ApplicationProperties applicationProperties) throws UnknownHostException {
        appName = buildProperties.getArtifact();
        appVersion = buildProperties.getVersion();
        port = String.valueOf(applicationProperties.server().port());
        hostName = InetAddress.getLocalHost().getCanonicalHostName();
        hostIp = InetAddress.getLocalHost().getHostAddress();

        userAgent = String.format("%s/%s(%s/%s:%s)", getAppName(), getAppVersion(), getHostName(), getHostIp(), getPort());
    }
}
