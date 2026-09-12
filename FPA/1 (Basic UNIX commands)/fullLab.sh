#Task 1

#Создаём директории и файлы в них
mkdir -p lab0/{globat0/{ursaring,wynaut},munna4/swanna,vulpix6/{zweilous,tranquill}} && touch lab0/dewgong5 lab0/globat0/jynx lab0/jynx2 lab0/munna4/{cherrim,magcargo} lab0/vulpix6/{feebas,excadrill,infernape} lab0/zorua7

#Записываем информацию в файлы
echo "satk=7 sdef=10 spd=7" > lab0/dewgong5
echo "Живет  Taiga Tundra
Urban" > lab0/globat0/jynx
echo "Ходы  Body Slam Counter Covet Double-Edge Drain Punch
Dynamicpunch Focus Punch Heal Bell Helping Hand Hyper Voice Ice Punch
Icy Wind Magic Coat Magic Room Mega Kick Mega Punch Metronome Mud-Slap
Recycle Role Play Seismic Toss Signal Beam Skill Swap Sleep Talk Snore
Trick Uproar Water Pulse Zen Headbutt" > lab0/jynx2
echo "weigth=20.5 height=20.0
atk=6 def=7" > lab0/munna4/cherrim
echo "weigth=121.3 height=31.0 atk=5
def=12" > lab0/munna4/magcargo
echo "Развитые способности  Adaptability" > lab0/vulpix6/feebas
echo "Ходы
Drill Run Earth Power Iron Defense Iron Head Magnet Rise Sleep Talk
Snore Stealth Rock" > lab0/vulpix6/excadrill
echo "Тип покемона  FIRE
FIGHTING" > lab0/vulpix6/infernape
echo "weigth=27.6 height=28.0 atk=7 def=4" > lab0/zorua7

#Task 2

#Расставляем права доступа
chmod u=rw,g=w,o= lab0/dewgong5
chmod u=rx,g=x,o=w lab0/globat0
chmod 444 lab0/globat0/jynx
chmod 337 lab0/globat0/ursaring
chmod 771 lab0/globat0/wynaut
chmod 444 lab0/jynx2
chmod 571 lab0/munna4
chmod 066 lab0/munna4/cherrim
chmod u=r,g=r,o=r lab0/munna4/magcargo
chmod 311 lab0/munna4/swanna
chmod u=wx,g=wx,o=wx lab0/vulpix6
chmod u=rx,g=wx,o=rwx lab0/vulpix6/zweilous
chmod u=rw,g=w,o= lab0/vulpix6/feebas
chmod 044 lab0/vulpix6/excadrill
chmod 640 lab0/vulpix6/infernape
chmod 571 lab0/vulpix6/tranquill
chmod u=rw,g=w,o=w lab0/zorua7

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

#Task 5
#vulpix6 отказывает в доступе, поэтому были изменены права доступа
#jynx отказывает в доступе, поэтому были изменены права доступа
#globat0 отказывает в доступе, поэтому были изменены права доступа
#munna4 отказывает в доступе, поэтому были изменены права доступа
#magcargojyn* отказывает в доступе, поэтому были изменены права доступа
chmod 777 lab0/vulpix6
chmod -R 777 lab0/globat0
chmod -R 777 lab0/munna4
rm lab0/zorua7
rm lab0/vulpix6/infernape
rm -r lab0/Copy*
rm -r lab0/munna4/magcargojy*
rm -rf lab0/globat0
rmdir lab0/vulpix6/zweilous
chmod u=wx,g=wx,o=wx lab0/vulpix6
chmod 571 lab0/munna4
chmod 066 lab0/munna4/cherrim
chmod u=r,g=r,o=r lab0/munna4/magcargo
chmod 311 lab0/munna4/swanna
