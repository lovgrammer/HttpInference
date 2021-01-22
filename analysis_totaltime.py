import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

data = pd.read_csv('result.csv')

local_inf_mean = np.mean(data['inferenceTimeLocal'])
remote1_inf_mean = np.mean(data['totalExecutionTimeRemote1'])
remote2_inf_mean = np.mean(data['totalExecutionTimeRemote2'])

local_inf_std = np.std(data['inferenceTimeLocal'])
remote1_inf_std = np.std(data['totalExecutionTimeRemote1'])
remote2_inf_std = np.std(data['totalExecutionTimeRemote2'])

nodes = ['Local', 'Remote1', 'Remote2']
x_pos = np.arange(len(nodes))
CTEs = [local_inf_mean, remote1_inf_mean, remote2_inf_mean]
error = [local_inf_std, remote1_inf_std, remote2_inf_std]

fig, ax = plt.subplots()
ax.bar(x_pos, CTEs, yerr=error, align='center', alpha=0.5, ecolor='black', capsize=10)
ax.set_ylabel('Latency (ms)')
ax.set_xticks(x_pos)
ax.set_xticklabels(nodes)
ax.set_title('Total Execution Time (Network + Inference)')
ax.yaxis.grid(True)

# Save the figure and show
plt.tight_layout()
plt.savefig('bar_plot_with_error_bars.png')
plt.show()


