# Windowing

| type | example | description |
|------|---------|-------------|
|Global Window| Window.globa| A single bucket for all events|
|Fixed Windows| Window.fixed(“1 hour”)|Creates windows of fixed duration (no overlap)|
|Sliding Window|Window.sliding(“1 hour”, “10 minutes”)|Creates a sliding window of fixed size, sliding every specified duration (with overlap)|





