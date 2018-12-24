var ws;
var port;
function isPresent(data) {
	return (typeof (data) !== 'undefined' && data !== null);
}

/**
 * Connect to websocket URL using the username in parameter or share the
 * existing websocket instance, if same user is logged in from another browser
 * context (i.e. Another tab or page) for same domain.
 * 
 * @param username
 */
function connect(username) {
	if(!isPresent(username))
		throw '<<<Username is required';
	
	var params = {};
	// check the status of current websocket session
	if (!isPresent(ws) || ws.readyState === ws.CLOSED || username !== ws.id) {
		ws = new WebSocket(self.location.origin.replace('http','ws') 
				+'/socket/conn/?username='+username);
		ws.onconnect = function() {
			params.command = 'CONNECTED';
			port.postMessage(params);
		};
		ws.id = username;
		ws.onclose = function() {
			params.command = 'CLOSED';
			port.postMessage(params);
		};
		ws.onerror = function(e) {
			console.log(e);
		};
		ws.onmessage = function(e) {
			params.command = 'RECEIVED_MESSAGE';
			params.content = JSON.parse(e.data);
			port.postMessage(params);
		};
		console.log('>>>New websocket instance is created.');
// checkAndNotifyConnect(params, 5);
	} else {
		console.log('>>>Sharing exsiting websocket instance.');
	}
	params.command = 'CONNECTED';
	port.postMessage(params);
}

// function checkAndNotifyConnect(params, timeout){
// setTimeout(
// function () {
// if (ws.readyState === ws.CLOSED) {
// return;
// } else if (ws.readyState === ws.OPEN) {
// console.log(">>>Socket connetion established.")
// port.postMessage(params)
// return;
//
// } else {
// console.log("<<<Wait for connection...>>>")
// // Increase timeout period for by multiply of 5.
// // if waited for more than 1 second, stop checking the state.
// // If a closed state is detected, re-opening the connection will
// // be attempted.
// if(timeout > 1000){
// console.log('>>>Stopping detection ready state change to OPEN(1). Will
// attempt to Re-connect.')
// return;
// }
// console.log('Waiting for ' + (timeout*5) + 'ms before change detection.')
// checkAndNotifyConnect(params,timeout*5);
// }
// }, timeout); // wait max 5ms for the connection...
// }

/**
 * Transfer the plain text content.
 * 
 * @param{JSON} data A JSON object containing the data
 */
function sendText(data) {
	ws.send(JSON.stringify(data));
}

/**
 * Transfer the plain text and file to the server. As this method will send an
 * instance of ArrayBuffer, the plain text is also wrapped in an ArrayBuffer
 * with the length of the plain text as meta data in first byte of the
 * transferred data. &lt;data length&gt;-&lt;data>-&lt;file&gt;
 * 
 * @param data
 *            Plain text in JSON object
 * @param file
 *            the file object
 */
function sendBinary(data, file) {
	// TextEncoder support is required to verify, especially in IE11/Edge
	// Convert the data into string and then to an ArrayBuffer.
	var body = new TextEncoder().encode(JSON.stringify(data));
	// One byte meta info to store body length (Required during decoding the
	// byte array in server)
	var meta = new Uint8Array(1);
	meta[0] = body.length;

	// ws.binaryType = "blob";
	var reader = new FileReader();
	reader.onload = function(e) {
		// Unify the file content into an Uint8Array for concatenation
		var fileContent = new Uint8Array(e.target.result);
		// Concatenate and send the data
		ws.send(concatBuffers(meta, body, fileContent));
	};
	reader.readAsArrayBuffer(file);
}

/**
 * Concatenate the ArrayBuffers in the order they are passed. The buffers should
 * be a type of Uint8Array.
 * 
 * @param ...arrays
 *            ArrayBuffers
 */
function concatBuffers(...arrays) {
    let totalLength = 0;
    for (const arr of arrays) {
        totalLength += arr.length;
    }
    const result = new Uint8Array(new ArrayBuffer(totalLength));
    let offset = 0;
    for (const arr of arrays) {
        result.set(arr, offset);
        offset += arr.byteLength;
    }
    return result;
}

self.onconnect= function(e) {
	console.log(e)
// if(!isPresent(port))
	port = e.ports[0];
	port.onmessage = function(e) {
		var content = e.data.content;
		if(typeof content === 'object') {
			if(typeof content.receivers === 'string')
				content.receivers = [content.receivers];
			if(typeof content.messageIds === 'number')
				content.messageIds = [content.messageIds];
		}
		switch (e.data.command) {
		case 'CONNECT':
			console.log('>>>Trying to obtain websocket instance.');
			connect(content);
			break;
		case 'SEND_TEXT':
		case 'SEND_RECEIPT':
			sendText(content);
			break;
		case 'SEND_BINARY':
			sendBinary(content, e.data.file);
			break;
		case 'CLOSE':
			break;
		default:
			throw '<<<Unknown command received';
		}
	};
};