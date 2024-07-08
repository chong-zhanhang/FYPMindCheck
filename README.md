# MindCheck
## Description
MindCheck is an Android mobile application that detects depression based on user's typing metadata on a daily basis by employing a multi-layer feedforward neural network that classifies users into low-risk groups and high-risk groups. The application is built with privacy in mind, where the app does not know what the user is typing as it only collects speed-related data (eg hold time, flight time, typing speed etc), and it does not share data to other parties without approval of the user. It uses Java, Kotlin, and Python as the main programming languages, Firebase as its authentication service and database service, Fleksy as the typing data collection mechanism, and Flask as the API framework.

## Navigation
The 'app/src/main/com.example.mindcheckdatacollectionapp/' directory consists of all the development files for the Android app.
The 'SampleKeyboardService.kt" file consists of codes to initialize the custom Fleksy keyboard for typing metadata collection.
The 'ui.theme' folder consists of all the Java and Kotlin files for the activities and other class files, and the '/Fragments' folder consists of all the Android fragments.
The backend server which handles requests to predict depression occurrence and requests to fine-tune user-specific depression detection models can be found through https://github.com/chong-zhanhang/MindCheckBackend

## Features
MindCheck consists of features that connects depressive users to professional mental healthcare:
- Depression Detection (Neural network model to predict user's depression occurrence based on user's typing data)
- Therapist Pairing (Suggest therapists based on general vicinity from user's location) 
- Appointments (Set up appointments with paired therapists)
- Data Visualization (Show visualization that help users understand current mental health status, eg depression history chart, typing data trends, depression detection model performance etc)
- Data Sharing (Shares record and data with therapist, and revokes viewing access if user opt out of data sharing)
- Notifications (Alert users and therapist if depression is detected)
