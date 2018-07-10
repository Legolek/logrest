package pl.legol.logrest;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class LogController {

    private CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getLogs() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(Duration.ofHours(1).toMillis());
        emitters.add(sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onError((e) -> emitters.remove(sseEmitter));

        ReversedLinesFileReader revFileReader = new ReversedLinesFileReader(new File("logs/Logrest.log"));
        int counter = 0;
        int lines = 50;
        List<String> reversedLines = new ArrayList<>();

        while (counter < lines) {
            reversedLines.add(0, revFileReader.readLine());
            counter++;
        }

        reversedLines.forEach(line -> {
            try {
                sseEmitter.send(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
