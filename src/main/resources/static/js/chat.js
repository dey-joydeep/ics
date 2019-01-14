'use strict';
var intervalId;
var reconnectTry = 1;
const reconnectMaxTry = 5;
var reconnectWaitPeriod = 60; // in seconds
var waitPeriodIncreaseFactor = 0;

const worker = new SharedWorker('./js/socket_worker.js');

$(function () {
    worker.port.onmessage = function (e) {
        switch (e.data.command) {
        case 'CONNECTED':
            reconnectTry = 1;
            $('#error-alert').hide();
            $('#ws-stat').val(1).trigger('change');
            break;
        case 'CLOSED':
            handleClosedConnection();
            break;
        case 'RECEIVED_MESSAGE':
            handleData(e.data.content);
            break;
        default:
            throw '<<<Unknown command received';
        }
    };

    worker.onerror = function (e) {
        console.log(e);
    };

    worker.port.onmessageerror = function (e) {
        console.log(e);
    };

    $('#image-display-modal').on('show.bs.modal', function (e, src) {
        displayModal(e, src);
    });
});

function openConnection() {
    $('.alert').hide();
    var params = {
        'command' : 'CONNECT',
        'content' : $('#username').val()
    };
    worker.port.postMessage(params);
}

function handleClosedConnection(){
    var jqXhr = $.ajax({
        url : 'cs',
        method : 'get',
    });

    jqXhr.done(function(data) {
        if(!data.success){
            redirect();
        }
    
        $('#ws-stat').val(0);
        console.log(`<<<Retry Count: ${reconnectTry}>>>`);
        if (reconnectTry > reconnectMaxTry) {
            clearInterval(intervalId);
            // Resetting counters
            reconnectTry = 1;
            reconnectWaitPeriod = 60;
            waitPeriodIncreaseFactor = 0;
            $('#info-msg').text(
                    'Connection failed...Try to mannualy connect again!');
            $('#error-alert').show();
        } else {
            reconnectTry++;
            reconnectWaitPeriod += waitPeriodIncreaseFactor;
            var i = reconnectWaitPeriod;
            var retry = function () {
                if (i === 0) {
                    clearInterval(intervalId);
                    console.log('>>>Trying to reconnect to websocket...');
                    openConnection();
                    return;
                } else {
                    var m, s, t;
                    if (i > 60) {
                        m = parseInt(i / 60);
                        s = i % 60;
                        t = `${m} min ${s} sec`;
                    } else {
                        t = `${i} sec`;
                    }
                    $('#info-msg').text(
                            `Connection failed...Trying to reconnect in ${t}`);
                    i--;
                }
            };
            intervalId = setInterval(retry, 1000);
            $('#error-alert').show();
            retry();
            waitPeriodIncreaseFactor += 60;
        }
    });
}

function sendMessage() {
    var content = '', temp;
    var nodes = $('#text-in')[0].childNodes;
    if (nodes.length > 0)
        $.each(nodes, function () {
            temp = $(this).text();
            content += (temp.endsWith('<br>') ? temp.replaceLast('<br>', '\n')
                    : temp + '\n');
        });
    else
        content = $('#text-in').text();
    while (content.endsWith('\r\n'))
        content = content.substring(0, content.length - 2);
    while (content.endsWith('\n')) 
        content = content.substring(0, content.length - 1);
    $('#text-in').focus();
    if(content.length === 0)
        return;
    var data = {
        'commType' : CommType.MSG,
        'sender' : $('#username').val(),
        'receivers' : $('#receiver-id').val(),
        'content' : encodeURIComponent(content.trim()),
        'receiverType' : ReceiverType.SINGLE,
        'contentType' : ContentType.TEXT
    };
    var reply = $('.msg-reply');
    if(reply.length === 1){
    	data.answerType = AnswerType.REPLY;
    	data.replyOf = {};
    	data.replyOf.messageId = reply.find('.r-msg-id').val();
    	reply.remove();
    }
    var params = {
        'content' : data,
        'command' : 'SEND_TEXT'
    };
    worker.port.postMessage(params);
    $('#text-in').empty();
    $('#send-btn').attr('disabled', 'disabled');
}

