## Data analysis notes


### Tested clustering against the training data 01

| Param                |   Value   |
| :------------------- | --------: |
| trainingSize         |   4898431 |
| numClusters          |        24 |
| iterations           |       100 |
| runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |    16 |  2807886 |
| neptune.             |   1072017 |    16 |  1072017 |
| normal.              |    972781 |    16 |   916708 |     0 |    54577 |    23 |      697 |
| satan.               |     15892 |    16 |    15891 |     0 |        1 |
| ipsweep.             |     12481 |    16 |    12480 |     0 |        1 |
| portsweep.           |     10413 |    16 |    10402 |    15 |        2 |     6 |        2 |
| nmap.                |      2316 |    16 |     2316 |
| back.                |      2203 |     0 |     2169 |    16 |       34 |
| warezclient.         |      1020 |    16 |      960 |    14 |       59 |    17 |        1 |
| teardrop.            |       979 |    16 |      979 |
| pod.                 |       264 |    16 |      264 |
| guess_passwd.        |        53 |    16 |       53 |
| buffer_overflow.     |        30 |    16 |       28 |     0 |        2 |
| land.                |        21 |    16 |       21 |
| warezmaster.         |        20 |    13 |       15 |    16 |        4 |    12 |        1 |
| imap.                |        12 |    16 |       11 |    21 |        1 |
| rootkit.             |        10 |    16 |        9 |     0 |        1 |
| loadmodule.          |         9 |    16 |        9 |
| ftp_write.           |         8 |    16 |        7 |     0 |        1 |
| multihop.            |         7 |    16 |        4 |    21 |        2 |     0 |        1 |
| phf.                 |         4 |    16 |        4 |
| perl.                |         3 |    16 |        3 |
| spy.                 |         2 |    16 |        2 |


| Results Info         |   Value        |
| :------------------- | -------------: |
| WSSSE                | 1.1396195292616312E14 |
| Training Runtime     |  13:47 (mm:ss) |
| Prediction Runtime   |  00:00 (mm:ss) |

| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


### Tested clustering against the training data 02

| Input Parameters     |   Value   |
| :------------------- | --------: |
| Training Data Size   |   4898431 |
| Clusters             |        24 |
| Iterations           |       500 |
| Runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |     0 |  2807886 |
| neptune.             |   1072017 |     0 |  1072017 |
| normal.              |    972781 |     0 |   921777 |    20 |    49564 |    17 |      633 |
| satan.               |     15892 |     0 |    15891 |    20 |        1 |
| ipsweep.             |     12481 |     0 |    12480 |    20 |        1 |
| portsweep.           |     10413 |     0 |    10402 |    19 |        2 |     6 |        2 |
| nmap.                |      2316 |     0 |     2316 |
| back.                |      2203 |     0 |     2203 |
| warezclient.         |      1020 |     0 |      960 |    13 |       59 |    14 |        1 |
| teardrop.            |       979 |     0 |      979 |
| pod.                 |       264 |     0 |      264 |
| guess_passwd.        |        53 |     0 |       53 |
| buffer_overflow.     |        30 |     0 |       28 |    20 |        2 |
| land.                |        21 |     0 |       21 |
| warezmaster.         |        20 |    11 |       15 |     0 |        4 |    16 |        1 |
| imap.                |        12 |     0 |       11 |    21 |        1 |
| rootkit.             |        10 |     0 |        9 |    20 |        1 |
| loadmodule.          |         9 |     0 |        9 |
| ftp_write.           |         8 |     0 |        7 |    20 |        1 |
| multihop.            |         7 |     0 |        4 |    21 |        1 |    16 |        1 |
| phf.                 |         4 |     0 |        4 |
| perl.                |         3 |     0 |        3 |
| spy.                 |         2 |     0 |        2 |

| Results Info         | Value         |
| :------------------- | ------------: |
| WSSSE                | 1.1224855630580314E14   |
| Training Runtime     | 14:34 (mm:ss) |
| Prediction Runtime   | 00:00 (mm:ss) |

| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


### Tested clustering against the training data 03

