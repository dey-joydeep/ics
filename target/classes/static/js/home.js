const messageCache = {};
$(function() {
	// $(window).bind('beforeunload', function(e) {
	// // save info somewhere
	// console.log(e);
	// return 'are you sure you want to leave?';
	// });
	$('#logout').click(function() {
		logout();
	});
	$('#username').change(function() {
		openConnection();
	});
	$('#ws-stat').change(function() {
		if (this.value === "1")
			loadUsers();
	});

	loadUsername();

	$('#search-id').on('input', function() {
		searchUser();
	});

	$('.av-in').click(function() {
	    $('#av-file').attr('accept',MEDIA_AV);
		$('#av-file').click();
	});
	$('.doc-in').click(function() {
	    $('#doc-file').attr('accept',MEDIA_DOC);
		$('#doc-file').click();
	});

	$('#text-in').on('input', function() {
		controlChatContent($(this));
	});

	adjustPanelDisplay();
	$(window).resize(function() {
		adjustPanelDisplay();
	});

	$('#back-to-friend-list').click(function() {
		$('.chat-sub-panel').hide();
		$('#user-panel').show();
	});

	$('#message-out').scroll(function() {
		// TODO: Implement scroll to bottom code.
	});
	
	$('#av-file').change(function() {
	    showPreview(this);
	});

	$('#file-link').click(function() {
		$('#filename').click();
	});

	$('#send-btn').click(function(e) {
		sendMessage();
		var currentUser = $('#chat-in-display').val();
        if (messageCache.hasOwnProperty(currentUser))
            delete messageCache[currentUser];
	});
	
	$('#av-send-btn').click(function(){
	    sendFiles();
	});

	$('#filename').change(function() {
		if (this.files.length > 0)
			$('#send-btn').removeAttr('disabled');
		else
			$('#send-btn').attr('disabled', 'disabled');
	});

	$("#text-in").bind("paste", function(e) {
	    e.preventDefault();
	    // get text representation of clip board
	    var text = (e.originalEvent || e).clipboardData.getData('text/plain');
	    if(text.trim()!==''){
    	    // insert text manually
    	    document.execCommand("insertHTML", false, text);
    	    $('#text-in').keypress();
	    }
// navigator.clipboard.readText()
// .then(text => {
// if(text.trim()!=='')
// $('#text-in').keypress();
// });
	});
	$('#text-in').keypress(function(e) {
		if ($(this).text().trim().length > 0)
			$('#send-btn').removeAttr('disabled');
		else
			$('#send-btn').attr('disabled', 'disabled');
		if (e.keyCode !== 13){
			sendTypeStatus();
		} else{
			if(!e.shiftKey && $('#enter-send-check').is(':checked')){
				$('#send-btn').click();
				return false;
			}
		}
	});
});

function logout() {
	var jqXhr = $.ajax({
		url : 'logout',
		method : 'get',
	});

	jqXhr.done(function(result) {
		if (result.success)
			window.location.replace('./');
	});
}

function adjustPanelDisplay() {
	if (isMobile()) {
		$('#user-panel').replaceClass('col', 'col-3');
		$('#search-id').addClass('form-control-sm');
	} else {
		$('#user-panel').replaceClass('col-3', 'col');
		$('#search-id').removeClass('form-control-sm');
	}
}

/**
 * Load the friend/user list associated with the logged in user
 */
function loadUsername() {
	var jqXhr = $.ajax({
		url : 'load/selfdetails',
		method : 'get',
	});

	jqXhr.done(function(data) {
		$('#self-avatar').attr('src', data.avatar);
		$('#self-avatar').attr('alt', data.firstname + ' ' + data.lastname);
		var text = 'Welcome ' + data.firstname + '! Your last login: '
				+ data.lastLoginDateTime;
		$('#self-summary').text(text);
		$('#firstname').val(data.firstname);
		$('#username').val(data.username).trigger('change');
	});
}
function loadUsers() {
	var jqXhr = $.ajax({
		url : 'load/member',
		data : {
			username : $('#username').val()
		},
		method : 'get',
		contentType : CONTENT_TYPE_JSON
	});

	jqXhr.done(function(data) {
		var div = $('#user-list');
		if (data.length > 0) {
			div.empty();
			var receivers = [];
			$('#search-id').removeAttr('disabled');
			// var isSelfSender;
			$.each(data, function(idx, user) {
				// isSelfSender =
				generateUserList(div, user);
				// if (!isSelfSender)
				receivers.push(user.memberId);
			});

			if (receivers.length > 0) {
				sendBulkDeliveredReceipt(receivers);
			}

			$('.user-unit').click(
					function() {
					    var user = $(this);
						var selectedUsername = user.find('.friend-ids').val();
						if (selectedUsername !== '')
							$('#chat-instruction').remove();
						if ($('#chat-in-display').val() === selectedUsername)
							return false;
						if (isPresent($('#text-in').text().trim()))
							messageCache[$('#chat-in-display').val()] = $('#text-in').html();
						$('#text-in').empty();
						if (messageCache.hasOwnProperty(selectedUsername))
							$('#text-in').html(messageCache[selectedUsername]);
						$('#chat-in-display').val(selectedUsername).trigger(
								'change');
						user.find('.message-summary').removeClass('unread');
						user.find('.unread-count').empty();
						openChatPanel(user);
					});
		}
	});
}

