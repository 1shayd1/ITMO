#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  19.11.25

import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv('Data.csv', sep=',')
df['date'] = pd.to_datetime(df['date'], format='%d.%m.%Y')
fig, ax = plt.subplots(figsize=(10, 5.5))

data_to_plot = []
labels = []

unique_dates = df['date'].dt.date.unique()

for date in unique_dates:
    day_data = df[df['date'].dt.date == date]
    data_to_plot.append(day_data['open'])
    data_to_plot.append(day_data['high'])
    data_to_plot.append(day_data['low'])
    data_to_plot.append(day_data['close'])
    labels.extend(["",f'              {date}',"",""])

box = ax.boxplot(data_to_plot, patch_artist=True, labels=labels, showfliers=False)

ax.set_title('Биржевые данные')
ax.set_ylabel('Курс')
ax.set_xlabel('Дата')

colors = ['blue', 'cyan', 'grey', 'darkblue']
for i, patch in enumerate(box['boxes']):
    patch.set_facecolor(colors[i % 4])

legend_labels = ['<OPEN>', '<HIGH>', '<LOW>', '<CLOSE>']
legend_handles = [plt.Rectangle((0,0),1,1, color=colors[i]) for i in range(4)]
ax.legend(legend_handles, legend_labels, loc='upper left')

plt.tight_layout()
plt.show()