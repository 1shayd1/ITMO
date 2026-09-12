#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  13.11.25

import time
def formatXml(obj, root="root", level=0):
    indent = "    " * level
    if isinstance(obj, dict):
        lines = [f"{indent}<{root}>"]
        for key, val in obj.items():
            lines.append(formatXml(val, key, level + 1))
        lines.append(f"{indent}</{root}>")
        return "\n".join(lines)

    elif isinstance(obj, list):
        lines = []
        for item in obj:
            lines.append(formatXml(item, root, level))
        return "\n".join(lines)

    else:
        if obj is None:
            content = ""
        elif isinstance(obj, bool):
            content = str(obj).lower()
        else:
            content = obj
        return f"{indent}<{root}>{content}</{root}>"
def getValueList(binText):
    text = str(binText)[2:-1].split("\\")  # turn to ordinary string
    text = text[1:]
    for i in range(len(text)):
        if len(text[i]) > 3:
            text[i] = text[i][3:]

    new = [text[0]]
    for i in range(1, len(text)):
        if len(text[i-1]) > 3 and len(text[i]) == 3 and len(text[i+1]) > 3:
            continue
        else:
            new += [text[i]]
    result = []
    i = 0
    while i+1 < len(new):
        if len(new[i]) == 3:
            result += [new[i][-1]]
            i += 1
        else:
            result += [new[i]]
            i += 1
    return result + [new[-1]]
def parse(tokens, i):
    if i > len(tokens):
        return 'Finished'

    token = tokens[i]

    if token == '6':  # dict
        i += 1
        n = int(tokens[i])
        obj = {}
        for _ in range(n):
            i += 1
            key, i = parse(tokens, i)
            i += 1
            value, i = parse(tokens, i)
            obj[key] = value
        return obj, i

    elif token == '5':  # list
        i += 1
        n = int(tokens[i])
        obj = []
        for _ in range(n):
            i += 1
            key, i = parse(tokens, i)
            obj += [key]
        return obj, i

    else:  # string
        return token, i
def binaryToXml(binText):
    tokens = getValueList(binText)
    i = 0
    return parse(tokens, i)[0]

start = time.perf_counter()

for i in range(100):
    with open('Schedule.bin', 'rb') as fileIn:
        with open('Schedule.xml', 'w') as fileOut:
            a = formatXml(binaryToXml(fileIn.read()))
            fileOut.write(a)

end = time.perf_counter()

print(f"Time of running: {end - start:.8f} seconds.")
#0.11600570