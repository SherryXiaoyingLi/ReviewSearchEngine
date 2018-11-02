import json
import gzip
import os

def parse(path):
  g = open(path, 'r')
  for l in g:
    yield json.dumps(eval(l))

for path, subdirs, files in os.walk('/Users/lixiaoying/Desktop/datas'):
	newFiles = []
	for num in range(len(files)):
		if(files[num].endswith(".json")):
			newFiles.append(files[num])
	print(newFiles)
	for filename in newFiles:
		print(filename)
		counter = 0
		fileNum = 0
		for l in parse(path+"/"+filename):
			if(counter >= 10000 or fileNum == 0):
				if (fileNum != 0):
					f.write("]}")
				f = open('/Users/lixiaoying/Desktop/datas/' + filename.replace(".json","_output_") +str(fileNum) + ".json", 'w')
				f.write("{\"Reviews\": [")
				counter = 0
				fileNum += 1
			f.write(l + ',\n')
			counter += 1
		f.write("]}")