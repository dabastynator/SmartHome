import Tkinter as tk
import PIL.Image
import PIL.ImageTk
import requests
import time
import locale

class Mirror(object):
	def __init__(self, master, **kwargs):
		master.bind('<Escape>',self.toggle_geom)
		self.master=master
		self.container_time = tk.Frame(master, bg="black")
		self.time = tk.Label(self.container_time, text="<Time>", fg="white", bg="black", font=("Ubuntu Medium", 100))
		self.date = tk.Label(self.container_time, text="<Date>", fg="white", bg="black", font=("Ubuntu Medium", 30))
		self.container_music = tk.Frame(master, bg="black")
		self.container_temp = tk.Frame(self.container_music, bg="black")
		self.container_blank = tk.Frame(master, bg="black")
		self.title = tk.Label(self.container_temp, text="<Title>", fg="white", bg="black", font=("Ubuntu Medium", 30))
		self.artist = tk.Label(self.container_temp, text="<Artist>", fg="white", bg="black", font=("Ubuntu Medium", 25))
		load = PIL.Image.open("img/speaker.png")
		render = PIL.ImageTk.PhotoImage(load)
		self.music = tk.Label(self.container_music, image=render, borderwidth=0)
		self.music.image = render

		self.container_time.grid(row=0, sticky=tk.W)
		self.time.pack()
		self.date.pack()

		self.container_blank.grid(row=1, sticky=tk.W)
		master.grid_rowconfigure(1, weight=10)

		self.container_music.grid(row=2, column=0, sticky=tk.W)
		self.container_temp.grid(row=0, column=1, sticky=tk.W)
		self.title.grid(row=0, sticky=tk.W)
		self.artist.grid(row=1, sticky=tk.W)
		self.music.grid(row=0, column=0, sticky=tk.W)

		self.refresh()

	def refresh(self):
		self.refreshTime()
		self.refreshPlaying()

	def refreshTime(self):
		current_time = time.localtime()
		time_format = time.strftime('%H:%M', current_time)
		self.time['text'] = time_format
		time_format = time.strftime('%A, %d. %B', current_time)
		self.date['text'] = time_format

	def refreshPlaying(self):
		artist = ''
		title = ''
		r = requests.get('http://asterix:5061/mediaserver/list?token=w4kzd4HQ', timeout=0.1)
		if r.json()[0]['current_playing'] is not None:
			if r.json()[0]['current_playing'].get('artist'):
				artist = r.json()[0]['current_playing']['artist']
			if r.json()[0]['current_playing'].get('title'):
				title = r.json()[0]['current_playing']['title']
		self.artist['text'] = artist
		self.title['text'] = title

	def toggle_geom(self,event):
		self.master.geometry('800x600+0+0')


if __name__ == '__main__':
	master = tk.Tk()
	master.configure(bg='black')

	mirror=Mirror(master)
	locale.setlocale(locale.LC_ALL, "de_DE.utf8")

	w, h = master.winfo_screenwidth(), master.winfo_screenheight()
	#master.overrideredirect(1)
	#master.geometry("%dx%d+0+0" % (w, h))

	tk.mainloop()