| Input Parameters     |   Value   |
| :------------------- | --------: |
| Training Data Size   |   4898431 |
| Clusters             |        12 |
| Iterations           |       100 |
| Runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |     0 |  2807886 |
| neptune.             |   1072017 |     0 |  1072017 |
| normal.              |    972781 |     0 |   972522 |    11 |      245 |     9 |       13 |
| satan.               |     15892 |     0 |    15892 |
| ipsweep.             |     12481 |     0 |    12481 |
| portsweep.           |     10413 |     0 |    10402 |     8 |        2 |     9 |        2 |
| nmap.                |      2316 |     0 |     2316 |
| back.                |      2203 |     0 |     2203 |
| warezclient.         |      1020 |     0 |      960 |     9 |       59 |    11 |        1 |
| teardrop.            |       979 |     0 |      979 |
| pod.                 |       264 |     0 |      264 |
| guess_passwd.        |        53 |     0 |       53 |
| buffer_overflow.     |        30 |     0 |       30 |
| land.                |        21 |     0 |       21 |
| warezmaster.         |        20 |     0 |       20 |
| imap.                |        12 |     0 |       12 |
| rootkit.             |        10 |     0 |       10 |
| loadmodule.          |         9 |     0 |        9 |
| ftp_write.           |         8 |     0 |        8 |
| multihop.            |         7 |     0 |        7 |
| phf.                 |         4 |     0 |        4 |
| perl.                |         3 |     0 |        3 |
| spy.                 |         2 |     0 |        2 |

| Results Info         | Value         |
| :------------------- | ------------: |
| WSSSE                | 2.9091938490376425E15   |
| Training Runtime     | 05:47 (mm:ss) |
| Prediction Runtime   | 00:00 (mm:ss) |


| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


### Tested clustering against the training data 04

| Input Parameters     |   Value   |
| :------------------- | --------: |
| Training Data Size   |   4898431 |
| Clusters             |        46 |
| Iterations           |       500 |
| Runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |     0 |  2807886 |
| neptune.             |   1072017 |     0 |  1072016 |    45 |        1 |
| normal.              |    972781 |     0 |   691412 |    42 |   178950 |    38 |    62689 |
| satan.               |     15892 |     0 |    15890 |    42 |        1 |    38 |        1 |
| ipsweep.             |     12481 |     0 |    12480 |    37 |        1 |
| portsweep.           |     10413 |     0 |    10402 |     8 |        2 |    17 |        2 |
| nmap.                |      2316 |     0 |     2316 |
| back.                |      2203 |    21 |     2181 |    45 |       22 |
| warezclient.         |      1020 |     0 |      632 |    42 |      296 |    11 |       59 |
| teardrop.            |       979 |     0 |      979 |
| pod.                 |       264 |     0 |      264 |
| guess_passwd.        |        53 |     0 |       52 |    42 |        1 |
| buffer_overflow.     |        30 |    42 |       25 |     0 |        3 |    28 |        1 |
| land.                |        21 |     0 |       21 |
| warezmaster.         |        20 |    14 |       15 |     0 |        2 |    42 |        2 |
| imap.                |        12 |     0 |       10 |    24 |        1 |    38 |        1 |
| rootkit.             |        10 |     0 |        6 |    42 |        2 |    38 |        1 |
| loadmodule.          |         9 |     0 |        5 |    42 |        3 |    38 |        1 |
| ftp_write.           |         8 |     0 |        6 |    42 |        1 |    37 |        1 |
| multihop.            |         7 |     0 |        3 |    23 |        1 |    18 |        1 |
| phf.                 |         4 |    38 |        4 |
| perl.                |         3 |    42 |        3 |
| spy.                 |         2 |     0 |        2 |

| Results Info         | Value         |
| :------------------- | ------------: |
| WSSSE                | 1.601204633150887E13 |
| Training Runtime     | 41:42 (mm:ss) |
| Prediction Runtime   | 00:00 (mm:ss) |


| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


### Tested clustering against the training data 05

