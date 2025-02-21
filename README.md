Business Logic used:
--------------------
I have decided to return requested logs in a file to account for very large requests and prevent out of memory error in java.



How to run the code in linux (Ubuntu):
- download the zip file and unpack it  directory.
- make sure you have installed java in your machine.
- run the following command [ java -jar LogFecher-1.0-SNAPSHOT.jar ]
- Go to http://localhost:8080/swagger-ui/index.html#/ in your browser and test the API with your input.




NOTES:
This code will create a temporary file in [/tmp/log_fetcher] for each request. We will need a crone job to delete these files periodically.
