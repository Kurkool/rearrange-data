package org.example;

import org.example.model.AppConfig;

import java.io.IOException;
import java.nio.file.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final String INPUT_DIR = "Input";
    private static final String OUTPUT_DIR = "Output";
    private static final String TRIGGER_FILE = "Configuration.xml";

    public static void main(String[] args) throws IOException {
        System.out.println("✅ Application started. Creating directories if they don't exist.");
        Files.createDirectories(Paths.get(INPUT_DIR));
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        System.out.println("⏳ Waiting for '" + TRIGGER_FILE + "' in the '" + INPUT_DIR + "' folder...");
        watchForTriggerFile();
    }

    private static void watchForTriggerFile() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path inputPath = Paths.get(INPUT_DIR);
            inputPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey key = watchService.take(); // This is a blocking call
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(TRIGGER_FILE)) {
                        System.out.println("🚀 Trigger file '" + TRIGGER_FILE + "' detected. Starting process...");

                        Path configFile = inputPath.resolve(TRIGGER_FILE);

                        // Short delay to ensure the file is fully written before processing
                        Thread.sleep(500);

                        AppConfig config = ConfigParser.parse(configFile.toFile());
                        if (config != null) {
                            new DataProcessor(config, INPUT_DIR, OUTPUT_DIR).processFiles();
                        }

                        // Remove the configuration file after processing as required
                        try {
                            Files.delete(configFile);
                            System.out.println("✅ Cleaned up trigger file: " + TRIGGER_FILE);
                        } catch (IOException e) {
                            System.err.println("❌ Error removing trigger file: " + e.getMessage());
                        }
                        System.out.println("\n⏳ Waiting for next '" + TRIGGER_FILE + "'...");
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ A critical error occurred in the file watcher: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}