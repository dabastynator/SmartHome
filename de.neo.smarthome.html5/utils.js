
function align(){
	var north = document.getElementById('north');
	var east = document.getElementById('east');
	var south = document.getElementById('south');
	var west = document.getElementById('west');
	var center = document.getElementById('center');
	var offset = 8;
	var padding = 0;
	var left = offset;
	var right = window.innerWidth - offset;
	var top = offset;
	var bottom = window.innerHeight - offset;
	if (north != null){
		north.style.position = "absolute";
		north.style.left = offset + 'px';
		north.style.width = window.innerWidth - 2 * (offset + padding) + 'px';
		north.style.top = offset + 'px';
		top = north.offsetHeight + 2 * offset;
	}
	if (south != null){
		south.style.position = "absolute";
		south.style.left = offset + 'px';
		south.style.width = window.innerWidth - 2 * (offset + padding) + 'px';
		south.style.top = bottom - south.offsetHeight - offset + 'px';
		bottom = bottom - south.offsetHeight - 2 * (offset + padding);
	}
	if (west != null){
		west.style.position = "absolute";
		west.style.top = top + 'px';
		west.style.height = bottom - top + 'px';
		west.style.left = offset + 'px';
		left = west.offsetWidth + 2 * offset;
	}
	if (east != null){
		east.style.position = "absolute";
		east.style.top = top + 'px';
		east.style.height = bottom - top + 'px';
		east.style.left = window.innerWidth - east.offsetWidth - offset + 2 + 'px';
		right = window.innerWidth - east.offsetWidth - 2 * (offset + padding);
	}
	if (center != null){
		center.style.position = "absolute";
		center.style.top = top + 'px';
		center.style.height = bottom - top + 'px';
		center.style.left = left + 'px';
		center.style.width = right - left + 'px';
	}
}

function showDialog(id) {
	var container = document.getElementById(id);
	var top_offset = 100;
	container.style.visibility = 'visible';
	container.style.opacity = 0;
	currentDialog = document.getElementById('content_' + id);
	if (currentDialog != null) {
		currentDialog.style.position = 'absolute';
		currentDialog.style.left = (window.innerWidth - currentDialog.offsetWidth)
				/ 2 + 'px';
		var top = (window.innerHeight - currentDialog.offsetHeight) / 2;
		if (top > top_offset) {
			disableScroll();
		} else {
			top = top_offset;
			currentDialog.style.height = window.innerHeight - 2 * top_offset
					+ 'px';
		}
		var animateOffset = 20;
		var topAnimate = top - animateOffset;
		currentDialog.style.top = topAnimate + 'px';
		var id = setInterval(appearDialog, 15);
		function appearDialog() {
			if (topAnimate >= top) {
				currentDialog.style.top = top;
				clearInterval(id);
			} else {
				topAnimate++;
				currentDialog.style.top = topAnimate + 'px';
				container.style.opacity = 1 - (top - topAnimate)
						/ animateOffset;
			}
		}
	}
}

function hideDialog(id) {
	var container = document.getElementById(id);
	currentDialog = document.getElementById('content_' + id);
	if (currentDialog != null) {
		var top = (window.innerHeight - currentDialog.offsetHeight) / 2;
		if (top < 0) {
			top = 10;
		}
		var animateOffset = 20;
		var topAnimate = top;
		var id = setInterval(disappearDialog, 15);
		function disappearDialog() {
			if (topAnimate <= top - animateOffset) {
				container.style.visibility = 'hidden';
				clearInterval(id);
			} else {
				topAnimate--;
				currentDialog.style.top = topAnimate + 'px';
				container.style.opacity = 1 - (top - topAnimate)
						/ animateOffset;
			}
		}
	}
	enableScroll();
}

function preventDefault(e) {
	e = e || window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;
}

function preventDefaultForScrollKeys(e) {
	if (keys[e.keyCode]) {
		preventDefault(e);
		return false;
	}
}

function disableScroll() {
	if (window.addEventListener) // older FF
		window.addEventListener('DOMMouseScroll', preventDefault, false);
	window.onwheel = preventDefault; // modern standard
	window.onmousewheel = document.onmousewheel = preventDefault; // older
	// browsers,
	// IE
	window.ontouchmove = preventDefault; // mobile
	document.onkeydown = preventDefaultForScrollKeys;
}

function enableScroll() {
	if (window.removeEventListener)
		window.removeEventListener('DOMMouseScroll', preventDefault, false);
	window.onmousewheel = document.onmousewheel = null;
	window.onwheel = null;
	window.ontouchmove = null;
	document.onkeydown = null;
}
