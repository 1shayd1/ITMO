#Task 4

#1
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
mkdir lab0/tmp
chmod -R 777 lab0
chmod -R 777 lab0/vulpix6
chmod -R 777 lab0/munna4
chmod -R 777 lab0/globat0
wc -w lab0/globat0/jynx lab0/munna4/cherrim lab0/munna4/magcargo lab0/vulpix6/feebas >lab0/tmp/result 2>/tmp/erro_502467  
chmod u=wx,g=wx,o=wx lab0/vulpix6
chmod u=rx,g=x,o=w lab0/globat0
chmod 571 lab0/munna4

#2
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
#ursaring отказывает в доступе, поэтому были изменены права доступа
#swanna  отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
chmod 777 lab0/globat0/ursaring
chmod 777 lab0/munna4/swanna
chmod 777 lab0/globat0/wynaut/globat0/ursaring
ls -lR lab0 2>/dev/null | grep "l$"| grep "^-" | sort -k9
chmod u=wx,g=wx,o=wx lab0/vulpix6
chmod 337 lab0/globat0/ursaring
chmod 311 lab0/munna4/swanna
chmod 337 lab0/globat0/wynaut/globat0/ursaring

#3
#ursaring отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/globat0/ursaring
cat lab0/globat0/* 2>&1 | grep -n -i "h$" 
chmod 337 lab0/globat0/ursaring
#не было выведено ничего, потому что в единственном файле jynx каталога lab0 нет строк, заканчивающихся на "h"

#4
#jynx2 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/jynx2
wc -m lab0/jynx2 2>&1 >> lab0/jynx2
chmod 444 lab0/jynx2
#5
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
ls -lR lab0 2>/dev/null | grep "^[-].* z[^:/]*$"  | head -3 | sort -k5
chmod u=wx,g=wx,o=wx lab0/vulpix6
#выведен только 1 файл, потому что во всём каталоге lab0 содержится только 1 файл, начинающийся на "z"

#6
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
cat lab0/globat0/jynx lab0/munna4/cherrim lab0/munna4/magcargo lab0/vulpix6/feebas 2>lab0/tmp/erro_502467 | grep -v "k$"
chmod u=wx,g=wx,o=wx lab0/vulpix6