function sendFiles(fileList) {
    $('#msg-in-box').focus();

    var data = {
        'commType' : CommType.MSG,
        'sender' : $('#username').val(),
        'receivers' : $('#receiver-id').val(),
        'content' : '',
        'receiverType' : ReceiverType.SINGLE,
        'contentType' : ContentType.BINARY
    };
    var params = {
        'content' : data,
        'command' : 'SEND_BINARY'
    };
    var totalFiles = Object.keys(fileList).length;
    console.log(`>>>Total files to send: ${totalFiles}`);
    $.each(fileList, function(i, file){
        console.log(`>>>Sending file- ${i + 1} of ${totalFiles}`);
        params.content.mainFilename = file.name;
        params.file = file.data;
        worker.port.postMessage(params);
    });
}

var rcvTypeTimeout;
function handleData(data) {
    switch (data.commType) {
    case CommType.ERR:
        resolveError(data);
        $('input[type="file"]').val('');
        break;
    case CommType.NOT:
        updateUserStatus(data);
        break;
    case CommType.ACK:
        updateMessageReceipt(data);
        break;
    case CommType.MSG:
        updateNewMessage(data);
        break;
    case CommType.TYPE:
        $('#type-stat').text('typing...');
        clearTimeout(rcvTypeTimeout);
        rcvTypeTimeout = setTimeout(function () {
            $('#type-stat').empty();
        }, 3000);
        break;
    default:
        throw `<<<[ERROR] Unknown CommType:${data.commType}`;
    }
}

function sendTypeStatus() {
    var data = {
        'commType' : CommType.TYPE,
        'sender' : $('#username').val(),
        'receivers' : $('#receiver-id').val(),
        'receiverType' : ReceiverType.SINGLE
    };
    var params = {
        'content' : data,
        'command' : 'SEND_TEXT'
    };
    worker.port.postMessage(params);
}

function sendBulkDeliveredReceipt(receivers) {
    var data = {
        'commType' : CommType.ACK,
        'sender' : $('#username').val(),
        'receivers' : receivers,
        'delivered' : true
    };

    var params = {
        'command' : 'SEND_RECEIPT',
        'content' : data
    };

    worker.port.postMessage(params);
}

function sendBulkReadReceipt(messages) {
    var data = {
        'commType' : CommType.ACK,
        'sender' : $('#username').val(),
        'receivers' : $('#chat-in-display').val(),
        'read' : true
    };

    var messageIds = [];
    $.each(messages, function (i, m) {
        if (m.sender !== $('#username').val()
                && AcknowledgeType.READ !== m.messageStatus) {
            messageIds.push(m.messageId);
        }
    });

    if (messageIds.length > 0) {
        data.messageIds = messageIds;

        var params = {
            'command' : 'SEND_RECEIPT',
            'content' : data
        };
        worker.port.postMessage(params);
    }
}

function sendMessageReceipt(ackType, message) {
    if (typeof message.receivers === 'undefined')
        message.receivers = null;
    var data = {
        'commType' : CommType.ACK,
        'sender' : $('#username').val(),
        'receivers' : message.sender,
        'messageIds' : message.messageId
    };

    switch (ackType) {
    case AcknowledgeType.DELIVERED:
        data.delivered = true;
        break;
    case AcknowledgeType.READ:
        data.read = true;
        break;
    default:
        throw `<<<[ERROR] Unsupported acknowledgement:${ackType}`;
    }

    var params = {
        'command' : 'SEND_RECEIPT',
        'content' : data
    };

    worker.port.postMessage(params);
}

function resolveError(error) {
	if (error.level === ErrorLevel.FATAL)
		window.location.replace('./');
    if (error.level === ErrorLevel.LOW) {
        $('#info-msg').text(error.message);
        $('#error-alert').show();
    } else {
        var modal = $('#file-error');
        modal.find('.modal-content').text(error.message);
        modal.modal();
    }
}

function updateUserStatus(data) {
    var displayUser = $('#chat-in-display').val();
    var userUnit = $(`.user-unit:has(.friend-ids[value="${data.sender}"])`);
    var userUnitStatus = userUnit.find('span.user-status');
    var userUnitLastSeen = userUnit.find('input.last-online');
    var displayUserStatus = $('#user-info').find('span.user-status');
    var displayUserLastSeen = $('#user-info').find('span.last-online');
    if (data.status === UserStatus.ONLINE) {
        userUnitLastSeen.val('');
        userUnitStatus.replaceClass('online', 'offline');
        if (data.sender === displayUser){
            displayUserLastSeen.text('');
            displayUserStatus.replaceClass('online', 'offline');
        }
    } else {
        userUnitLastSeen.val(data.lastOnlineDateTime);
        userUnitStatus.replaceClass('offline', 'online');
        if (data.sender === displayUser){
            displayUserStatus.replaceClass('offline', 'online');
            displayUserLastSeen.text(getLastSeenFormatted(data.lastOnlineDateTime));
        }
    }
}

