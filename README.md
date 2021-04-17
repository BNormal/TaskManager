# Dreambot Scripts

## JavaDoc for DreamBot
- https://dreambot.org/javadocs/

## Getting Started
- When using Eclipse make sure to include client.jar in your project
- Right click your project and click **Properties**
- Go to the **Java Build Path** column and **Libraries** tab
- Click on the **Add External Jar** button and go to *"C:\Users\{your pc name here}\DreamBot\BotData"* and open **client.jar**
- Do the same process as before except you will be adding the jar file **gson.jar** which should be located in the **libs** folder in your project
- Now select **Apply and Close**
- Copy the **gson.jar** *(may have a version number on the jar file)* and paste a new copy in *"C:\Users\{your pc name here}\DreamBot\Libs"*
- Once you've copied the **gson.jar** file to the **Libs** folder, open the **libs** file in the project source folder and record all the names of the jar files in the **Libs** folder into the **libs** file.

## Add your script to the dreambot
- Using Eclipse go to **File** > **Export...**
- Expand **Java**, select **Jar File** and then click **Next**
- Uncheck everything and expand your project (typically named dreambot) and check only the **src**
- Under **Select the export destination:** click **Browse** and go to *"C:\Users\{your pc name here}\DreamBot\Scripts"*
- After selecting the destination, put the name of your script after the destination *"C:\Users\{your pc name here}\DreamBot\Scripts\Bots.jar"*
- Click **Finish** and click **Yes** to overwrite and lastly click **Ok**.
