# Cuisine Prediction Training

## Some MLlib results:

### LogisticRegressionModel model evaluation
| Parameter                    | Value    |
| :--------------------------- | -------: |
| Precision                    | 67.5614% |
| Error                        | 32.4386% |
| Weighted Precision           | 68.2420% |
| Weighted True Positive Rate  | 67.5614% |
| Weighted False Positive Rate |  2.6687% |

### NaiveBayesModel model evaluation
| Parameter                    | Value    |
| :--------------------------- | -------: |
| Precision                    | 74.8772% |
| Error                        | 25.1228% |
| Weighted Precision           | 76.0445% |
| Weighted True Positive Rate  | 74.8772% |
| Weighted False Positive Rate |  2.7983% |

### DecisionTreeModel model evaluation
| Parameter                    | Value    |
| :--------------------------- | -------: |
| Precision                    | 44.0589% |
| Error                        | 55.9411% |
| Weighted Precision           | 57.9342% |
| Weighted True Positive Rate  | 44.0589% |
| Weighted False Positive Rate |  7.7057% |

### RandomForest model evaluation
| Parameter                    | Value    |
| :--------------------------- | -------: |
| Precision                    | 32.1500% |
| Error                        | 67.8500% |
| Weighted Precision           | 51.8948% |
| Weighted True Positive Rate  | 32.1500% |
| Weighted False Positive Rate | 16.0193% |

## Data analysis notes
|||
| :---------------------------- | ------------: |
| Total training data size      | 39774 records |
| Training / Testing split      |     80% / 20% |
| Cuisines (labels / classes)   |            20 |
| Ingredients (features)        |          6703 |

### Total ingredients per cuisine
| Cuisine                                  | Ingredients #   |
| :----------------------------------------| --------------: |
| italian                                  |            2928 |
| mexican                                  |            2681 |
| southern_us                              |            2459 |
| french                                   |            2100 |
| chinese                                  |            1791 |
| indian                                   |            1664 |
| cajun_creole                             |            1575 |
| japanese                                 |            1439 |
| thai                                     |            1376 |
| spanish                                  |            1263 |
| greek                                    |            1198 |
| british                                  |            1165 |
| vietnamese                               |            1108 |
| irish                                    |             999 |
| moroccan                                 |             974 |
| filipino                                 |             947 |
| korean                                   |             898 |
| jamaican                                 |             877 |
| russian                                  |             872 |
| brazilian                                |             853 |

### Max ingredients used in a recipe per cuisine
| Ingredient                               | Max Ingr.       |
| :--------------------------------------- | --------------: |
| italian                                  |              65 |
| brazilian                                |              59 |
| mexican                                  |              52 |
| indian                                   |              49 |
| southern_us                              |              40 |
| thai                                     |              40 |
| chinese                                  |              38 |
| filipino                                 |              38 |
| jamaican                                 |              35 |
| spanish                                  |              35 |
| japanese                                 |              34 |
| french                                   |              31 |
| vietnamese                               |              31 |
| cajun_creole                             |              31 |
| moroccan                                 |              31 |
| british                                  |              30 |
| korean                                   |              29 |
| irish                                    |              27 |
| greek                                    |              27 |
| russian                                  |              25 |

### Top 20 ingredients

| Ingredient                               | Occurrences     |
| :--------------------------------------- | --------------: |
| salt                                     |           18049 |
| olive oil                                |            7972 |
| onions                                   |            7972 |
| water                                    |            7457 |
| garlic                                   |            7380 |
| sugar                                    |            6434 |
| garlic cloves                            |            6237 |
| butter                                   |            4848 |
| ground black pepper                      |            4785 |
| all-purpose flour                        |            4632 |
| pepper                                   |            4438 |
| vegetable oil                            |            4385 |
| eggs                                     |            3388 |
| soy sauce                                |            3296 |
| kosher salt                              |            3113 |
| green onions                             |            3078 |
| tomatoes                                 |            3058 |
| large eggs                               |            2948 |
| carrots                                  |            2814 |
| unsalted butter                          |            2782 |

### Bottom 20 ingredients
| Ingredient                               | Occurrences     |
| :----------------------------------------| --------------: |
| zatarain’s jambalaya mix                 |               1 |
| zatarains creole seasoning               |               1 |
| yucca root                               |               1 |
| young nettle                             |               1 |
| young leeks                              |               1 |
| yoplait® greek 2% caramel yogurt         |               1 |
| yoplait® greek 100 blackberry pie yogurt |               1 |
| yoplait                                  |               1 |
| yogurt low fat                           |               1 |
| yogurt dressing                          |               1 |
| yoghurt natural low fat                  |               1 |
| yellowtail snapper fillets               |               1 |
| yellowtail                               |               1 |
| yellowfin                                |               1 |
| yellow heirloom tomatoes                 |               1 |
| yam noodles                              |               1 |
| yam bean                                 |               1 |
| xuxu                                     |               1 |
| worcestershire sauce low sodium          |               1 |
| wood mushrooms                           |               1 |

### Total recipes per cuisine
| Cuisine                                  | Recipes         |
| :----------------------------------------| --------------: |
| italian                                  |            7838 |
| mexican                                  |            6438 |
| southern_us                              |            4320 |
| indian                                   |            3003 |
| chinese                                  |            2673 |
| french                                   |            2646 |
| cajun_creole                             |            1546 |
| thai                                     |            1539 |
| japanese                                 |            1423 |
| greek                                    |            1175 |
| spanish                                  |             989 |
| korean                                   |             830 |
| vietnamese                               |             825 |
| moroccan                                 |             821 |
| british                                  |             804 |
| filipino                                 |             755 |
| irish                                    |             667 |
| jamaican                                 |             526 |
| russian                                  |             489 |
| brazilian                                |             467 |
