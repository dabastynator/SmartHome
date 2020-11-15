
function align(){
	var north = document.getElementById('north');
	var east = document.getElementById('east');
	var south = document.getElementById('south');
	var west = document.getElementById('west');
	var center = document.getElementById('center');
	var navigation = document.getElementById('border_navigation');
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
	if (right - left > 1000){
		if (navigation != null){
			navigation.style.visibility = 'hidden';
		}
		if (west != null){
			west.style.position = "absolute";
			west.style.top = top + 'px';
			west.style.height = bottom - top + 'px';
			west.style.left = offset + 'px';
			west.style.width = '310px';
			west.style.visibility = 'visible';
			left = west.offsetWidth + 2 * offset;
		}
		if (east != null){
			east.style.position = "absolute";
			east.style.top = top + 'px';
			east.style.height = bottom - top + 'px';
			east.style.width = (window.innerWidth / 3) + 'px';
			east.style.left = window.innerWidth - east.offsetWidth - offset + 2 + 'px';
			east.style.visibility = 'visible';
			right = window.innerWidth - east.offsetWidth - 2 * (offset + padding);
		}
		if (center != null){
			center.style.position = "absolute";
			center.style.top = top + 'px';
			center.style.height = bottom - top + 'px';
			center.style.left = left + 'px';
			center.style.visibility = 'visible';
			center.style.width = right - left + 'px';
		}
	} else {
		if (navigation != null){
			navigation.style.visibility = 'visible';
			navigation.style.position = "absolute";
			navigation.style.left = offset + 'px';
			navigation.style.width = window.innerWidth - 2 * (offset + padding) + 'px';
			navigation.style.top = top + 'px';
			top = top + navigation.offsetHeight + 2 * offset;
		}
		if (west != null){
			west.style.position = "absolute";
			west.style.top = top + 'px';
			west.style.height = bottom - top + 'px';
			west.style.left = left + 'px';
			west.style.width = right - left + 'px';
		}
		if (east != null){
			east.style.position = "absolute";
			east.style.top = top + 'px';
			east.style.height = bottom - top + 'px';
			east.style.left = left + 'px';
			east.style.width = right - left + 'px';
		}
		if (center != null){
			center.style.position = "absolute";
			center.style.top = top + 'px';
			center.style.height = bottom - top + 'px';
			center.style.left = left + 'px';
			center.style.width = right - left + 'px';
		}
	}

	offset = 15;
	var filling = document.getElementsByClassName("filling");
	for (var i = 0; i < filling.length; i++) {
		filling[i].style.position = "absolute";
		filling[i].style.left = offset + 'px';
		filling[i].style.width = window.innerWidth - 2 * (offset + padding) + 'px';
		filling[i].style.top = offset + 'px';
		filling[i].style.height = window.innerHeight - 2 * (offset + padding) + 'px';
	} 
}

function showArea(area){
	var east = document.getElementById('east');
	var west = document.getElementById('west');
	var center = document.getElementById('center');
	if (east != null){
		if (area === 'east')
			east.style.visibility = "visible";
		else
			east.style.visibility = "hidden";
	}
	if (west != null){
		if (area === 'west')
			west.style.visibility = "visible";
		else
			west.style.visibility = "hidden";
	}
	if (center != null){
		if (area === 'center')
			center.style.visibility = "visible";
		else
			center.style.visibility = "hidden";
	}
}

function showDialog(id) {
	var container = document.getElementById(id);
	container.classList.add("visible");
	var content = document.getElementById('content_' + id);
	if (content.classList.contains('dialog')){
		content.classList.add("dialog_on");
	}
	if (content.classList.contains('filling')){
		content.classList.add("filling_on");
	}
}

function hideDialog(id) {
	var container = document.getElementById(id);
	container.classList.remove("visible");
	var content = document.getElementById('content_' + id);
	if (content.classList.contains('dialog')){
		content.classList.remove("dialog_on");
	}
	if (content.classList.contains('filling')){
		content.classList.remove("filling_on");
	}
}