function updateMessageReceipt(data) {
    if (data.receivers === null || data.receivers.length !== 1)
        return;
    var displayUser = $('#chat-in-display').val();
    var userUnit = $(`.user-unit:has(.friend-ids[value="${data.sender}"])`);
    var userUnitMessageStatus = userUnit.find('i.status-icon');
    if (data.sender !== userUnit.find('.last-message-sender').val()) {
        if (data.delivered || data.read)
            userUnitMessageStatus.text(StatusIconText.DELIVERED);
        if (data.read)
            userUnitMessageStatus.addClass('read');
    }

    if (!isPresent(data.messageIds))
        return;
    for (var i = 0; i < data.messageIds.length; i++) {
        var displayedMsg = $(`#message-out>div.col>div.msg:has(
        .msg-ids[value="${data.messageIds[i]}"])`);
        var status = displayedMsg.find('.ts-div>i.status-icon');
        if (data.delivered || data.read) {
            status.text(StatusIconText.DELIVERED);
            displayedMsg.find('.ts-div>.d-dt').val(data.deliveredAt);
	        if (data.read) {
            status.addClass('read');
	            displayedMsg.find('.ts-div>.r-dt').val(data.readAt);
	        }
        }
    }
}

function updateNewMessage(data) {
    clearTimeout(rcvTypeTimeout);
    $('#type-stat').empty();
    var caller, userUnit;
    var isSelf = (data.sender === $('#username').val());
    if (isSelf) {
        caller = ChatAppenderCaller.SEND_BTN;
        userUnit = $(`.user-unit:has(.friend-ids[value="${data.receivers[0]}"])`);
        userUnit.find('.status-icon').text(StatusIconText.SENT);
    } else {
        caller = ChatAppenderCaller.NEW_MESSAGE;
        userUnit = $(`.user-unit:has(.friend-ids[value="${data.sender}"])`);
        userUnit.find('.status-icon').empty();
    }
    userUnit.find('.d-none').removeClass('d-none');
    var sentAt = data.sentAt.substring(0, data.sentAt.lastIndexOf(':'));
    userUnit.find('.message-time').text(sentAt);
    var content = getMessageSummaryContent(data, isSelf);
    userUnit.find('.message-summary').html(content);
    if(isSelf || data.sender === $('#chat-in-display').val())
        messageAppender($('#message-out>div.col'), data, caller);

    if (!isSelf) {
        var displayUser = $('#chat-in-display').val();
        if(data.sender !== displayUser){
            userUnit.find('.message-summary').addClass('unread');
            var unreadSpan = userUnit.find('.unread-count');
            var unreadCount = unreadSpan.text();
            if(unreadCount === '' || isNaN(unreadCount))
                unreadCount = 1;
            else
                unreadCount = parseInt(unreadCount) + 1;
            unreadSpan.text(unreadCount);
        }
        
        var receipt;
        if (!document.hidden && data.sender === displayUser)
            receipt = AcknowledgeType.READ;
        else
            receipt = AcknowledgeType.DELIVERED;
        sendMessageReceipt(receipt, data);
    }
    
    userUnit.parent().prepend(userUnit);
}

