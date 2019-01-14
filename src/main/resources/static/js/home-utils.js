function getDateGroupDiv(ts, caller) {
	var months = [ 'JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG',
			'SEP', 'OCT', 'NOV', 'DEC' ];
	var weekdays = [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday',
			'Friday', 'Saturday' ];
	var lastTs = ts.substring(0, ts.lastIndexOf(' '));
	var dateParts = lastTs.split('/');
	var lastTsDate = parseDate(lastTs);
	var lastDtGrp = (caller === ChatAppenderCaller.SEND_BTN || caller === ChatAppenderCaller.NEW_MESSAGE) ? $('.dt-grp:last')
			: $('.dt-grp:first');
	var today = new Date();
	today.setHours(0);
	today.setMinutes(0);
	today.setSeconds(0);
	var days = dateDiff(lastTsDate, today);
	var dtGrp;
	if (days === 0) {
		dtGrp = 'Today';
	} else if (days === 1) {
		dtGrp = 'Yesterday';
	} else if (days >= 2 && days <= 6) {
		dtGrp = weekdays[lastTsDate.getDay()];
	} else {
		dtGrp = `${dateParts[0]} ${months[parseInt(dateParts[1]) - 1]} ${dateParts[2]}`;
	}
	var proceed = (lastDtGrp.length === 0 || (lastDtGrp.length > 0 && lastDtGrp
			.text() !== dtGrp));

	return proceed ? $(`<div class="dt-grp" title="${lastTs}"><p>${dtGrp}</p></div>`)
			: null;
}

function parseDate(str) {
	var mdy = str.split('/');
	return new Date(mdy[0], mdy[1] - 1, mdy[2]);
}

function dateDiff(first, second) {
	// Take the difference between the dates and divide by milliseconds per day.
	// Round to nearest whole number to deal with DST.
	return Math.round((second - first) / (1000 * 60 * 60 * 24));
}

function scrollToBottom(content) {
	content[0].scrollTop = content[0].scrollHeight;
}

function durationToHhMmSs(duration) {
	var s = parseInt(duration);
	if (s < 60) {
		if (s < 10)
			s = '0' + s;
		return '0:' + s;
	}
	var m = parseInt(s / 60);
	s = s % 60;
	s = s < 10 ? ('0' + s) : s;
	if (m < 60)
		return (m < 10 ? ('0' + m) : m) + ':' + s;

	var h = parseInt(m / 60);
	m = m % 60;
	m = m < 10 ? ('0' + m) : m;
	return h + ':' + m + ':' + s;

}

function getLastSeenFormatted(lastSeen) {
	var todayDateTime = new Date();
	var lsDateTime = new Date(lastSeen);

	var diffInSec = parseInt((todayDateTime.getTime() - lsDateTime.getTime()) / 1000);
	var today = new Date(todayDateTime.getTime());
	today.setHours(0, 0, 0, 0);
	var lsDateOnly = new Date(lsDateTime.getTime());
	lsDateOnly.setHours(0, 0, 0, 0);

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
			lastSeen = 'a moment ago';
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
	return `Seen ${lastSeen}`;
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

function getMessageSummaryContent(message, isSelfSender) {
	if (message.contentType === ContentType.TEXT
			|| message.contentType === ContentType.MIXED)
		return decodeURIComponent(message.content);

	if (message.contentType === ContentType.BINARY
			|| message.contentType === ContentType.MIXED) {

		var content = isSelfSender ? "Sent " : "Received ";
		switch (message.mediaType) {
		case MediaType.IMAGE:
			content += '<i class="material-icons md-15">insert_photo</i>';
			break;
		case MediaType.AUDIO:
			content += '<i class="material-icons md-15">audiotrack</i>';
			break;
		case MediaType.VIDEO:
			content += '<i class="material-icons md-15">videocam</i>';
			break;
		case MediaType.TEXT:
		case MediaType.PDF:
		case MediaType.DOCUMENT:
			content += '<i class="material-icons md-15">insert_drive_file</i>';
			break;
		}
	}
	return content;
}