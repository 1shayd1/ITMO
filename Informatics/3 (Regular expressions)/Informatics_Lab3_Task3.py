#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  14.10.25
#Task: write the check for the correctness of the Cron-expression (length 5).

import re
def checkQueue(queue):
    queue = [int(queue[i]) for i in range(len(queue))]
    for i in range(1, len(queue)):
        if int(queue[i-1]) > int(queue[i]):
            return 0
    return 1
def rightCronExpression(expression):
    expression += " "
    minutes = "([1-9]|[1-5][0-9])"
    mn = rf"(\* |(0|{minutes}) |\*/{minutes} |(0|{minutes})-{minutes}/{minutes} |{minutes}/{minutes} |(0|{minutes})-{minutes} |(0|{minutes})(,{minutes})* )"
    hours = "([1-9]|1[0-9]|2[0-3])"
    hr = rf"(\* |(0|{hours}) |\*/{hours} |(0|{hours})-{hours}/{hours} |{hours}/{hours} |(0|{hours})-{hours} |(0|{hours})(,{hours})* )"
    days = "([1-9]|[1-2][0-9]|3[0-1])"
    ds = rf"(\* |{days} |\*/{days} |{days}-{days}/{days} |{days}/{days} |{days}-{days} |{days}(,{days})* )"
    months = "([1-9]|1[1-2])"
    ms = rf"(\* |{months} |\*/{months} |{months}-{months}/{months} |{months}/{months} |{months}-{months} |{months}(,{months})* )"
    weekdays = "[0-6]"
    ws = rf"(\* |{weekdays} |\*/{weekdays} |{weekdays}-{weekdays}/{weekdays} |{weekdays}/{weekdays} |{weekdays}-{weekdays} |{weekdays}(,{weekdays})* )"
    if re.fullmatch(mn+hr+ds+ms+ws, expression) == None:
        return "It's wrong"
    expression = expression[:-1]
    for i in re.split(" ", expression):
        if '/' in i:
            if '-' in i:
                expr = re.sub("-", "/", i).split('/')
                if int(expr[0]) > int(expr[1]):
                    return "It's wrong"
            else: continue
        elif '-' in i and int(re.split('-', i)[0]) > int(re.split('-',i)[1]):
            return "It's wrong"
        elif ',' in i:
            if checkQueue(re.split(',', i)) == 0:
                return "It's wrong"
    return "U r right"

test1 = "0-30/5 9-17 10,14,16,17 * 1-5"
test2 = "0-12 9-17 10,18,16,17 * *"
test3 = "0/30/5 13 14 * 0-6"
test4 = "*/5 13 14 * 6"
test5 = "12-24/12 5,6,7,8,9,10 14 * 7"

tests = [test1, test2, test3, test4, test5]
for i in tests:
    print(rightCronExpression(i))