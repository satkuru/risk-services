Feature: Validate trade eligibility

  @TradeEligibility
  Scenario Outline: Validate a trade message received on trade history topic
    When a trade message is received in valuation service
      | reference | productType | account      | maturityDate | notional        |
      | <ref>     | <product>   | <accountRef> | <maturity>   | <notionalValue> |
    Then the trade with reference '<ref>' should be classified as eligible
    And the trade message is send to trade control
    Examples:
      | ref                                  | product    | accountRef  | maturity   | notionalValue |
      | e2e2271c-7eea-4747-a9f5-2f6e26cb8256 | FX Option  | Abc123453se | 2024-10-25 | 10000000      |
      | ab7127ad-0bd6-4b10-a953-904422b6150b | FX Forward | Abc893453se | 2024-10-25 | 13000000      |

  @TradeEligibility
  Scenario Outline: Validate a trade message that is not eligible for risk service
    When a trade message is received in valuation service
      | reference | productType | account      | maturityDate | notional        |
      | <ref>     | <product>   | <accountRef> | <maturity>   | <notionalValue> |
    Then the trade with reference '<ref>' should not be classified as eligible
    And the trade message is send to trade control
    Examples:
      | ref                                  | product   | accountRef  | maturity   | notionalValue |
      | 24020e19-4b3c-4ed6-a1c0-229a2458edc8 | FX Basket | Abc123453se | 2024-10-25 | 16000000      |