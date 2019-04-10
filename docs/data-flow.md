Each component in the API was designed to be pluggable allowing users to mix and match between the framework's built-in components and user provided custom components

(images/HighLevelDataFlow.png)


1. Events: Input Dataset each record has a timestamp

2. Windowing: Events are grouped into windows for processing e.g. 1 hour, 24 hours

3. Aggregation: Data structure and processing of the grouped events collected in each window

4. Anomaly Detection Model: Analyzes an aggregation and decides if it’s an anomaly, produces reports

5. Reports: Output Dataset of an anomaly detection analysis, produced when examining each aggregation
