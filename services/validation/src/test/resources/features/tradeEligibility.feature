
Feature: Validate trade eligibility
  Scenario: Validate a trade message received on trade history topic
    Given trade message with id  1 is received
    When the trade message is a product type of 'FX Option'
    Then the trade should be classed as eligible
    And the trade message is send to trade control
