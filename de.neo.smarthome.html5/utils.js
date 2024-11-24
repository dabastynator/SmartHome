
window.onresize = function()
{
	var areas = ["playlists","west","center"];
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var elem = document.getElementById(a);
		if (elem != null){
			elem.style.zIndex = "auto";
		}
	}
};

function align()
{
	var padding = 0;

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

function showArea(area)
{
	var areas = ["playlists","west","center"];
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var elem = document.getElementById(a);
		var nav = document.getElementById('nav_' + a);
		if (elem != null){
			if (a === area)
				elem.style.zIndex = "auto";
			else
				elem.style.zIndex = -1;
		}
		if (nav != null){
			if (a === area){
				nav.classList.add('on');
				nav.classList.remove('off');
			}else{
				nav.classList.remove('on');
				nav.classList.add('off');
			}
		}
	}
}

function showDialog(id)
{
	var container = document.getElementById(id);
	container.classList.add("visible");
}

function hideDialog(id)
{
	var container = document.getElementById(id);
	container.classList.remove("visible");
}
