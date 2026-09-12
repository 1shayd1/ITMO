#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  08.11.25

import yaml
import json
import time

start1 = time.perf_counter()
for i in range(100):
    with open('Schedule.yaml', 'r') as yamlFile:
        data = yaml.safe_load(yamlFile)
end1 = time.perf_counter()
print(f"Time of running: {end1 - start1:.8f} seconds.")

start2 = time.perf_counter()
for i in range(100):
    with open('PythonOutput.json', 'w') as jsonFile:
        json.dump(data, jsonFile, indent = 4)

end2 = time.perf_counter()

print(f"Time of running: {end2 - start2:.8f} seconds.")
#0.40941330
#0.10408600