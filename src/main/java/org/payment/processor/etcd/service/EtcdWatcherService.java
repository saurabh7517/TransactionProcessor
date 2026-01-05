package org.payment.processor.etcd.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchResponse;
import io.etcd.jetcd.options.WatchOption;
import org.payment.processor.config.SpringConfiguration;
import org.payment.processor.etcd.EtcdConstants;
import org.payment.processor.service.ObserverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class EtcdWatcherService {

    private static final Logger log = LoggerFactory.getLogger(EtcdWatcherService.class);

    private final Client client;
    private final ObserverService observerService;
    private final Executor watcherExecutor = Executors.newSingleThreadExecutor();
    private final Executor eventHandlerExecutor = Executors.newSingleThreadExecutor();
    private final SpringConfiguration springConfiguration;

    public EtcdWatcherService(Client client,
                              ObserverService observerService,
                              SpringConfiguration springConfiguration) {
        this.client = client;
        this.observerService = observerService;
        this.springConfiguration = springConfiguration;
    }

    public EtcdWatcherService(Client client, ObserverService observerService) {
        this.client = client;
        this.observerService = observerService;
        this.springConfiguration = null;


    }

    private void startWatcher() {
        watcherExecutor.execute(() -> {
            try {
                Watch watchClient = client.getWatchClient();
                ByteSequence prefix = ByteSequence.from(EtcdConstants.getLockKey("").getBytes(StandardCharsets.UTF_8));

                WatchOption watchOption = WatchOption.builder().isPrefix(true).build();
                watchClient.watch(prefix, watchOption, this::handleWatchEvent);
            } catch (Exception e) {
                log.error("Etcd watcher could not be started", e);
            }
        });
    }

    private void handleWatchEvent(WatchResponse watchResponse) {

        watchResponse.getEvents().forEach(event -> {
            final String key = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
            final String value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
            log.info("Event type : {} with key : {} and value : {}", event.getEventType(), key, value);

            eventHandlerExecutor.execute(() -> {
                try {
                    switch (event.getEventType()) {
                        case DELETE -> observerService.observe(key);
                        default -> {
                            assert springConfiguration != null;
                            log.warn("Ignore event type: {} with key : {} on instance : {}", event.getEventType(), key, springConfiguration.getAppInstanceId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error handling watch event for key: {}", key, e);
                }
            });
        });
    }
}
