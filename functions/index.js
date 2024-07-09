/* eslint-disable */
// Your entire file here



"use strict";
//import {initializeApp, applicationDefault } from 'firebase-admin/app';
//import { getMessaging } from "firebase-admin/messaging";
//import express, { json } from "express";

const functions = require("firebase-functions");
const admin = require("firebase-admin");

process.env.GOOGLE_APPLICATION_CREDENTIALS;

//const app = express();
//app.use(express.json());
//
//initializeApp({
//  credential: applicationDefault(),
//  projectId: 'mindcheckdatacollectionapp',
//});
//
//
//app.listen(3000, function () {
//  console.log("Server started on port 3000");
//})

admin.initializeApp();

exports.sendReminderNotifications = functions.pubsub.schedule("every 24 hours")
.onRun(async (context) => {
  const minuteAgo = admin.firestore.Timestamp.now().toMillis() - (1000*60);
  console.log(`Timestamp to be considered: ${minuteAgo}`);
  const twoWeeksAgo = admin.firestore.Timestamp.now().toMillis()-
  (2*7*24*60*60*1000);
  const questionnaireRef = admin.firestore().collection("questionnaire");
  const querySnapshot = await questionnaireRef.get();
  querySnapshot.forEach(doc => {
    console.log(doc.id, " => ", admin.firestore.Timestamp.fromMillis(doc.data().Timestamp));
  });
  console.log(`Total questionnaires: ${querySnapshot.size}`);
  //const outdatedQuestionnaire = await questionnaireRef.where("Timestamp", '<=', twoWeeksAgo).get();
  const outdatedQuestionnaire = await questionnaireRef.where("Timestamp", '<=', admin.firestore.Timestamp.fromMillis(minuteAgo)).get();

  console.log(`Questionnaires fetched: ${outdatedQuestionnaire.size}`);

  const usersRef = admin.firestore().collection("mobileUser");

  if(!outdatedQuestionnaire.empty) {
    outdatedQuestionnaire.forEach(async doc => {
      const userId = doc.data().UserID;
      console.log(`Processing questionnaire for UserID: ${userId}`);
      const userQuerySnapshot = await usersRef.where("userID", '==', userId).get();
      //const userDoc = await usersRef.doc(userId).get();

      if(!userQuerySnapshot.empty) {
        userQuerySnapshot.forEach(doc => {
          const userToken = doc.data().fcmToken;
          if(userToken) {
            const payload = {
              notification: {
                title: "Test Your Depression Score",
                body: "It's been 2 weeks since you last completed your PHQ-9 questionnaire, please login to complete the questionnaire."
              }
            };
            admin.messaging().sendToDevice(userToken, payload).then(response => {
              console.log(`Successfully sent message to ${userId}`)
              response.results.forEach((result, index) => {
                const error = result.error;
                if(error) {
                  console.error("Failure sending notification to", userId, error);
                }
              });
            }).catch(error => {
              console.error("Error sending notification:", error);
            });
          } else {
            console.warn(`No FCM token found for UserID: ${userId}`);
          }
        })
      } else {
        console.warn(`User document not found for UserID: ${userId}`);
      }
    });
  } else {
    console.log("No questionnaires require reminders at this time.");
  }
  console.log("Function execution ended");
  return null;
});
/* eslint-enable */