function messageAppender(messageDiv, response, caller) {
	var elP, fileContent;
	var div = $(MESSAGE_PROTO);
    var ts = response.sentAt;
    ts = ts.substring(0, ts.lastIndexOf(':'));
    var dtGrp = getDateGroupDiv(ts, caller);
    var content = decodeURIComponent(response.content);
    div.find('.msg-ids').val(response.messageId);
    var msgDiv = div.find('.msg-div');
    var tsDiv = div.find('.ts-div');
    var paraContent = div.find('.text-content');
    var fileDiv = div.find('.attach-content');
    
    if (caller === ChatAppenderCaller.USER_SELECT) {
        var firstDtGrp = $('.dt-grp:first');
        if (dtGrp !== null) {
            messageDiv.prepend(div);
            messageDiv.prepend(dtGrp);
        } else {
            div.insertAfter(firstDtGrp);
        }
    } else if (caller === ChatAppenderCaller.RECONNECT) {
    } else {
        if (dtGrp !== null)
            messageDiv.append(dtGrp);
        messageDiv.append(div);
    }
    
    var reply = response.replyOf;
    if(isPresent(reply)){
        var rDiv = $(MESSAGE_REPLY_PREV_PROTO);
        div.find('.msg-div').prepend(rDiv);
        var senderName = 'You';
        if(reply.sender !== $('#username').val()) {
            senderName = $(`.user-unit:has(.friend-ids[value="${reply.receiver}"])`).find('.fullname').text();
            senderName = senderName.split(' ')[0];
        }
        rDiv.find('.r-msg-id').val(reply.messageId);
        rDiv.find('.reply-origin-name').text(senderName);
        if(isPresent(reply.content))
            rDiv.find('.text-content').text(decodeURIComponent(reply.content));
        
        var hasFile = false;
        if(reply.contentType === ContentType.BINARY) {
            hasFile = true;
            rDiv.addClass('image-only');
        }
        
        if(reply.contentType === ContentType.MIXED) {
            hasFile = true;
            rDiv.addClass('text-image');
        }
        if(hasFile) {
            prepareBinaryDisplay(response);
        }
    }
    
    if(response.contentType === ContentType.TEXT || response.contentType === ContentType.MIXED){
    	paraContent.html(content);
    	elP = paraContent;
    }
    
    var dLink, isViewable = false;
    var hasBinary = (response.contentType === ContentType.BINARY || response.contentType === ContentType.MIXED);
    if(hasBinary) {
    	var data = prepareBinaryDisplay(response);
    	dLink = data.dLink
    	fileContent = data.fileContent;
    	isViewable = data.isViewable;
    	
    	if(response.mediaType !== MediaType.AUDIO)
    	    fileContent.addClass('center');
    	
    	if(isPresent(dLink))
    		if(isViewable)
    			fileDiv.append(fileContent).append(dLink.attr('title', 'Click to download'));
    		else
    		fileDiv.append(dLink.prepend(fileContent).attr('title', 'Click to download'));
    	else 
    		fileDiv.append(fileContent);
    	
    	var srcPath = div.find('.attach-content>.attach-source');
    	var srcName = div.find('.attach-content>.attach-filename');
    	var srcType = div.find('.attach-content>.attach-mediatype');
    	srcPath.val(response.modFilename);
    	srcName.val(response.mainFilename);
    	srcType.val(response.mediaType);
		if(isViewable) {
    		isViewable = false;
			srcPath.addClass('viewable');
			srcName.addClass('viewable');
			srcType.addClass('viewable');
			// Reset viewable
	    	fileContent.click(function(){
	    		handleFileClick(this);
	    	});
    	}
	}

    div.find('.ts-div>.ts').text(ts.split(' ')[1]);
    div.find('.ts-div>.s-dt').val(response.sentAt);
    
    var messageStatus = div.find('.ts-div>.status-icon');
    if (response.sender === $('#username').val()) {
        switch (response.messageStatus) {
        case AcknowledgeType.SENT:
            messageStatus.text(StatusIconText.SENT);
            break;
        case AcknowledgeType.DELIVERED:
            messageStatus.text(StatusIconText.DELIVERED);
            div.find('.ts-div>.d-dt').val(response.deliveredAt);
            break;
        case AcknowledgeType.READ:
            messageStatus.addClass('read');
            messageStatus.text(StatusIconText.DELIVERED);
            div.find('.ts-div>.r-dt').val(response.readAt);
            break;
        default:
            messageStatus.text(StatusIconText.SENT);
            break;
        }

        div.addClass('float-right');
        msgDiv.addClass('msg-self');
    } else {
        div.addClass('float-left');
        msgDiv.addClass('msg-other');
        if (caller === ChatAppenderCaller.NEW_MESSAGE && document.hidden){
            console.log('Sending desktop notification.');
            notifyMe($('#user-info').text());
        }
    }

    if(isPresent(elP))
    	elP.html(urlify(elP.html()));
    if(caller !== ChatAppenderCaller.USER_SELECT)
        if(hasBinary)
            fileContent.on('load', function(){
                scrollToBottom(messageDiv.parent());                
            });
        else
            scrollToBottom(messageDiv.parent());
}

