/* Directives */

angular.module('springChat.directives', [])
	.directive('printMessage', function () {
	    return {
	    	restrict: 'E',
	        templateUrl: 'message.html'

	    };

    }).directive('printHistory', function () {
		return {
			restrict: 'E',
			templateUrl: 'history.html'

		}
	}).directive('repeatDone', function() {
        return function(scope, element, attrs) {
            if (scope.$last){
                setTimeout(function(){$('[data-toggle="tooltip"]').tooltip({placement: 'right'});},0);
            }
        };
    }).directive('onFileChange', function() {
		return {
			restrict: 'A',
			link: function (scope, element, attrs) {
				element.bind('change', scope.$eval(attrs.onFileChange));
			}
		};
	}).directive('onVideoLoad', function () {
		return { link: function (scope, elm, attrs) {
            elm[0].src = attrs.onVideoLoad;
			elm[0].load();
        } }
	}).directive('onAudioLoad', function () {
	return { link: function (scope, elm, attrs) {
        var audio = elm[0];
		audio.src = attrs.onAudioLoad;

        // audio visualization
        var context = new (window.AudioContext || window.webkitAudioContext)();
        var analyser = context.createAnalyser(); // AnalyserNode method
        analyser.fftSize = 2048;
        var canvas = audio.previousElementSibling;
        var ctx = canvas.getContext('2d');
        // Re-route audio playback into the processing graph of the AudioContext
        //createMediaStreamSource for streaming source
        var source = context.createMediaElementSource(audio);
        source.connect(analyser);
        analyser.connect(context.destination);

        // animation loop
        scope.requestAnimationFrame = function() {
            window.requestAnimationFrame(scope.requestAnimationFrame);
            var bufferLength = analyser.frequencyBinCount;
            var fbc_array = new Uint8Array(bufferLength);
            analyser.getByteTimeDomainData(fbc_array);
            ctx.fillStyle = '#fff';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.lineWidth = 1;
            ctx.strokeStyle = 'rgb(0, 0, 0)';
            ctx.beginPath();
            var sliceWidth = canvas.width * 1.0 / bufferLength;
            var x = 0;
            for(var i = 0; i < bufferLength; i++) {
                var v = fbc_array[i] / 128.0;
                var y = v * canvas.height/2;
                if(i === 0)
                    ctx.moveTo(x, y);
                else
                    ctx.lineTo(x, y);
                x += sliceWidth;
            }
            ctx.lineTo(canvas.width, canvas.height/2);
            ctx.stroke();
        }
        scope.requestAnimationFrame();
    } }
});
