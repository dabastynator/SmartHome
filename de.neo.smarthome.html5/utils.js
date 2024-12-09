var currentVisible = undefined;

window.onresize = function()
{
	if (currentVisible == undefined)
	{
		currentVisible = document.getElementById("switches");
	}
	var areas = ["playlists","switches","filesystem", "information"];
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var elem = document.getElementById(a);
		if (elem != null)
		{
			if (elem == currentVisible || window.innerWidth >= 900)
			{
				elem.style.zIndex = "auto";
			}
			else
			{
				elem.style.zIndex = -1;
			}
		}
	}
};


loadAppearence();
function loadAppearence()
{
	appearence = token = window.localStorage.getItem("appearence");
	if(appearence == 'bright')
	{
		document.documentElement.style.setProperty("--body_background", "white");
		document.documentElement.style.setProperty("--background", "white");
		document.documentElement.style.setProperty("--font", "black");
		document.documentElement.style.setProperty("--font_on", "white");
		document.documentElement.style.setProperty("--font_name", "'Source Code Pro', monospace");
		document.documentElement.style.setProperty("--title", "black");
		document.documentElement.style.setProperty("--border", "black");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "0px");
		document.documentElement.style.setProperty("--focus", "black");
		document.documentElement.style.setProperty("--btn_radius", "0px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#888");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid white");
		document.documentElement.style.setProperty("--btn_on_bg", "black");
		document.documentElement.style.setProperty("--btn_off_border_color", "black");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid black");
		document.documentElement.style.setProperty("--btn_off_bg", "white");
		document.documentElement.style.setProperty("--img_brightness", "0.3");
		document.documentElement.style.setProperty("--img_brightness_hover", "0");
		document.documentElement.style.setProperty("--btn_card_color", "#4d4d4d");
		document.documentElement.style.setProperty("--img_card_brightness", "1");
	}
	else if(appearence == 'turquoise')
	{
		document.documentElement.style.setProperty("--body_background", "#05445e");
		document.documentElement.style.setProperty("--background", "#d4f1f4");
		document.documentElement.style.setProperty("--font", "black");
		document.documentElement.style.setProperty("--font_on", "var(--font)");
		document.documentElement.style.setProperty("--font_name", "Ubuntu");
		document.documentElement.style.setProperty("--title", "black");
		document.documentElement.style.setProperty("--border", "black");
		document.documentElement.style.setProperty("--card_border", "0px");
		document.documentElement.style.setProperty("--card_radius", "25px");
		document.documentElement.style.setProperty("--focus", "#05445e");
		document.documentElement.style.setProperty("--btn_radius", "20px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#05445E");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid #05445E");
		document.documentElement.style.setProperty("--btn_on_bg", "#189AB4");
		document.documentElement.style.setProperty("--btn_off_border_color", "#189AB4");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid #189AB4");
		document.documentElement.style.setProperty("--btn_off_bg", "#D4F1F4");
		document.documentElement.style.setProperty("--img_brightness", "0.3");
		document.documentElement.style.setProperty("--img_brightness_hover", "0");
		document.documentElement.style.setProperty("--btn_card_color", "var(--body_background)");
		document.documentElement.style.setProperty("--img_card_brightness", "0.9");
	}
	else if(appearence == 'darkred')
	{
		document.documentElement.style.setProperty("--body_background", "black");
		document.documentElement.style.setProperty("--background", "#100");
		document.documentElement.style.setProperty("--font", "white");
		document.documentElement.style.setProperty("--font_on", "var(--font)");
		document.documentElement.style.setProperty("--font_name", "'Source Code Pro', monospace");
		document.documentElement.style.setProperty("--title", "white");
		document.documentElement.style.setProperty("--border", "#800");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "13px");
		document.documentElement.style.setProperty("--focus", "#f55");
		document.documentElement.style.setProperty("--btn_radius", "8px");
		document.documentElement.style.setProperty("--btn_border", "1px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#a00");
		document.documentElement.style.setProperty("--btn_on_border", "var(--btn_border) solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "var(--btn_border) solid #a00");
		document.documentElement.style.setProperty("--btn_on_bg", "#500");
		document.documentElement.style.setProperty("--btn_off_border_color", "#a00");
		document.documentElement.style.setProperty("--btn_off_border", "var(--btn_border) solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "var(--btn_border) solid #a00");
		document.documentElement.style.setProperty("--btn_off_bg", "#000");
		document.documentElement.style.setProperty("--img_brightness", "0.8");
		document.documentElement.style.setProperty("--img_brightness_hover", "1");
		document.documentElement.style.setProperty("--btn_card_color", "white");
		document.documentElement.style.setProperty("--img_card_brightness", "0");
	}
	else
	{
		document.documentElement.style.setProperty("--body_background", "black");
		document.documentElement.style.setProperty("--background", "black");
		document.documentElement.style.setProperty("--font", "white");
		document.documentElement.style.setProperty("--font_on", "var(--font)");
		document.documentElement.style.setProperty("--font_name", "'Source Code Pro', monospace");
		document.documentElement.style.setProperty("--title", "#aaa");
		document.documentElement.style.setProperty("--border", "#666");
		document.documentElement.style.setProperty("--card_border", "1px solid var(--border)");
		document.documentElement.style.setProperty("--card_radius", "3px");
		document.documentElement.style.setProperty("--focus", "#888");
		document.documentElement.style.setProperty("--btn_radius", "2px");
		document.documentElement.style.setProperty("--btn_border", "2px");
		document.documentElement.style.setProperty("--btn_on_border_color", "#666");
		document.documentElement.style.setProperty("--btn_on_border", "2px solid var(--btn_on_border_color)");
		document.documentElement.style.setProperty("--btn_on_border_hover", "2px solid #888");
		document.documentElement.style.setProperty("--btn_on_bg", "#222");
		document.documentElement.style.setProperty("--btn_off_border_color", "#222");
		document.documentElement.style.setProperty("--btn_off_border", "2px solid var(--btn_off_border_color)");		
		document.documentElement.style.setProperty("--btn_off_border_hover", "2px solid #888");
		document.documentElement.style.setProperty("--btn_off_bg", "black");
		document.documentElement.style.setProperty("--img_brightness", "0.8");
		document.documentElement.style.setProperty("--img_brightness_hover", "1");
		document.documentElement.style.setProperty("--btn_card_color", "#e6e6e6");
		document.documentElement.style.setProperty("--img_card_brightness", "0");
	}
}

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
	var areas = ["playlists","switches","filesystem", "information"];
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var elem = document.getElementById(a);
		var nav = document.getElementById('nav_' + a);
		if (elem != null){
			if (a === area)
			{
				elem.style.zIndex = "auto";
				currentVisible = elem;
			}
			else
			{
				elem.style.zIndex = -1;
			}
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
