'use strict';

/* Services */

angular.module('springChat.services', [])
	.factory('ChatSocket', ['$rootScope', function($rootScope) {
			var stompClient;

			var wrappedSocket = {

					init: function(url) {
						stompClient = Stomp.over(new SockJS(url));
					},
					connect: function(successCallback, errorCallback) {

						stompClient.connect({}, function(frame) {
							$rootScope.$apply(function() {
								successCallback(frame);
							});
						}, function(error) {
							$rootScope.$apply(function(){
								errorCallback(error);
							});
				        });
					},
					subscribe : function(destination, callback) {
						stompClient.subscribe(destination, function(message) {
							  $rootScope.$apply(function(){
								  callback(message);
							  });
				          });
					},
					send: function(destination, headers, object) {
						stompClient.send(destination, headers, object);
					}
			}

			return wrappedSocket;

	}]);

springChat.service('LoadingInterceptor', ['$q', '$rootScope', '$log', function ($q, $rootScope, $log) {

		var xhrCreations = 0;
		var xhrResolutions = 0;

		function isLoading() {
			return xhrResolutions < xhrCreations;
		}

		function updateStatus() {
			$rootScope.loading = isLoading();
		}

		return {
			request: function (config) {
				xhrCreations++;
				updateStatus();
				return config;
			},
			requestError: function (rejection) {
				xhrResolutions++;
				updateStatus();
				$log.error('Request error:', rejection);
				return $q.reject(rejection);
			},
			response: function (response) {
				xhrResolutions++;
				updateStatus();
				return response;
			},
			responseError: function (rejection) {
				xhrResolutions++;
				updateStatus();
				$log.error('Response error:', rejection);
				return $q.reject(rejection);
			}
		};
	}]);

springChat.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('LoadingInterceptor');
}]);