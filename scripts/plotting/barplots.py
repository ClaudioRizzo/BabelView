import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.cm as cm

from matplotlib.backends.backend_pdf import PdfPages


# group_labels: list of labels (l0, l2...ln)
# categories: list of tuples [ (x_l0..x_li..x_ln), (y_l0..y_li..y_ln) , .. #number of actual vulns]
# labels: vulnerabilities labels
# title: title of the plot
def stack_plot(group_labels, categories, labels, title):
	font = {'family' : 'normal', 
		'weight' : 'normal', 
		'size'   : 15
		}

	plt.rc('font', **font)
	colors = cm.tab20b(np.linspace(0, 1, len(categories)+1))
	
	plt.ylabel('#Applications')
	width=0.35
	y_pos = np.arange(len(categories[0]))
	
	plots = []
	for i in range(len(categories)):
		bott = 0
		for j in range(0, i):
			bott += np.array(categories[j])
		print colors[i]
		p = plt.bar(y_pos, categories[i], color=colors[i], bottom=bott)
		plots.append(p)
		

	plt.xticks(y_pos, group_labels, rotation=40, ha='right')
	plt.legend( [p[0] for p in plots],  labels)
	plt.show()

# object: list of labels for bars
# performance: list of values for each label
# title: title for the plot
# hatches supported for objectes with no more than 12 labels
def simple_plot(objects, performance, title, limit=700):
	font = {'family' : 'normal', 
			'weight' : 'normal', 
			'size'   : 15
			}

	plt.rc('font', **font)
	
	fig, ax = plt.subplots(figsize=(10,5))
	
	colors = cm.Blues(np.linspace(0, 1, len(objects)))
	y_pos = np.arange(len(objects))
	width = 0.5
	barlist = plt.bar(y_pos, performance, width, align='center')
	#barlist[0].set_color('#000000')
	
	plt.xticks(y_pos, objects, rotation=40, ha='right')
	#plt.xticks([])
	for p in ax.patches:
		height = p.get_height()
		ax.text(p.get_x()+p.get_width()/2., height + 3, '%d' % (height), ha="center") 


	ax.set_ylabel('#Applications')
	
	ax.set_axisbelow(True)
	ax.yaxis.grid(color='#BEBEBE', linestyle='dashed')
	ax.xaxis.grid(color='#BEBEBE', linestyle='dashed')

	plt.ylim(0, limit)
	
	hatches = ['-', '+', 'x', '\\', '*', 'o', 'O', '.', '|', 'X', "/", "\\\\"]
	i = 0
	for x,y,c,lb in zip(y_pos,performance,colors,objects):
		ax.bar(x, y, width, color=c,label=lb, alpha=0.8, edgecolor = '#000000')#, hatch=hatches[i])
		i+=1

	print i


	#ax.legend(ncol=2, loc='best')
	# ax.set_title(title)
	fig.tight_layout()

	plt.savefig(title+'.pdf')