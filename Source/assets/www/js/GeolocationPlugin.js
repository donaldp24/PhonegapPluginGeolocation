
var GeolocationPlugin = {
		
		startservice:function(options) {
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'startservice', [options]);
		},		
		stopservice:function(options) {
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'stopservice', [options]);
		},		
		getcurrentpositions:function(options, successCurFn, failureFn) {
			cordova.exec(successCurFn, failureFn, 'GeolocationPlugin', 'getcurrentpositions', [options]);
		},		
		getpreviouspositions:function(options, successPrevFn, failureFn) {
			cordova.exec(successPrevFn, failureFn, 'GeolocationPlugin', 'getpreviouspositions', [options]);
		},
		isServiceRunning:function() {
			cordova.exec(isServiceRunning, function(error){}, 'GeolocationPlugin', 'isServiceRunning', []);
		}
};