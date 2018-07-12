package pl.legol.logrest;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class LogController {

    private CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    private ConsumerFactory consumerFactory;

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getLogs() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(Duration.ofHours(1).toMillis());
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onError((e) -> emitters.remove(sseEmitter));

        Consumer consumer = consumerFactory.createConsumer();
        consumer.assign(Collections.singleton(new TopicPartition("logs", 0)));
        consumer.seek(new TopicPartition("logs", 0), 0);
        ConsumerRecords polled = consumer.poll(100);
        polled.records(new TopicPartition("logs", 0)).forEach(cr -> {
            try {
                sseEmitter.send(((ConsumerRecord) cr).value().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        emitters.add(sseEmitter);
        return sseEmitter;
    }

    public void onLogAdded(String log) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(log);
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
    }
}
