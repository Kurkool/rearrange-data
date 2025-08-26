package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles the core logic of reading, transforming, and writing data.
 */
public class DataProcessor {
    private final AppConfig config;
    private final String inputDir;
    private final String outputDir;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DataProcessor(AppConfig config, String inputDir, String outputDir) {
        this.config = config;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Main processing method triggered by the file watcher.
     * It handles both Mode 1 (parallel) and Mode 2 (aggregate).
     */
    public void processFiles() {
        List<File> jsonFiles = findInputFiles();
        if (jsonFiles.isEmpty()) {
            System.out.println("🟡 No .json files found in the Input directory to process.");
            return;
        }

        if (config.mode() == 2) {
            processAggregateMode(jsonFiles); // Bonus requirement
        } else {
            processParallelMode(jsonFiles); // Bonus requirement
        }
    }

    /**
     * Mode 1: Process each file individually in parallel.
     */
    private void processParallelMode(List<File> jsonFiles) {
        System.out.println("📂 Mode 1: Processing " + jsonFiles.size() + " files in parallel with " + config.threads() + " threads.");
        ExecutorService executor = Executors.newFixedThreadPool(config.threads());

        for (File inputFile : jsonFiles) {
            executor.submit(() -> {
                try {
                    InputFile inputData = objectMapper.readValue(inputFile, InputFile.class);
                    OutputFile outputData = transformData(inputData.getCards());

                    Path outputPath = Paths.get(outputDir, inputFile.getName());
                    objectMapper.writeValue(outputPath.toFile(), outputData);
                    System.out.println("   -> Successfully processed and wrote: " + outputPath);
                } catch (IOException e) {
                    System.err.println("❌ Error processing file " + inputFile.getName() + ": " + e.getMessage());
                }
            });
        }
        shutdownExecutor(executor);
    }

    /**
     * Mode 2: Aggregate all data from all files into one output.
     */
    private void processAggregateMode(List<File> jsonFiles) {
        System.out.println("📦 Mode 2: Aggregating data from " + jsonFiles.size() + " files.");
        List<Card> allCards = new ArrayList<>();

        for (File inputFile : jsonFiles) {
            try {
                InputFile inputData = objectMapper.readValue(inputFile, InputFile.class);
                if (inputData.getCards() != null) {
                    allCards.addAll(inputData.getCards());
                }
            } catch (IOException e) {
                System.err.println("❌ Error reading file for aggregation " + inputFile.getName() + ": " + e.getMessage());
            }
        }

        if (allCards.isEmpty()) {
            System.out.println("🟡 No card data found across all files to aggregate.");
            return;
        }

        OutputFile finalOutput = transformData(allCards);

        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(java.time.LocalDateTime.now());
        String outputFileName = "aggregated-result_" + timestamp + ".json"; //
        Path outputPath = Paths.get(outputDir, outputFileName);

        try {
            objectMapper.writeValue(outputPath.toFile(), finalOutput);
            System.out.println("   -> Successfully aggregated and wrote: " + outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error writing aggregated file: " + e.getMessage());
        }
    }

    /**
     * The core data transformation logic.
     * @param cards A list of cards to be transformed.
     * @return An OutputFile object representing the new data structure.
     */
    public OutputFile transformData(List<Card> cards) {
        // Group all cards by account number
        Map<String, List<Card>> cardsByAccount = cards.stream()
                .collect(Collectors.groupingBy(Card::getAccountNumber));

        List<Account> accounts = cardsByAccount.entrySet().stream()
                // Map each entry (account number + list of cards) to an Account object
                .map(entry -> {
                    String accountNumber = entry.getKey();
                    List<Card> accountCards = entry.getValue();

                    // Within each account, group cards by product name
                    Map<String, List<Card>> cardsByProduct = accountCards.stream()
                            .collect(Collectors.groupingBy(Card::getProductName));

                    // Map each product entry to a Product object
                    List<Product> products = cardsByProduct.entrySet().stream()
                            .map(productEntry -> {
                                String productName = productEntry.getKey();
                                List<Card> productCards = productEntry.getValue();

                        // Sum balances for the same product
                                BigDecimal totalBalance = productCards.stream()
                                        .map(card -> new BigDecimal(card.getBalance()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // Create and sort card details
                                List<CardDetail> details = productCards.stream()
                                        .map(card -> new CardDetail(
                                                card.getCardNumber(),
                                                card.getBalance(),
                                                convertDate(card.getExpireDate()) // Convert date format
                                        ))
                            .sorted(Comparator.comparing(CardDetail::getCardNumber))
                                        .collect(Collectors.toList());

                                return new Product(productName, totalBalance.toPlainString(), details);
                            })
                            .collect(Collectors.toList());

                    return new Account(accountNumber, products);
                })
                // Sort accounts by account number
                .sorted(Comparator.comparing(Account::getAccountNumber))
                .collect(Collectors.toList());

        return new OutputFile(accounts.size(), accounts);
    }

    /**
     * Converts a date string from Buddhist (ddMMyyyy) to Christian (yyyy-MM-dd).
     * @param buddhistDateStr The input date string, e.g., "01092568".
     * @return The formatted Christian date string, e.g., "2025-09-01".
     */
    private String convertDate(String buddhistDateStr) {
        try {
            LocalDate buddhistDate = LocalDate.parse(buddhistDateStr, INPUT_DATE_FORMAT);
            // Convert from Buddhist year to Christian year by subtracting 543
            LocalDate christianDate = buddhistDate.minusYears(543);
            return christianDate.format(OUTPUT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Could not parse date: '" + buddhistDateStr + "'. Returning original value.");
            return buddhistDateStr; // Fallback
        }
    }

    private List<File> findInputFiles() {
        try (Stream<Path> stream = Files.list(Paths.get(inputDir))) {
            return stream
                    .map(Path::toFile)
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("❌ Error reading input directory: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
