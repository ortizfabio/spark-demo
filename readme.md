

1. Define a new Twitter application
-----------------------------------
   * Go to http://dev.twitter.com and sign in with your Twitter account
   * Go to <code>My applications</code> and create a new application (put any URL you want in the <code>Website</code> field)
   * After your application is created, go to the <code>Settings</code> tab and ensure the access is set to <code>Read and Write</code>


2. Edit the <code>application.conf</code> configuration file under /src/main/resources
------------------------------------------------------------------
   * Open the file in the editor and look for the section kafkaConnect
   * and enter the server and ports for zookeeper and kafka server
     <pre>
      kafka.host=[kafka hostname]
      kafka.port=[kafka port]
      zk.host=[zookeeper hostname]
      zk.port=[zookeeper port]
     </pre>
 
3. Edit the <code>application.conf</code> configuration file under /src/main/resources
------------------------------------------------------------------
   * Open the file in the editor and look for the section oauth
   * Insert the following (these values can be found in your application details on the Twitter developer site, http://dev.twitter.com):
     <pre>
      oauth.consumerKey=[your app's consumer key]
      oauth.consumerSecret=[your app's consumer secret]
      oauth.accessToken=[your access token]
      oauth.accessTokenSecret=[your access secret]
     </pre>
   * NOTE: You need to manually generate an access token and secret on the Twitter developer site. This access token is valid for your own Twitter account. 

5. Run
--------
   * In the IDE or from the command line run the Demo object
   * If the tweeter configuration is valid it will start printing the tweets that match the query "track=london" otherwise you probably see a HTTP 401 code indicating invalid credentials. The tweets will be saved into Kafka server.