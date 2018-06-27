from matplotlib import cm as cm
import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt



def corr_plot(corr, labels):
	sns.set(style="white")

	# Generate a mask for the upper triangle
	mask = np.zeros_like(corr, dtype=np.bool)
	mask[np.triu_indices_from(mask)] = True

	# Set up the matplotlib figure
	f, ax = plt.subplots(figsize=(11, 9))

	# Generate a custom diverging colormap
	cmap = cmap = sns.diverging_palette(220, 10	, as_cmap=True)
	#sns.diverging_palette(220, 10, as_cmap=True)
	#cm.get_cmap('jet', 30)

	#plt.yticks(rotation=180)
	plt.xticks(rotation=90, fontsize=14) 
	# Draw the heatmap with the mask and correct aspect rotation
	g = sns.heatmap(corr,  cmap=cmap, vmax=.3,
	            square=True, xticklabels=labels, yticklabels=labels,
	            linewidths=0, cbar_kws={"shrink": .5}, ax=ax)
	
	g.set_yticklabels(g.get_yticklabels(), rotation=0, fontsize=14)


	#xticks = ax.xaxis.get_major_ticks()
	#xticks[-1].label.set_visible(False)

	#yticks = ax.yaxis.get_major_ticks()
	#yticks[-1].label.set_visible(False)

	plt.tight_layout()
	plt.show()


def correlation_matrix(matrix, labels):
	df = pd.DataFrame(matrix)

	fig = plt.figure()
	ax1 = fig.add_subplot(111)
	cmap = cm.get_cmap('jet', 30)
	cax = ax1.imshow(df, interpolation="nearest", cmap=cmap)
	ax1.grid(False)
	plt.title('Vulnerabilities Correlation')
	labels=labels
	ax1.set_xticklabels(labels,fontsize=12)
	ax1.set_yticklabels(labels,fontsize=12)
	# Add colorbar, make sure to specify tick locations to match desired ticklabels
	fig.colorbar(cax, ticks=[0, .1, .2,.30])

	plt.show()
