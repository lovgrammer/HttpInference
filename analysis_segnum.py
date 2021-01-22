import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

data = pd.read_csv('result.csv')

local_mean = np.mean(data['numberOfSegmentsLocal'])
remote1_mean = np.mean(data['numberOfSegmentsRemote1'])
remote2_mean = np.mean(data['numberOfSegmentsRemote2'])

local_std = np.std(data['numberOfSegmentsLocal'])
remote1_std = np.std(data['numberOfSegmentsRemote1'])
remote2_std = np.std(data['numberOfSegmentsRemote2'])

nodes = ['Local', 'Remote1', 'Remote2']
x_pos = np.arange(len(nodes))
CTEs = [local_mean, remote1_mean, remote2_mean]
error = [local_std, remote1_std, remote2_std]

fig, ax = plt.subplots()
ax.bar(x_pos, CTEs, yerr=error, align='center', alpha=0.5, ecolor='black', capsize=10)
ax.set_ylabel('Nubmer of segments')
ax.set_xticks(x_pos)
ax.set_xticklabels(nodes)
ax.set_title('Number of segments')
ax.yaxis.grid(True)

# Save the figure and show
plt.tight_layout()
plt.savefig('bar_plot_with_error_bars.png')
plt.show()


