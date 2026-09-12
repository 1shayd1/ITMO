#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  14.10.25
#Task: check if the row is Haiku -> (5 syllables/7 syllables/5 syllables).

import re
consontants = "йцкнгшщзхъфвпрлджчсмтьбЙЦКНГШЩЗХЪФВПРЛДЖЧСМТЬБ"
vowels = "ёуеыаояию"
def checkHaiku(test):
    if test.count("/") != 2:
        return "Не хайку. Должно быть 3 строки."
    result = re.sub(f'[{consontants} !.?:,]', '', test)
    if re.fullmatch(f"[{vowels}]{{5}}/[{vowels}]{{7}}/[{vowels}]{{5}}", result, re.IGNORECASE):
        return "Хайку!"
    else:
        return "Не хайку("

test1 = "Вот плыла луна / И самый мелкий кустик / На праздник приглашён."
test2 = "Старый пруд. / Прыгнула в воду лягушка. / Всплеск в тишине. / Гром среди неба"
test3 = "Осенний ветер, / Листья кружатся в лесу, / Зима у дверей."
test4 = "Мой компьютер гудит, / Я пишу код до ночи,"
test5 = "Вишни лепестки / Опадают на землю, / Розовый ковер."
tests = [test1, test2, test3, test4, test5]
for i in tests:
    print(checkHaiku(i))