import sys
import os
import subprocess

inputName = sys.argv[1]
maxEdge = sys.argv[2]
minEdge = sys.argv[3]
vertices = sys.argv[4]

numberOfTests = int(sys.argv[5])
launchOption = int(sys.argv[6])

resultsMPI = []
resultsMPI2 = []
resultsMt = []
theType = ""

edges = subprocess.check_output(["python3", "scriptGenerateGraph.py", inputName, maxEdge, minEdge, vertices])
#edges = edges.rsplit(b".")[-1].strip().decode('utf-8')
edges = edges.rsplit(b".")
print(edges[-1].strip().decode('utf-8'))
edges = edges[-1].strip().decode('utf-8')

inputName += ".gr"

for index in range(0, numberOfTests):
	mpiTime = subprocess.check_output(["mpiexec", "-n", "6", "./proiect", inputName, "outputScript.out"])
	resultsMPI.append(float(mpiTime))
	
	mpiTime2 = subprocess.check_output(["mpiexec", "-n", "8", "./proiect", inputName, "outputScript.out"])
	resultsMPI2.append(float(mpiTime2))
	
	javaTime = subprocess.check_output(["/mnt/c/Users/Breje/.jdks/openjdk-15.0.1/bin/java.exe", "-jar", "./proiect.jar", inputName])
	resultsMt.append(float(javaTime))

totalMilisMPI6=0
for x in resultsMPI2:
	totalMilisMPI6 += float(x)
averageMPI2 = totalMilisMPI6 / numberOfTests

totalMilisMPI=0
for x in resultsMPI:
	totalMilisMPI += float(x)
averageMPI = totalMilisMPI / numberOfTests

totalMilisJava=0
for x in resultsMt:
	totalMilisJava += float(x)
averageJava = totalMilisJava / numberOfTests

with open("./benchMarkResult", "a+") as f:
	f.write("For {} iterations on {} vertices and {} edges:\n\tMPI 8 nodes average milliseconds={}\n\tMPI 6 nodes average milliseconds={}\n\tMultithreaded average milliseconds={}\n\n".format(numberOfTests, vertices, edges, averageMPI2, averageMPI, averageJava))





