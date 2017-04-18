# CooperativeCaching
Presently, web browsers don't contact with each other for sharing web cache. So when a miss occurs in local web cache, the request will be sent to web cache server and then to source server, despite the existence of the requested web resource in the same subnet. In this situation, each browser is separated and full resource utilization doesn't happen. 

But if each browser cooperates with each other, a node will try to find web resource in the other node when hit in local web cache misses.

This project aims at implementing "A Distributed Browser-level Web Caching System Based on Chord".

Chord is a protocol and algorithm for a peer-to-peer distributed hash table. By implementing cooperative caching based on Chord, we intend to increase cache hit. Accordingly, decreasing the time required for loading the webpage's content.

In a close area, the chances that node needs the same resource is higher. If nodes are connected directly with peer-to-peer, the waiting time will be reduced. And we believe the problem will be solved to some extent.

#Features
* connect different systems in a same subnet.
* option to manually connect to a system by entering ip address and username of user or simply enter the broadcast address of the subnet and leave the connection part to smart browser
![shot_1](https://cloud.githubusercontent.com/assets/20504256/25142550/e74d9652-2484-11e7-9c6d-7f5531eb6d7e.png)

![mysmartbrowser](https://cloud.githubusercontent.com/assets/20504256/25142168/a91cc354-2483-11e7-9a26-101f67ca8fb6.png)
