import sys
import os
import subprocess

inputName = sys.argv[1]
maxEdge = sys.argv[2]
minEdge = sys.argv[3]
vertices = sys.argv[4]

with open("tempArgs", "w") as f_args:
	f_args.write("{}\n1\n2\n2\n{}\n{}\n1\n2\n1\n{}\nt\nt".format(inputName, maxEdge, minEdge, vertices))

os.system("python3 -m pyrgg < tempArgs")
#subprocess.call(["python3", "-m", "pyrgg", "< tempArgs"])

fila = open(inputName + ".gr", "r")
allLines = fila.readlines()
fila.close()

fila = open(inputName + ".gr", "w")

allLines = allLines[7:]
firstLine = 0
for line in allLines:
	tokensi = line.split(" ")
	if firstLine == 0:
		fila.write("p edge {} {}".format(tokensi[2], tokensi[3]))
		print(tokensi[3])
		firstLine = 1
	else:
		fila.write("e {} {}\n".format(tokensi[1], tokensi[2]))
fila.close()