/**
 * Generate list of users info depending
 * 
 * @param mainDiv
 *            DIV, where user list will be added
 * @param user
 *            Details of an user fetched from server
 */
function generateUserList(mainDiv, member) {
	var proto = `<div class="row no-gutters user-unit">
			        <input type="hidden" class="friend-ids">
    			    <input type="hidden" class="last-message-id">
    			    <input type="hidden" class="last-message-sender">
    			    <input type="hidden" class="last-online">
    			    <div class="col-2">
    			        <img class="avatar" height="40" width="40" >
    			    </div>
    			    <div class="col-10">
    			        <div class="row no-gutters">
    			            <div class="users text-truncate col-7">
    			                <span class="user-status"></span>
    			                <label class="fullname"></label>
    			             </div>
    			            <div class="col message-time text-right">
    			            </div>
    			            <div class="w-100"></div>
    			            <div class="message-summary col-10 text-truncate"></div>
    			            <div class="col text-right">
    			                <i class="material-icons md-12 status-icon"></i>
    			                <span class="badge badge-dark unread-count"></span>
    			            </div>
    			            </div>
    			     </div>
    			  </div>`;
	proto = $(proto);
	mainDiv.append(proto);

	proto.find('.friend-ids').val(member.memberId);
	var img = proto.find('.avatar');
	img.attr('src', member.avatar);
	img.prop('alt', member.memberName);

	var statusSpan = proto.find('.user-status');
	if (member.online)
		statusSpan.addClass('online');
	else 
		statusSpan.addClass('offline');
	proto.find('.last-online').val(member.lastOnlineAt);
	proto.find('.fullname').text(member.memberName);

	var lastMessage = member.lastMessage;
	if (lastMessage !== null) {
		proto.find('.last-message-id').val(lastMessage.messageId);
		proto.find('.last-message-sender').val(lastMessage.sender);
		var sentAt = lastMessage.sentAt;
		sentAt = sentAt.substring(0, sentAt.lastIndexOf(':'))
		proto.find('.message-time').text(sentAt);
		var messageSummaryDiv = proto.find('.message-summary'); 
		messageSummaryDiv.html(decodeURIComponent(lastMessage.content));
		if(member.unreadMessageCount > 0){
		    messageSummaryDiv.addClass('unread');
		    proto.find('.unread-count').text(member.unreadMessageCount);
		}

		if (lastMessage.sender === member.memberId)
			return;
		// false;
		var status = proto.find('.status-icon');
		switch (lastMessage.messageStatus) {
		case AcknowledgeType.SENT:
			status.text(StatusIconText.SENT);
			break;
		case AcknowledgeType.DELIVERED:
			status.text(StatusIconText.DELIVERED);
			break;
		case AcknowledgeType.READ:
			status.addClass('read');
			status.text(StatusIconText.DELIVERED);
			break;
		default:
			status.addClass('send-failed');
			status.text('error_outline');
			break;
		}
	}

	// return true;
}

/**
 * Open chat panel for selected user passed via parameter as jQuery object.
 * 
 * @param user
 *            Selected user
 */
function openChatPanel(user) {
	$('#curr-size').val(0);
	$('#next-size').val(100);
	$('#msg-panel').show();
	$('#message-out>div.col').empty();

	if (isMobile())
		$('#user-panel').hide();
	else
		$('#user-panel').show();

	var userDiv = $('#user-info');
	var proto = `<div class="image col">
                     <img class="avatar float-right" src="" alt="">
	             </div>
	             <div class="info col">
                     <div class="row">
                         <div class="col">
                             <span class="user-status"></span>
                             <label class="fullname"></label>
                         </div>
                     </div>
                     <div class="row">
	                     <div class="col">
                             <div class="last-online"></div>
                         </div>
                     </div>
	             </div>`;
	userDiv.html(proto);
	var src,tgt;
	src = user.find('.avatar');
	tgt = userDiv.find('.avatar');
	tgt.attr('src', src.attr('src'));
	tgt.attr('alt', src.attr('alt'));
	
	src = user.find('.user-status');
	tgt = userDiv.find('.user-status');
	tgt.removeAttr('class');
	tgt.addClass(src.attr('class'));
	
	src = user.find('.fullname');
	tgt = userDiv.find('.fullname');
	tgt.text(src.text());
	
	src =  user.find('.last-online');
	var lastSeen = src.val();
	tgt = userDiv.find('.last-online');
	if(isPresent(lastSeen) && lastSeen.length > 0){
    	lastSeen = `Seen ${getLastSeenFormatted(lastSeen)}`;
    	tgt.text(lastSeen)
    }
	$('#receiver-id').val(user.find('.friend-ids').val());

	loadMessages(1);
}

