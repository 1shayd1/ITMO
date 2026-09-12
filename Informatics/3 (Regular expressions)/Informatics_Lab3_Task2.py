#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  14.10.25
#Task: replace all numbers with function f(x) = 5*x**3 - 13 of them.

import re
def replaceNumber(num):
    return str(5 * (int(num))**3 - 13)

def solveTheEquation(string):
    pattern = re.findall("-?[0-9]+" , string)
    if len(list(pattern)) == 0:
        return "No numbers here!"
    equation = re.split("-?[0-9]+", string)
    codedNums = [replaceNumber(i) for i in list(pattern)]
    for i in range(len(equation)-2):
        print(equation[i] + codedNums[i], end = '')
    return equation[-2] + codedNums[-1]

test1 = "15 + 22 = 37"
test2 = "Hello 123 World -45 test 0"
test3 = "Numbers: 1, 2, 3, 10, 100"
test4 = "No numbers here!"
test5 = "-5 * 8 = -40"

tests = [test1, test2, test3, test4, test5]
for i in tests:
    print(i)
    print(solveTheEquation(i))
    print()