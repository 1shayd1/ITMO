#Task 3

#1
#Рекурсивно нельзя копировать папку в саму себя, поэтому написал все вручную
mkdir lab0/globat0/wynaut/globat0
mkdir lab0/globat0/wynaut/globat0/{ursaring,wynaut}
cp lab0/globat0/jynx lab0/globat0/wynaut/globat0
chmod 444 lab0/globat0/wynaut/globat0/jynx
chmod 337 lab0/globat0/wynaut/globat0/ursaring
chmod 771 lab0/globat0/wynaut/globat0/wynaut

#2
ln -s lab0/globat0 lab0/Copy_5

#3
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
cat lab0/munna4/magcargo lab0/vulpix6/feebas > lab0/jynx2_48
chmod u=wx,g=wx,o=wx lab0/vulpix6

#4
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
ln -s lab0/dewgong5 lab0/vulpix6/excadrilldewgong
chmod u=wx,g=wx,o=wx lab0/vulpix6

#5
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
#tranquill отказывает в изменении состояния каталога, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
chmod 777 lab0/vulpix6/tranquill
cp lab0/jynx2 lab0/vulpix6/tranquill
chmod u=wx,g=wx,o=wx lab0/vulpix6
chmod 571 lab0/vulpix6/tranquill

#6
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
cat lab0/jynx2 > lab0/vulpix6/infernapejynx
chmod u=wx,g=wx,o=wx lab0/vulpix6

#7
#munna4 отказывает в изменении состояния каталога, поэтому были изменены права доступа
chmod 777 lab0/munna4
ln lab0/jynx2 lab0/munna4/magcargojynx
chmod 571 lab0/munna4