| Input Parameters     |   Value   |
| :------------------- | --------: |
| Training Data Size   |   4898431 |
| Clusters             |        69 |
| Iterations           |       100 |
| Runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |     0 |  2807658 |    54 |      228 |
| neptune.             |   1072017 |    54 |  1072016 |    47 |        1 |
| normal.              |    972781 |    54 |   411638 |    46 |   196646 |    20 |   103568 |
| satan.               |     15892 |    54 |    15882 |     0 |        7 |    46 |        2 |
| ipsweep.             |     12481 |    54 |    12480 |    34 |        1 |
| portsweep.           |     10413 |    54 |     9420 |    21 |      495 |    63 |      291 |
| nmap.                |      2316 |    54 |     2316 |
| back.                |      2203 |    38 |     2172 |    67 |       17 |    66 |        8 |
| warezclient.         |      1020 |    54 |      590 |    20 |      275 |    12 |       59 |
| teardrop.            |       979 |    54 |      979 |
| pod.                 |       264 |     0 |      259 |    54 |        5 |
| guess_passwd.        |        53 |    54 |       52 |    46 |        1 |
| buffer_overflow.     |        30 |    20 |       15 |    58 |        8 |    46 |        5 |
| land.                |        21 |    54 |       21 |
| warezmaster.         |        20 |    11 |       15 |    58 |        1 |    16 |        1 |
| imap.                |        12 |    54 |        9 |     0 |        1 |    40 |        1 |
| rootkit.             |        10 |    54 |        6 |    62 |        1 |    58 |        1 |
| loadmodule.          |         9 |    46 |        6 |    58 |        2 |    40 |        1 |
| ftp_write.           |         8 |    54 |        6 |    34 |        1 |    20 |        1 |
| multihop.            |         7 |    54 |        2 |    55 |        1 |    40 |        1 |
| phf.                 |         4 |    40 |        4 |
| perl.                |         3 |    20 |        3 |
| spy.                 |         2 |    46 |        2 |

| Results Info         | Value         |
| :------------------- | ------------: |
| WSSSE                | 4.12917795099456E12 |
| Training Runtime     | 28:31 (mm:ss) |
| Prediction Runtime   | 00:00 (mm:ss) |


| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


### Tested clustering against the training data 06

| Input Parameters     |   Value   |
| :------------------- | --------: |
| Training Data Size   |   4898431 |
| Clusters             |        92 |
| Iterations           |       100 |
| Runs                 |         3 |

| Label                |   Total   | Clust |  Count   | Clust |  Count   | Clust |  Count   |
| -------------------- | --------: | ----: | -------: | ----: | -------: | ----: | -------: |
| smurf.               |   2807886 |    13 |  2279118 |    84 |   527608 |    58 |     1045 |
| neptune.             |   1072017 |    67 |  1072016 |    73 |        1 |
| normal.              |    972781 |    67 |   374352 |    45 |   194204 |    55 |   105583 |
| satan.               |     15892 |    67 |    15881 |    58 |        7 |    45 |        3 |
| ipsweep.             |     12481 |    67 |    12480 |    83 |        1 |
| portsweep.           |     10413 |    67 |     9420 |    64 |      512 |    72 |      362 |
| nmap.                |      2316 |    67 |     2316 |
| back.                |      2203 |    38 |     2173 |    47 |       19 |    73 |       11 |
| warezclient.         |      1020 |    67 |      590 |    55 |      275 |    14 |       59 |
| teardrop.            |       979 |    67 |      979 |
| pod.                 |       264 |    58 |      259 |    67 |        5 |
| guess_passwd.        |        53 |    67 |       52 |    55 |        1 |
| buffer_overflow.     |        30 |    55 |       15 |    31 |       10 |    45 |        3 |
| land.                |        21 |    67 |       21 |
| warezmaster.         |        20 |    15 |       15 |    62 |        1 |    68 |        1 |
| imap.                |        12 |    67 |        9 |    70 |        1 |    58 |        1 |
| rootkit.             |        10 |    67 |        6 |    68 |        1 |    55 |        1 |
| loadmodule.          |         9 |    45 |        5 |    31 |        2 |    55 |        1 |
| ftp_write.           |         8 |    67 |        6 |    39 |        1 |    55 |        1 |
| multihop.            |         7 |    67 |        2 |    68 |        1 |    58 |        1 |
| phf.                 |         4 |    68 |        4 |
| perl.                |         3 |    55 |        3 |
| spy.                 |         2 |    45 |        2 |

| Results Info         | Value            |
| :------------------- | ---------------: |
| WSSSE                | 1.8860300144367476E12 |
| Training Runtime     | 33:32 (mm:ss) |
| Prediction Runtime   | 00:00 (mm:ss) |


| Legend ||
| ------ | -------------------------------- |
| WSSSE  | Within Set Sum of Squared Errors |
| Clust  | Cluster Id                       |


[Home](../README.md)
