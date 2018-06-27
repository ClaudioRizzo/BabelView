import matplotlib.pyplot as plt


def draw_pie(percentages, labels):


	# Pie chart, where the slices will be ordered and plotted counter-clockwise:
	#explode = (0, 0.1, 0, 0)  # only "explode" the 2nd slice (i.e. 'Hogs')

	fig1, ax1 = plt.subplots()
	ax1.pie(percentages, labels=labels, autopct='%1.1f%%',
	        shadow=True, startangle=90)
	ax1.axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.
	plt.show()