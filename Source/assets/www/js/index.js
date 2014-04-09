/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {   
    	//navigator.geolocationplugin = GeolocationPlugin;
    	//navigator["geolocationplugin"] = GeolocationPlugin;
    	
    	GeolocationPlugin.isServiceRunning();	
    	
    	app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        /*
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');
		*/
        console.log('Received Event: ' + id);
    }
};


///////////////////////////////////////////////////////////////

function updateUI(eventType)
{
	var startBtn = document.getElementById("startService");
	var endBtn = document.getElementById("endService");
	var curBtn = document.getElementById("getCurrentPositions");
	var preBtn = document.getElementById("getPreviousPositions");
	
	if (eventType == "onStartService") {
		startBtn.disabled = true;
		endBtn.disabled = false;
		curBtn.disabled = false;
		preBtn.disabled = false;
	}
	else if (eventType == "onStopService") {
		startBtn.disabled = false;
		endBtn.disabled = true;
		curBtn.disabled = true;
		preBtn.disabled = true;		
	}
}


function onStartService()
{
	updateUI("onStartService");
	
	var options = { url: "http://www.test.com", // URL to send positions as JSON 
		    requestFrequency: "5000",			// how often to request positions
		    maximumAge: "3000",					// same as cordova geolocationOptions
		    timeout: "5000",					// same as cordova geolocationOptions
		    maxPositions: "10",					// send to server when X positions have been recorded
		    maxSeconds: "60",					// send to server when X seconds have passed
		    notifIcon: "/assets/www/noti.icon",	// icon to display on the notification(path relative to /assets/www)
		    notifText: "start service notify"	// text to display on the notification
			};
	
	GeolocationPlugin.startservice(options);
	//navigator.geolocationplugin.startservice(options);
}

function onStopService()
{
	updateUI("onStopService");
	
	GeolocationPlugin.stopservice(true);
	//navigator.geolocationplugin.stopservice(true);
}

function onGetCurrentPosition()
{
	var options = {
		    maximumAge: "3000",					// same as cordova geolocationOptions
		    timeout: "5000" 					// same as cordova geolocationOptions
			};
	
	GeolocationPlugin.getcurrentpositions(options, successCurFn, failureFn);
	//navigator.geolocationplugin.getcurrentpositions(options, successCurFn, failureFn);
}

function onPreviousPositions()
{
	var options = {
		    numPositions: "10",					// send to server when X positions have been recorded
		    numSeconds: "6000"					// send to server when X milliseconds have passed
			};
	
	GeolocationPlugin.getpreviouspositions(options, successPrevFn, failureFn);
	//navigator.geolocationplugin.getpreviouspositions(options, successPrevFn, failureFn);
}

function onIsServiceRunning()
{
	GeolocationPlugin.isServiceRunning(isServiceRunning, errorLogger);
}

function successCurFn(objPosition)
{
	// Example for testing.
	if (objPosition != null)
	{
		alert("dt=" + objPosition[0].dt + 
				", lat=" + objPosition[0].lat + 
				", lon=" + objPosition[0].lon + 
				", acc=" + objPosition[0].acc +
				", alt=" + objPosition[0].alt +
				", hdg=" + objPosition[0].hdg + 
				", spd=" + objPosition[0].spd);
	}	
}

function successPrevFn(arrPositions)
{
	// Example for testing.
	for(var one in arrPositions)
	{		
		alert("dt=" + arrPositions[one].dt + 
				", lat=" + arrPositions[one].lat + 
				", lon=" + arrPositions[one].lon + 
				", acc=" + arrPositions[one].acc +
				", alt=" + arrPositions[one].alt +
				", hdg=" + arrPositions[one].hdg + 
				", spd=" + arrPositions[one].spd);
		break;
	}
}

function failureFn(error)
{
	alert(error);
}

function isServiceRunning(flag)
{	
	var startBtn = document.getElementById("startService");
	var endBtn = document.getElementById("endService");
	var curBtn = document.getElementById("getCurrentPositions");
	var preBtn = document.getElementById("getPreviousPositions");

	if (flag > 0) {
		startBtn.disabled = true;
		endBtn.disabled = false;
		curBtn.disabled = false;
		preBtn.disabled = false;		
	}
	else {
		startBtn.disabled = false;
		endBtn.disabled = true;
		curBtn.disabled = true;
		preBtn.disabled = true;		
	}
}

function errorLogger(error)
{
	alert(error);	
}


////////////////////////////////////////////////////////////////