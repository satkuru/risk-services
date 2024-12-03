
Feature: Validate trade eligibility
  Scenario: Validate a trade message received on trade history topic
    When a trade message with a product type 'FX Option' is received in valuation service
    Then the trade should be classified as eligible
    And the trade message is send to trade control
  Scenario: Validate a trade message that is not eligible for risk service
    When a trade message with a product type 'FX Basket' is received in valuation service
    Then the trade should not be classified as eligible
    And the trade message is send to trade control