/**
 * Load message for given user. If called by user selection, only last 100
 * messages will be loaded. If called by scrolling up, next 100 from current
 * will be loaded. Calling from message notification is same as user selection.
 * 
 * @param user
 *            Selected user
 * @param caller
 *            caller identifier {0: scroll, 1: other}
 */
function loadMessages(caller) {
	var jqXhr = $.ajax({
		url : 'load/message',
		data : {
			sender : $('#receiver-id').val(),
			receiver : $('#username').val(),
			currentSize : $('#curr-size').val(),
			nextSize : $('#next-size').val()
		},
		method : 'get',
		contentType : CONTENT_TYPE_JSON
	});

	jqXhr.done(function(chats) {
		var messageDiv = $('#message-out>div.col');
		if (chats.length > 0) {
			$.each(chats,
					function(idx, msg) {
						messageAppender(messageDiv, msg,
								ChatAppenderCaller.USER_SELECT);
					});
			if (caller === 1) {
				scrollToBottom(messageDiv.parent());
			}
			sendBulkReadReceipt(chats)
		}
	});
}

// function renderMessage(mainDiv, msg) {
// var proto = '<div class="msg row">'
// + '<div class="col-10 col-lg-4 content"></div>'
// + '<div class="w-100"></div>'
// + '<div class="col-10 col-lg-4 attachments"></div>'
// + '<div class="w-100"></div>' + '<div class="col-10 col-lg-4">'
// + '<div class="d-inline message-time"></div>'
// + '<div class="d-inline message-status float-right"></div>'
// + '</div>';
// proto = $(proto);
// mainDiv.append(proto);
//
// proto.attr('id', msg.messageId);
// if (msg.sender)
// proto.addClass('justify-content-start');
// else
// proto.addClass('justify-content-end');
// proto.find('.content').text(msg.content);
// proto.find('.message-time').text(msg.sentAt);
// switch (msg.messageStatus) {
// case AcknowledgeType.READ:
// proto.find('.message-status').text('Seen');
// break;
// case AcknowledgeType.DELIVERED:
// proto.find('.message-status').text('Seen');
// break;
// default:
// proto.find('.message-status').text('Seen');
// break;
// }
// }

function searchUser() {
	var input, filter, ul, li;
	input = $('#search-id');
	filter = input.val().toLowerCase();
	ul = $('#user-list>ul');
	li = $('li');
	for (var i = 0; i < li.length; i++) {
		if ($(li[i]).text().toLowerCase().indexOf(filter) > -1) {
			$(li[i]).show();
		} else {
			$(li[i]).hide();

		}
	}
}

function controlChatContent(elem) {
	if (elem.text().length > 0) {
		$('#send-btn').removeClass('disabled');
	} else {
		$('#send-btn').addClass('disabled');
	}
}

function getLastSeenFormatted(lastSeen) {
    var todayDateTime = new Date();
    var lsDateTime = new Date(lastSeen);

    var diffInSec = parseInt((todayDateTime.getTime() - lsDateTime.getTime()) / 1000);
    var today = new Date(todayDateTime.getTime());
    today.setHours(0, 0, 0, 0);
    var lsDateOnly = new Date(lsDateTime.getTime());
    lsDateOnly.setHours(0, 0, 0, 0);

    var oneDay = 24 * 3600 * 1000;
    var diffInDays = today.getTime() - lsDateOnly.getTime();
    diffInDays /= (24 * 3600 * 1000);

    var h = parseInt((diffInSec / 3600));
    var m = parseInt((diffInSec / 60));

    if (h <= 12) {
        if (h >= 1) {
            lastSeen = parseInt((diffInSec / 3600)) + 'h ago';
        } else if (m >= 1) {
            lastSeen = (m === 1) ? 'a minute ago' : (m + 'm ago');
        } else {
            lastSeen = diffInSec + 's ago';
        }
    } else if (diffInDays === 0) {
        lastSeen = 'Today ' + formatTime(lsDateTime);
    } else if (diffInDays == 1) {
        lastSeen = 'Yesterday ' + formatTime(lsDateTime);
    } else if (diffInDays <= 30) {
        lastSeen = diffInDays + ' days ago';
    } else {
        lastSeen = formatDateTime(lsDateTime);
    }
    return lastSeen;
}

function formatDateTime(datetime) {
    return formatDate(datetime) + ' ' + formatTime(datetime);
}
function formatDate(date) {
    return to2digit(date.getFullYear()) + '/' + to2digit(date.getMonth() + 1)
            + '/' + to2digit(date.getDate());
}
function formatTime(time) {
    return to2digit(time.getHours()) + ':' + to2digit(time.getMinutes());
}
function to2digit(d) {
    return d < 10 ? '0' + d : d;
}