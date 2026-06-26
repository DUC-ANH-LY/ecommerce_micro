# ecommerce_micro

- Standalon mongo (3vpus, 6G memory)
![alt text](image.png)


~5.01 requests per second (QPS) over the entire duration of the test.

- Sharding with others mongo (3vpus, 6G memory) -> not handle full text search
~ 3 
- High load 
![alt text](image-3.png)
- Can't handle other request 
-> MOngo db if too many request full text search, server full table scan despite index ( even sharding ) -> hold server for a minitues 
- for full text search or search in general using elastic search 