'use strict';
var curSrc;
var intervalId;
const reconnectMaxTry = 5;
var reconnectWaitPeriod = 60; // in seconds

const worker = new SharedWorker('./js/socket_worker.js');

$(function () {
    var reconnectTry = 1;
    var waitPeriodIncreaseFactor = 0;

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
                    t = `$i} sec`;
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
    $('#msg-in-box').focus();

    var data = {
        'commType' : CommType.MSG,
        'sender' : $('#username').val(),
        'receivers' : $('#receiver-id').val(),
        'content' : encodeURIComponent(content.trim()),
        'receiverType' : ReceiverType.SINGLE
    };
    var params = {
        'content' : data,
        'command' : 'SEND_TEXT'
    };
    worker.port.postMessage(params);
    $('#text-in').empty();
    $('#send-btn').attr('disabled', 'disabled');
}

function sendFiles() {
    $('#msg-in-box').focus();

    var data = {
        'commType' : CommType.MSG,
        'sender' : $('#username').val(),
        'receivers' : $('#receiver-id').val(),
        'content' : '',
        'receiverType' : ReceiverType.SINGLE
    };
    var params = {
        'content' : data,
        'command' : 'SEND_BINARY'
    };
    var files = $('input[type="file"]')[0].files;
    if (isPresent(files) && files.length > 0) {
        console.log(`>>>Total files to send: ${files.length}`);
        
        for (var i = 0; i < files.length; i++) {
            console.log(`>>>Sending file- ${i + 1} of ${files.length}`);
            params.file = files[i];
            params.content.filename = files[i].name;
            worker.port.postMessage(params);
        }
    }
}

var rcvTypeTimeout;
function handleData(data) {
    switch (data.commType) {
    case CommType.ERR:
        resolveError(data);
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
    var displayUserStatus = $('#user-info').find('span.user-status');
    var displayUserLastSeen = $('#user-info').find('span.last-online');
    if (data.status === UserStatus.ONLINE) {
        userUnitStatus.replaceClass('online', 'offline');
        userUnitStatus.find('.last-online').val('');
        if (data.sender === displayUser){
            displayUserLastSeen.text('');
            displayUserStatus.replaceClass('online', 'offline');
        }
    } else {
        userUnitStatus.replaceClass('offline', 'online');
        userUnitStatus.find('.last-online').val(data.lastOnlineDateTime);
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
        var status = displayedMsg.find('i.status-icon');
        if (data.delivered || data.read)
            status.text(StatusIconText.DELIVERED);
        if (data.read)
            status.addClass('read');
    }
}

function updateNewMessage(data) {
    clearTimeout(rcvTypeTimeout);
    $('#type-stat').empty();
    $('#type-stat').hide();
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
    userUnit.find('.message-summary')
            .text(decodeURIComponent(data.content));
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
        if (data.sender === displayUser)
            receipt = AcknowledgeType.READ;
        else
            receipt = AcknowledgeType.DELIVERED;
        sendMessageReceipt(receipt, data);
    }
}

function messageAppender(messageDiv, response, caller) {
    var msgs = $('.msg');
    var ts = response.sentAt;
    ts = ts.substring(0, ts.lastIndexOf(':'));
    var dtGrp = getDateGroupDiv(ts, caller);
    var elP;
    var content = decodeURIComponent(response.content);
    var div = $(`<div class="msg">
                    <input type="hidden" class="msg-ids" value="${response.messageId}">
                 </div>`);
    var msgDiv = $('<div class="msg-div"></div>');
    var tsDiv = $('<div class="ts-div"></div>');
    var paraContent = $('<p class="text-content"></p>').html(content);
    elP = paraContent;
    var spanTime = `<span class="ts">${ts.split(' ')[1]}</span>`;
    var messageStatus = $('<i class="material-icons md-12 status-icon"></i>');
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
    div.append(msgDiv);
    msgDiv.append(paraContent);
    msgDiv.append(tsDiv);
    tsDiv.append(spanTime);
    tsDiv.append(messageStatus);

    var sender = response.sender;
    if (sender === $('#username').val()) {
        switch (response.messageStatus) {
        case AcknowledgeType.SENT:
            messageStatus.text(StatusIconText.SENT);
            break;
        case AcknowledgeType.DELIVERED:
            messageStatus.text(StatusIconText.DELIVERED);
            break;
        case AcknowledgeType.READ:
            messageStatus.addClass('read');
            messageStatus.text(StatusIconText.DELIVERED);
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
        if (caller === ChatAppenderCaller.NEW_MESSAGE && !isWindowActive)
            notifyMe($('#user-info').text());
    }

    elP.html(urlify(elP.html()));
    scrollToBottom(messageDiv.parent());
}

// function renderMessage(message) {
// var mainDiv = $(`<div class="main"></div>`);
// var msgId = $(`<input type="hidden" class="msg-id">`);
// var content = $(`<div class="msg-content"></div>`);
// var ts = $(`<div class="msg-status"></div>`);
// var img = $(`<img class="image"></img>`);
//
// msgId.val(message.messageId);
// mainDiv.append(msgId);
//
// if (isPresent(message.filename)) {
// img.attr(`src`, message.filename);
// mainDiv.append(img);
//
// img.click(function() {
// var modal = $(`#image-display-modal`);
// modal.find(`#msg-attachment-img`).attr(`src`, this.src);
// curSrc = this.src;
// modal.modal();
// });
// }
//
// if (isPresent(message.content)) {
// content.text(message.content);
// mainDiv.append(content);
// }
// if (message.sender === $(`#username`).val()) {
// ts.text(`Sent @ ` + message.sentAt);
// mainDiv.append(ts);
// }
//
// $(`#chat-content`).append(mainDiv);
//
// }

// function showThumbnails(field, thumbnailTarget) {
// thumbnailTarget.empty();
// var files = field[0].files;
// for (var i = 0; i < files.length; i++) {
// var thumb;
// if (files[i].type.startsWith('image/')) {
// thumb = $('<img></img>');
// thumb.attr('src', window.URL.createObjectURL(files[i]));
// thumb.height(60);
// thumb[0].onload = window.URL.revokeObjectURL(files[i]);
// } else {
// thumb = $('<span class="thumb"></span>');
// thumb.text(`File-$(i + 1)`);
// }
// thumbnailTarget.append(thumb);
// }
// }
