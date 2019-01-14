/**
 * Copyright Joydeep Dey 2018-2019
 * 
 * Source:
 * https://github.com/josefrichter/resize/blob/master/public/preprocess.js
 * Modified as per need. Improvement may require later.
 */
"use strict";
const IMAGE_MAX_WIDTH = 1024;
const IMAGE_MAX_HEIGHT = 768;
function getArrayBuffer(file){
	return new Promise((resolve, reject) => {
		// read the files
		const reader = new FileReader();
		reader.readAsArrayBuffer(file);
		reader.onload = () => resolve(reader.result);
		reader.onerror = error => reject(error);
	});
}
function getImage(blobURL){
	return new Promise((resolve, reject) => {
		// helper Image object
		const image = new Image();
		image.src = blobURL;
		image.onload = ()=> resolve(image);
		image.onerror = error => reject(error);
	});
}

function getCompressFile(file) {
	return new Promise((resolve, reject) => {
	    var result = getArrayBuffer(file);
		// blob stuff
		result.then(function(array){
			var blob = new Blob([ array ]); // create blob...
			window.URL = window.URL || window.webkitURL;
			var blobURL = window.URL.createObjectURL(blob); // and get it's URL
			var img = getImage(blobURL);
			img.then(function(image){
				resolve(resizeMe(image)); // send it to canvas
			});
		});
	});
}

// === RESIZE ====
function resizeMe(img) {
	var width = img.width;
	var height = img.height;

	// calculate the width and height, constraining the proportions
	if (width > height) {
		if (width > IMAGE_MAX_WIDTH) {
			// height *= max_width / width;
			height = Math.round(height *= IMAGE_MAX_WIDTH / width);
			width = IMAGE_MAX_WIDTH;
		}
	} else {
		if (height > IMAGE_MAX_HEIGHT) {
			// width *= max_height / height;
			width = Math.round(width *= IMAGE_MAX_HEIGHT / height);
			height = IMAGE_MAX_HEIGHT;
		}
	}

	// create the canvas and draw the image data into it with the new
	// width/height
	var canvas = $(`<canvas id="myCanvas" width="${width}" height="${height}"></canvas>`)[0];
	var ctx = canvas.getContext("2d");
	ctx.drawImage(img, 0, 0, width, height);
	// get the data from canvas as /70% JPG
	return canvas.toDataURL('image/jpeg', 0.7); 
}

function getBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = error => reject(error);
    });
}

function dataURItoBlobURL(dataURI) {
    // convert base64 to raw binary data held in a string
    var byteString = atob(dataURI.split(',')[1]);

    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];

    // write the bytes of the string to an ArrayBuffer
    var arrayBuffer = new ArrayBuffer(byteString.length);
    var _ia = new Uint8Array(arrayBuffer);
    for (var i = 0; i < byteString.length; i++) {
        _ia[i] = byteString.charCodeAt(i);
    }

    var dataView = new DataView(arrayBuffer);
    var blob = new Blob([dataView], { type: mimeString });
    return window.URL.createObjectURL(blob);
}

function videoToImage(url, videoType, width, height) {
    return new Promise((resolve, reject) => {
        var video = $('<video />', {
            src: url,
            type: videoType,
            controls: true
        })[0];
        video.onloadeddata = function(data){
            var canvas = document.createElement("canvas");
            if(typeof width === 'undefined')
            	width = 640;
            if(typeof height === 'undefined')
            	height = 320;
            canvas.width = width;
            canvas.height = height;
            var ctx = canvas.getContext('2d');
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
            var fontSize = 35;
            var duartion = durationToHhMmSs(video.duration);
            ctx.font = `${fontSize}px Arial`;    
            ctx.textAlign = "end";      
            if(!isPresent(fileType))
            	fileType = new FileType();
            fileType.getThumbnail('play').then(function(icon){
                var image = new Image();
                image.onload = function() {
                    var x = parseInt((canvas.width / 2) - (image.width / 2));
                    var y = parseInt((canvas.height / 2) - (image.height / 2));
                    ctx.drawImage(image, x, y, image.width, image.height);
                    ctx.fillStyle = "#fff";
                    ctx.fillText(duartion, width-fontSize, height-fontSize);
                    resolve(canvas.toDataURL());
                };
                image.src = icon;
            });
        };
        video.onerror = error => reject(error);
        video.load();
    });
}

function textToImage (text, canvasWidth, canvasHeight) {
    var canvas = $(`<canvas width="${canvasWidth}" height="${canvasHeight}">`)[0];
    var tCtx = canvas.getContext('2d');
    var lines = text.split(/\n|\r\n/);
    var fontSize = 20;
    tCtx.font = `${fontSize}px Georgia`;
    tCtx.fillStyle = '#fff';
    tCtx.fillRect(0, 0, canvas.width, canvas.height);
    var lineCount = 1;
    tCtx.fillStyle = '#000';
    lines.forEach(function(line){
        var width = tCtx.measureText(line).width;
        if(width > canvasWidth){
            var newText = line;
            while(newText.length > 0) {
                var endIdx = newText.length;
                var targetText = newText;
                do {
                    targetText = targetText.substring(0, endIdx--);
                    width = parseInt(tCtx.measureText(targetText).width);
                } while(width > canvasWidth);
                tCtx.fillText(targetText, 0, fontSize * (lineCount++));
                newText = newText.replace(targetText, '');
            }
        }else {
            tCtx.fillText(line, 0, fontSize * (lineCount++));
        }
    });
    return canvas.toDataURL('image/jpeg', 0.7);
}