name = parser


javac : jj
	javac *.java

jj : jjt
	javacc $(name).jj

jjt :
	jjtree $(name).jjt


clean :
	rm *.java *.class *.jj
