package com.example.mrd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

@Component
public class CsvDirectoryWatcher {

    private final CsvImporter importer;
    private final String directoryPath;
    private WatchService watchService;

    public CsvDirectoryWatcher(CsvImporter importer, @Value("${app.csv.directory.path:./data}") String directoryPath) {
        this.importer = importer;
        this.directoryPath = directoryPath;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWatcher() {
        Path dir = Paths.get(directoryPath).toAbsolutePath();
        System.out.println("!!!!! Starting CSV directory watcher on: " + dir.toString());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Import any existing CSV files on startup
        importExistingCsvs(dir);

        try {
            watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Thread watcherThread = new Thread(() -> watchLoop(dir), "csv-directory-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void importExistingCsvs(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.csv")) {
            for (Path entry : stream) {
                try {
                    importer.importCsv(entry.toAbsolutePath().toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void watchLoop(Path dir) {
        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                if (filename.toString().toLowerCase().endsWith(".csv")) {
                    Path fullPath = dir.resolve(filename).toAbsolutePath();
                    // Small delay to allow file to be completely written
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    try {
                        importer.importCsv(fullPath.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
