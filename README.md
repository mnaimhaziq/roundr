# Rounder

## Database Setup

1. Check if the mysql-connector 8.0.29 library exists in your external libraries.
2. If not, reload the project to make sure that Maven installs this library. To do this, right-click on rounder -> maven -> Reload Project.
3. Locate the file RoundrDatabase.sql which contains the SQL statements required for the project.
4. Create a new database called game.
5. Execute the SQL query contained in the RoundrDatabase.sql file to set up the necessary tables and data.
6. Once the query has been executed successfully, the project is good to go.
7. Happy Coding!

## Before You Code...

1. Create a new folder with your module name under the `src/main/java/com/game/roundr` directory.
2. Add your new folder to the `src/main/java/module-info.java` file using the following line of code:
```
```
For example, the lobby folder has already been added to the file at line 8:
```
```
3. Create another folder with your module name under the `src/main/resources/com/game/roundr` directory. You do not need to add this folder to any file.
4. You are now ready to start coding! Controller files should go in the `src/main/java/com/game/roundr/<module name>` folder and FXML files go in the
`src/main/resources/com/game/roundr/<module name>` folder. You can see some samples of controllers and FXML files, and how to name them in the lobby folders.