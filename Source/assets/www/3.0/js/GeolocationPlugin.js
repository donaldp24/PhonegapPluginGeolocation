
var GeolocationPlugin = {
		
		startservice:function(options) {
			//alert("startservice");
			var json_options = ConvertToJSONArray(options);
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'startservice', [json_options]);
		},		
		stopservice:function(options) {
			//alert("stopservice");			
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'stopservice', [options]);
		},		
		getcurrentpositions:function(options, successCurFn, failureFn) {
			//alert("getcurrentpositions");
			var json_options = ConvertToJSONArray(options);
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'getcurrentpositions', [json_options]);
		},		
		getpreviouspositions:function(options, successPrevFn, failureFn) {
			//alert("getpreviouspositions");
			var json_options = ConvertToJSONArray(options);
			cordova.exec(function(data){}, function(error){}, 'GeolocationPlugin', 'getpreviouspositions', [json_options]);
		}
};



function ConvertToJSONArray(options) {
	
	var result = [];
	result.push(options);
	
	return JSON.stringify(result);
}
