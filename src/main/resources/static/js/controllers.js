'use strict';

/* Controllers */

angular.module('springChat.controllers', ['toaster', 'ngFileUpload'])
    .controller('ChatController', ['$scope', '$location', '$interval', 'toaster', 'ChatSocket', '$http', 'Upload',
        function ($scope, $location, $interval, toaster, chatSocket, $http, Upload) {

            var typing = undefined;
            $scope.username = '';
            $scope.sendTo = '';
            $scope.participants = [];
            $scope.messages = [];
            $scope.newMessage = '';
            $scope.history = [];
            $scope.options = {
                weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
                hour12: false, hour: 'numeric', minute: 'numeric', second: 'numeric'
            };
            $scope.uploadFileType = "";
            $scope.loadingPercent = 0;

            $scope.navCollapse = function () {
                $("#navbar-collapse-01").removeClass("in open");
            }

            $scope.sendMessage = function () {
                if ($scope.sendTo == null || $scope.sendTo == "")
                    return;
                var destination = "/app/chat.private." + $scope.sendTo;
                $scope.messages.push({
                    date: new Date(),
                    message: $scope.newMessage,
                    username: $scope.username,
                    to: $scope.sendTo
                });
                $("html, body").animate({scrollTop: $(document).height()}, 1000);

                chatSocket.send(destination, {}, JSON.stringify({
                    date: new Date().toUTCString(),
                    message: $scope.newMessage,
                    to: $scope.sendTo
                }));
                $scope.newMessage = '';
            };

            $scope.startTyping = function () {
                // Don't send notification if we are still typing or we are typing a private message
                if (angular.isDefined(typing)) return;
                typing = $interval(function () {
                    $scope.stopTyping();
                }, 500);

                chatSocket.send("/app/chat.typing", {}, JSON.stringify({
                    from: $scope.username,
                    to: $scope.sendTo,
                    typing: true
                }));
            };

            $scope.stopTyping = function () {
                if (angular.isDefined(typing)) {
                    $interval.cancel(typing);
                    typing = undefined;

                    chatSocket.send("/app/chat.typing", {}, JSON.stringify({
                        from: $scope.username,
                        to: $scope.sendTo,
                        typing: false
                    }));
                }
            };

            $scope.privateSending = function (username) {
                $scope.sendTo = (username != $scope.sendTo) ? username : $scope.participants[0].username;
                $scope.messages = [];
                // request chat history
                chatSocket.send("/app/chat.history." + $scope.sendTo);
            };

            $scope.filterMessages = function (message) {
                var list = [$scope.username, $scope.sendTo];
                return (list.indexOf(message.username) !== -1
                && list.indexOf(message.to) !== -1);
            };

            $scope.sendFile = function () {
                if ($scope.sendTo == null || $scope.sendTo == "")
                    return;
                var $this = document.getElementById('file-input');
                $scope.uploadFileType = $this.files[0].type.split('/')[0];
                var size = $this.files[0].size / (1024 * 1024);
                if ($scope.uploadFileType != "image" && $scope.uploadFileType != "video" && $scope.uploadFileType != "audio") {
                    toaster.pop('error', 'Error', 'Select only image/video/audio files');
                    $scope.$apply();
                    console.log("select only image/video/audio files...")
                    return;
                }
                if (size > 2048) {
                    toaster.pop('error', 'Error', 'Select only files < 2 GB');
                    $scope.$apply();
                    console.log("Select only files <  GB...")
                    return;
                }

                // upload on file select or drop
                Upload.upload({
                    method: 'POST',
                    url: '/chat/upload',
                    data: {file: $this.files[0], name: $scope.sendTo}
                }).then($scope.uploadSuccess, $scope.uploadError, $scope.uploadProgress);
            }

            $scope.uploadProgress = function (event) {
                // evt.config.data.file.name
                $scope.loadingPercent = parseInt(100.0 * event.loaded / event.total);
            }

            $scope.uploadSuccess = function (response) {
                console.log(response);
                response = response["data"]["id"];
                // success callback
                var destination = "/app/chat.private." + $scope.sendTo;
                if ($scope.uploadFileType == "image") {
                    // image
                    chatSocket.send(destination, {}, JSON.stringify({
                        date: new Date().toUTCString(),
                        image: true,
                        message: response,
                        to: $scope.sendTo
                    }));
                    $scope.messages.push({
                        date: new Date(),
                        image: true,
                        message: response,
                        username: $scope.username,
                        to: $scope.sendTo
                    });
                    // $scope.$apply();
                } else if ($scope.uploadFileType == "video") {
                    // video
                    chatSocket.send(destination, {}, JSON.stringify({
                        date: new Date().toUTCString(),
                        video: true,
                        message: response,
                        to: $scope.sendTo
                    }));
                    $scope.messages.push({
                        date: new Date(),
                        video: true,
                        message: response,
                        username: $scope.username,
                        to: $scope.sendTo
                    });
                    // $scope.$apply();
                } else {
                    // audio
                    chatSocket.send(destination, {}, JSON.stringify({
                        date: new Date().toUTCString(),
                        audio: true,
                        message: response,
                        to: $scope.sendTo
                    }));
                    $scope.messages.push({
                        date: new Date(),
                        audio: true,
                        message: response,
                        username: $scope.username,
                        to: $scope.sendTo
                    });
                }
                $("html, body").animate({scrollTop: $(document).height()}, 1000);
            }

            $scope.uploadError = function (response) {
                console.log('Error status: ' + response.status);
            }

            var initStompClient = function () {
                chatSocket.init('/stomp');

                chatSocket.connect(function (frame) {

                    $scope.username = frame.headers['user-name'];

                    chatSocket.subscribe("/app/chat.participants", function (message) {
                        $scope.participants = JSON.parse(message.body);
                        for (var index in $scope.participants) {
                            if ($scope.participants[index].username == $scope.username) {
                                $scope.participants.splice(index, 1);
                            }
                        }
                        if ($scope.sendTo == '' && $scope.participants.length > 0) {
                            $scope.sendTo = $scope.participants[0].username;
                            // request chat history: init
                            chatSocket.send("/app/chat.history." + $scope.sendTo);
                        }
                    });

                    chatSocket.subscribe("/topic/chat.login", function (message) {
                        $scope.participants.push({username: JSON.parse(message.body).username, typing: false});
                        if ($scope.sendTo == '' && $scope.participants.length > 0) {
                            $scope.sendTo = $scope.participants[0].username;
                            // request chat history: init
                            chatSocket.send("/app/chat.history." + $scope.sendTo);
                        }
                    });

                    chatSocket.subscribe("/topic/chat.logout", function (message) {
                        var username = JSON.parse(message.body).username;
                        for (var index in $scope.participants) {
                            if ($scope.participants[index].username == username) {
                                $scope.participants.splice(index, 1);
                            }
                        }
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/chat.typing", function (message) {
                        var parsed = JSON.parse(message.body)
                        for (var index in $scope.participants) {
                            var participant = $scope.participants[index];
                            if (participant.username == parsed.from) {
                                $scope.participants[index].typing = parsed.typing;
                            }
                        }
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/chat.message", function (message) {
                        var parsed = JSON.parse(message.body)
                        parsed.date = new Date(parseInt(parsed.date));
                        if ($scope.filterMessages(parsed))
                            $scope.messages.push(parsed);
                        $("html, body").animate({scrollTop: $(document).height()}, 1000);
                    });

                    // subscribe for receiving chat history
                    chatSocket.subscribe("/user/exchange/amq.direct/chat.history", function (message) {
                        var parsed = JSON.parse(message.body);
                        for (var index in parsed) {
                            parsed[index].message.date = new Date(parseInt(parsed[index].message.date));
                        }
                        $scope.history = parsed;
                        $("html, body").animate({scrollTop: $(document).height()}, 1000);
                    });

                    chatSocket.subscribe("/user/exchange/amq.direct/errors", function (message) {
                        toaster.pop('error', "Error", message.body);
                    });

                }, function (error) {
                    toaster.pop('error', 'Error', 'Connection error ' + error);

                });
            };

            initStompClient();
            $scope.hexToBase64 = function (str) {
                return btoa(String.fromCharCode.apply(null, str.replace(/\r|\n/g, "")
                    .replace(/([\da-fA-F]{2}) ?/g, "0x$1 ").replace(/ +$/, "").split(" ")));
            }
        }]);
	
