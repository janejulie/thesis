import matplotlib
import matplotlib.pyplot as plt
import numpy as np

month_intensity = np.array([60, 70, 80, 90, 100])
week_intensity = np.array([70, 85, 100])
regeneration_week = 30
intensity = np.zeros(5*4)
for i in range(5):
    for j in range(3):
        intensity[i*4+j] = (month_intensity[i]*week_intensity[j]/100)
    intensity[i*4+3] = regeneration_week

t = np.arange(len(intensity))
intensity = np.array(intensity)
hours = np.array(intensity*20/100)

fig, ax = plt.subplots()
ax.plot(t, intensity)
ax.plot(t, hours)

ax.set_xticks(t)
ax.set(xlabel='week', ylabel='traininghours')
ax.set(xlabel='week', ylabel='intensity')
ax.grid()
fig.savefig("5months.png")
plt.show()
