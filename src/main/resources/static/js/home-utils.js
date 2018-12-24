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
    var days = dateDiff(lastTsDate, new Date());
    var dtGrp;
    switch (days) {
    case 0:
        dtGrp = 'Today';
        break;
    case 1:
        dtGrp = 'Yesterday';
        break;
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
        dtGrp = weekdays[lastTsDate.getDay()];
        break;
    default:
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