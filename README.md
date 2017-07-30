# CellularNetworkMonitor

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](License.txt)

An Android application that registers your device on to a Django based server keeping IMEI, 
carrier service, model make and Android Version as phone identifiers using a HTTP POST. 
Serialization of data sent over the network is achieved using [Google Protocol Buffers](https://github.com/google/protobuf) 

# Features:  
1. Tracks location based on NETWORK_PROVIDER (strict and relaxed) and Fused API  
2. Monitors [RSSI](https://en.wikipedia.org/wiki/Received_signal_strength_indication) and DBM levels  
3. Current network state and other network parameters such as MCC, MNC, LAC and CID  
4. Data activity  
5. Stores all data in SQLite DB on local device storage  
6. Reports can be exported in the form of CSV files  
 
# More to come:
7. Statistical Analysis in the form of UI  
8. Map UI integration lets you know in what areas you get what Network Reception(both RSSI and data type)

# UI
## Map
The map view will show the users their signal strength over three time periods: day, week, and month. Each of these periods will begin at the beginning of the respective interval, and therfore there will not be any half weeks displayed.
Given the amount of data returned, clustering will need to be done to display the data to the users. Each cluster will need to reflect the average signal strength over the data points, and should not contain different technologies (3G/LTE).

## Statistics
The statistics view will also be over the three time periods, but individual data entries will not be shown to users. Rather, aggregate data such as technology usage and overall signal strength will be displayed.

# Protobuf usage, installation and compilation

# License
This project is licensed under the [MIT License](https://en.wikipedia.org/wiki/MIT_License).

Copyright (c) 2016 [UB Computer Science](https://www.cse.buffalo.edu/)
