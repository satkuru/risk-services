
Feature: Validate trade eligibility
  Scenario: Validate a trade message received on trade history topic
    When a trade message is received in valuation service
      |id   |productType|account    |maturityDate |notional   |
      |1    |FX Option  |Abc123453se|2024-10-25   |10000000 |
    Then the trade should be classified as eligible
    And the trade message is send to trade control
  Scenario: Validate a trade message that is not eligible for risk service
    When a trade message is received in valuation service
      |id   |productType|account    |maturityDate |notional   |
      |1    |FX Basket  |Abc123453se|2024-10-26   |16000000 |
    Then the trade should not be classified as eligible
    And the trade message is send to trade control