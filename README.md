# Chatapp
A anonymous chat website that pairs you with random people online powered by Spring boot, mySQL and redis. Designed with persistent session that would not be cleared even after refreshing the page. Ensuring ensuring your chat would not end unless you choose to.

![image](https://github.com/user-attachments/assets/3f1692e8-41ba-482a-afb3-0a3fc8f47182)

### Structure
![image](https://github.com/user-attachments/assets/ad222b10-e5fe-4ac1-8587-5eb8c1534ece)

#### Real-time dilivery
The communication between front-end and back-end is done through websocket to ensure messages can be delivered in real-time. 
#### Anonymity
Our server serves as a proxy for users, relaying messages while hiding users from one another. And We assign unique ID along side some miscellaneous information to each user so we can deliver each message to the corect receiver. 
#### Persistent session
To implement recoverble chatting session, we store all messages sent and user related data in MySQL database. However, some stored data are utilized more often then others, such as users' ID, thus we incorperated redis to cache these data. 
#### horizontal scaling
To support horizontal scaling, we added distributed lock(Reddison RLock) to pairng to prevent collition in paring and redis pubsub to propagate messages between server instances when paired user are served by diffrent instances.




### credit
The front-end page is adapted from https://ithelp.ithome.com.tw/articles/10239615, made by 阿翔 (nianxiang)
