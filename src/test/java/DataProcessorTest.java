import org.example.model.AppConfig;
import org.example.DataProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import org.example.model.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the DataProcessor class.
 * Focuses on the data transformation logic.
 */
class DataProcessorTest {

    private DataProcessor dataProcessor;

    @BeforeEach
    void setUp() {
        // The config is not used by the transformData method, so dummy values are fine.
        AppConfig dummyConfig = new AppConfig(1, 1);
        dataProcessor = new DataProcessor(dummyConfig, "Input", "Output");
    }

    @Test
    @DisplayName("Should correctly transform data based on the provided sample")
    void testTransformData_WithSampleInput_ShouldMatchExpectedOutput() {
        // --- Arrange: Create input data based on the sample.json ---
        Card debitCard1 = new Card();
        debitCard1.setProductName("debit-card");
        debitCard1.setCardNumber("1234567890123456");
        debitCard1.setAccountNumber("1234567890123");
        debitCard1.setBalance("55.24");
        debitCard1.setExpireDate("01092568"); // Buddhist year

        Card debitCard2 = new Card();
        debitCard2.setProductName("debit-card");
        debitCard2.setCardNumber("1234567890123457");
        debitCard2.setAccountNumber("1234567890123");
        debitCard2.setBalance("999.89");
        debitCard2.setExpireDate("01052569"); // Buddhist year

        Card prepaidCard = new Card();
        prepaidCard.setProductName("prepaid-card");
        prepaidCard.setCardNumber("1234567890123458");
        prepaidCard.setAccountNumber("1234567890123");
        prepaidCard.setBalance("150.00");
        prepaidCard.setExpireDate("01032569"); // Buddhist year

        List<Card> inputCards = List.of(debitCard1, debitCard2, prepaidCard);

        // --- Act: Perform the transformation ---
        OutputFile actualOutput = dataProcessor.transformData(inputCards);

        // --- Assert: Verify the output matches the expected structure ---
        assertThat(actualOutput.getAccountTotal()).isEqualTo(1);
        assertThat(actualOutput.getAccounts()).hasSize(1);

        Account account = actualOutput.getAccounts().get(0);
        assertThat(account.getAccountNumber()).isEqualTo("1234567890123");
        assertThat(account.getProducts()).hasSize(2);

        // Verify Debit Card product
        Product debitProduct = account.getProducts().stream()
                .filter(p -> p.getName().equals("debit-card")).findFirst().orElse(null);
        assertThat(debitProduct).isNotNull();
        assertThat(debitProduct.getTotalBalance()).isEqualTo("1055.13"); // 55.24 + 999.89
        assertThat(debitProduct.getDetails()).hasSize(2);
        assertThat(debitProduct.getDetails().get(0).getCardNumber()).isEqualTo("1234567890123456"); // Sorted
        assertThat(debitProduct.getDetails().get(0).getExpireDate()).isEqualTo("2025-09-01"); // Converted
        assertThat(debitProduct.getDetails().get(1).getCardNumber()).isEqualTo("1234567890123457");
        assertThat(debitProduct.getDetails().get(1).getExpireDate()).isEqualTo("2026-05-01"); // Converted

        // Verify Prepaid Card product
        Product prepaidProduct = account.getProducts().stream()
                .filter(p -> p.getName().equals("prepaid-card")).findFirst().orElse(null);
        assertThat(prepaidProduct).isNotNull();
        assertThat(prepaidProduct.getTotalBalance()).isEqualTo("150.00");
        assertThat(prepaidProduct.getDetails()).hasSize(1);
        assertThat(prepaidProduct.getDetails().get(0).getCardNumber()).isEqualTo("1234567890123458");
        assertThat(prepaidProduct.getDetails().get(0).getExpireDate()).isEqualTo("2026-03-01"); // Converted
    }

    @Test
    @DisplayName("Should return an empty result for an empty card list")
    void testTransformData_WithEmptyInput_ShouldReturnEmptyResult() {
        // --- Act ---
        OutputFile actualOutput = dataProcessor.transformData(Collections.emptyList());

        // --- Assert ---
        assertThat(actualOutput.getAccountTotal()).isZero();
        assertThat(actualOutput.getAccounts()).isEmpty();
    }

    @Test
    @DisplayName("Should correctly group multiple accounts and sort them by account number")
    void testTransformData_WithMultipleUnsortedAccounts_ShouldSortAccounts() {
        // --- Arrange: Create cards for two accounts in reverse order ---
        Card cardAcc2 = new Card();
        cardAcc2.setAccountNumber("22222"); // Should come second
        cardAcc2.setProductName("p1");
        cardAcc2.setCardNumber("c1");
        cardAcc2.setBalance("10");
        cardAcc2.setExpireDate("01012560");

        Card cardAcc1 = new Card();
        cardAcc1.setAccountNumber("11111"); // Should come first
        cardAcc1.setProductName("p2");
        cardAcc1.setCardNumber("c2");
        cardAcc1.setBalance("20");
        cardAcc1.setExpireDate("01012560");

        // --- Act ---
        OutputFile actualOutput = dataProcessor.transformData(List.of(cardAcc2, cardAcc1));

        // --- Assert ---
        assertThat(actualOutput.getAccountTotal()).isEqualTo(2);
        assertThat(actualOutput.getAccounts())
                .extracting(Account::getAccountNumber)
                .containsExactly("11111", "22222"); // Asserting the sort order
    }

    @Test
    @DisplayName("Should sort card details by card number within a product")
    void testTransformData_WithUnsortedCards_ShouldSortCardDetails() {
        // --- Arrange ---
        Card card2 = new Card(); // Card "2"
        card2.setAccountNumber("123");
        card2.setProductName("debit-card");
        card2.setCardNumber("222222"); // Should come second
        card2.setBalance("100");
        card2.setExpireDate("01012560");

        Card card1 = new Card(); // Card "1"
        card1.setAccountNumber("123");
        card1.setProductName("debit-card");
        card1.setCardNumber("111111"); // Should come first
        card1.setBalance("100");
        card1.setExpireDate("01012560");

        // --- Act ---
        OutputFile actualOutput = dataProcessor.transformData(List.of(card2, card1));

        // --- Assert ---
        Product product = actualOutput.getAccounts().get(0).getProducts().get(0);
        assertThat(product.getDetails())
                .extracting(CardDetail::getCardNumber)
                .containsExactly("111111", "222222"); // Asserting sort order
    }

    @Test
    @DisplayName("Should throw NumberFormatException for an invalid balance string")
    void testTransformData_WithInvalidBalance_ShouldThrowException() {
        // --- Arrange ---
        Card card = new Card();
        card.setBalance("not-a-number"); // Invalid balance
        card.setAccountNumber("123");
        card.setProductName("p1");
        card.setCardNumber("c1");
        card.setExpireDate("01012560");

        // --- Act & Assert ---
        Exception exception = assertThrows(NumberFormatException.class, () -> {
            dataProcessor.transformData(List.of(card));
        });

        assertThat(exception).isInstanceOf(NumberFormatException.class);
    }
}