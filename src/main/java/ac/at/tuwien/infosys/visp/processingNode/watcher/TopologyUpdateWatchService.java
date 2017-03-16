package ac.at.tuwien.infosys.visp.processingNode.watcher;


import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class TopologyUpdateWatchService {

    @Autowired
    private Receiver receiver;

    @Autowired
    private ErrorHandler errorHandler;

    private static final Logger LOG = LoggerFactory.getLogger(TopologyUpdateWatchService.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        LOG.info("Initializing file system watcher");
        executorService.execute(() -> {
            WatchService watcher;
            try {
                watcher = FileSystems.getDefault().newWatchService();
                Path dir = Paths.get("/root/");
                dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                File checkFirst = new File("/root/topologyUpdate");
                if(checkFirst.exists()) {
                    receiver.handleTopologyUpdate(checkFirst.toPath());
                }

                while (true) {
                    WatchKey key;
                    key = watcher.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        LOG.info(kind.name() + ": " + fileName);
                        if (kind == OVERFLOW) {
                            continue;
                        } else if (kind == ENTRY_MODIFY) {
                            receiver.handleTopologyUpdate(new File("/root/topologyUpdate").toPath());
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        errorHandler.send("invalid watcher key");
                        break;
                    }
                }
            } catch (IOException | InterruptedException e) {
                errorHandler.send(e.getLocalizedMessage());
            }
        });
    }
}
