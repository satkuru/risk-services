
Feature: Validate trade eligibility
  Scenario Outline: Validate a trade message received on trade history topic
    When a trade message is received in valuation service
      |id    |productType|account      |maturityDate |notional       |
      |<ref> |<product>  |<accountRef> |<maturity>   |<notionalValue>|
    Then the trade should be classified as eligible
    And the trade message is send to trade control
    Examples:
      |ref  |product   |accountRef    |maturity     |notionalValue |
      |1    |FX Option |Abc123453se   |2024-10-25   |10000000      |
      |2    |FX Forward|Abc893453se   |2024-10-25   |13000000      |
  Scenario Outline: Validate a trade message that is not eligible for risk service
    When a trade message is received in valuation service
      |id    |productType|account      |maturityDate |notional       |
      |<ref> |<product>  |<accountRef> |<maturity>   |<notionalValue>|
    Then the trade should not be classified as eligible
    And the trade message is send to trade control
    Examples:
      |ref   |product   |accountRef    |maturity     |notionalValue |
      |10    |FX Basket |Abc123453se   |2024-10-25   |16000000      |