function prepareBinaryDisplay(response) {
    var dLink, fileContent, isViewable = false;
    var mediaType = isPresent(response.replyOf) ? response.replyOf.mediaType : response.mediaType;
    switch(mediaType) {
    case MediaType.IMAGE:
        isViewable = true;
        if(isPresent(response.replyOf)){
            var img = $(`.msg:has(.msg-ids[value="${response.messageId}"])>.msg-div>.msg-reply-prev>img`);
            img.attr('src', response.replyOf.modFilename);
        } else {
            fileContent = $('<img class="attach-img"></img>');
            fileContent.attr('src', response.modFilename);
        }
        break;
    case MediaType.AUDIO:
        if(isPresent(response.replyOf)) {
            fileContent = $('<img class="attach-icon" src="./images/gif/spinner.gif"></img>');
            fileType.getThumbnail('audio').then(function(icon){
                var img = $(`.msg:has(.msg-ids[value="${response.messageId}"])>.msg-div>.msg-reply-prev>img`);
                img.attr('src', icon);
            });
        } else {
            fileContent = $('<audio />', {
                src: response.modFilename,
                controls: true
            });
        }
        break;
    case MediaType.VIDEO:
        isViewable = true;
        fileContent = $('<img class="attach-img" src="./images/gif/spinner.gif"></img>');
        var video = isPresent(response.replyOf) ? response.replyOf.modFilename : response.modFilename;
        videoToImage(video).then(function(src){
            var tgt = $(`.msg:has(.msg-ids[value="${response.messageId}"])>.msg-div`);
            if(!isPresent(response.replyOf))
                tgt.find('.attach-content>img').attr('src', src);
            else
                tgt.find('.msg-reply-prev>img').attr('src', src);
        });
        break;
    case MediaType.TEXT:
        isViewable = true;
        var text = isPresent(response.replyOf) ? response.replyOf.modFilename : response.modFilename;
        fileContent = $('<img class="attach-img" src="./images/gif/spinner.gif"></img>');
        var jqXhr = $.ajax({
            url : text,
            method : 'get',
        });

        jqXhr.done(function (data) {
            var src = textToImage(data, 640, 320);
            var tgt = $(`.msg:has(.msg-ids[value="${response.messageId}"])>.msg-div`);
            if(!isPresent(response.replyOf))
                tgt.find('.attach-content>img').attr('src', src);
            else
                tgt.find('.msg-reply-prev>img').attr('src', src);
        });
        break;
    case MediaType.PDF:
        isViewable = true;
    case MediaType.DOCUMENT:
        var doc = isPresent(response.replyOf) ? response.replyOf.mainFilename : response.mainFilename;
        fileContent = $('<img class="attach-icon" src="./images/gif/spinner.gif"></img>');
        dLink = $(`<a class="msg-download-link" href="${response.modFilename}" download=${response.mainFilename}>
                       ${response.mainFilename}
                   </a>`);
        var ext = doc.substring(doc.lastIndexOf('.') + 1);
        fileType.getThumbnail(ext).then(function(icon){
            var tgt = $(`.msg:has(.msg-ids[value="${response.messageId}"])>.msg-div`);
            if(!isPresent(response.replyOf))
                tgt.find('.attach-content img').attr('src', icon);
            else
                tgt.find('.msg-reply-prev>img').attr('src', icon);
        });
        break;
    }
    
    return {
            fileContent: fileContent,
            isViewable : isViewable,
            dLink : dLink
    };
}

function handleFileClick(thisFile){
	var viewTarget = [];
	var activeSrc = $(thisFile).parent().find('.attach-source.viewable').val();
	var viewableSrc = $('.attach-source.viewable');
	var viewableName = $('.attach-filename.viewable');
	var viewableType = $('.attach-mediatype.viewable');
	// for debug
	if(!((viewableSrc.length === viewableName.length) && (viewableSrc.length === viewableType.length))){
		console.log(`Source(${viewableSrc.length}),  filename(${viewableName.length}) media type(${viewableType.length}) length mismatch`);
		return false;
	}
	$.each(viewableSrc, function(i, target){
		viewTarget[i] = {
				src : target.value,
				name : viewableName[i].value,
				type : viewableType[i].value,
				isActive : (target.value === activeSrc)
		};
	});
	
	var viewer = new FileViewer($('#m-view-pane'), viewTarget); 
	viewer.fileView();
}