# EventRecommendationEngine
CS286 data mining project

Steps to run the project on hadoop cluster:

1. Pull this code from github into /user/user01 (same as cs286 Lab1).
2. Create a folder called DATA and OUT on the same level as pom.xml.
3. Put the input file event_attendees.csv into DATA folder. Make sure to remove the headers from the first line in the file. 
4. mvn clean install of the pom.xml level (if the build fails might need to run this as root).
5. Make executionScripts folder as executable, as well as the scripts inside it. Use chmod a+x to do this.
6. Also check that the jar file generated under the target folder after 'mvn clean install' is executable. If not make it executable by using chmod.
7. Execute the createUserEventValueMatrix.sh to run the map reduce job to generate the user event value matrix.
8. The output file will be in OUT folder as part-r-*.
9. The output file should in following format userId, eventId, value.
