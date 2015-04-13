javac : jj
	javac *.java

jj : jjt
	javacc parser.jj

jjt :
	jjtree parser.jjt


clean :
	rm *.java *.class *.jj
