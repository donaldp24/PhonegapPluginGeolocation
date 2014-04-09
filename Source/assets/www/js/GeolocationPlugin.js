
var GeolocationPlugin = {
		
		startservice:function(options) {
			cordova.exec(function(data){}, function(error){alert(error);}, 'GeolocationPlugin', 'startservice', [options]);
		},		
		stopservice:function(options) {
			cordova.exec(function(data){}, function(error){alert(error);}, 'GeolocationPlugin', 'stopservice', [options]);
		},		
		getcurrentpositions:function(options, successCurFn, failureFn) {
			cordova.exec(successCurFn, failureFn, 'GeolocationPlugin', 'getcurrentposition', [options]);
		},		
		getpreviouspositions:function(options, successPrevFn, failureFn) {
			cordova.exec(successPrevFn, failureFn, 'GeolocationPlugin', 'getpreviouspositions', [options]);
		},
		isServiceRunning:function(isServiceRunning, errorLogger) {
			cordova.exec(isServiceRunning, errorLogger, 'GeolocationPlugin', 'isServiceRunning', []);
		}
};


//navigator.geolocationplugin = GeolocationPlugin;
//navigator["geolocationplugin"] = GeolocationPlugin;