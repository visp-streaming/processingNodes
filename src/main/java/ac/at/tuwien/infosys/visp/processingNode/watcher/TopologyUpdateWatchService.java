package ac.at.tuwien.infosys.visp.processingNode.watcher;


import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class TopologyUpdateWatchService {

    @Autowired
    Receiver receiver;

    private static final Logger LOG = LoggerFactory.getLogger(TopologyUpdateWatchService.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {

        LOG.info("Initializing file system watcher");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                WatchService watcher;
                try {
                    watcher = FileSystems.getDefault().newWatchService();
                    Path dir = Paths.get("/root/");
                    dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

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
                            LOG.error("invalid watcher key");
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    LOG.error(e.getLocalizedMessage());
                }
            }
        });

    }
}
