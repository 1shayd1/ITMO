#Author = Shalabodov Yaroslav Dmitrievich
#Group = P3110
#Date =  08.11.25

import time
def parseValue(value):
    """Парсит значение и возвращает (тип, данные)"""
    value = value.strip()

    if value == 'true': return (1, b'\x01')                                                        # bool true
    if value == 'false': return (1, b'\x00')                                                       # bool false
    if value.isdigit(): return (2, int(value).to_bytes(1, 'big', signed=True))     # int
    if value.replace('.', '').replace('-', '').isdigit():                                          # float
        return (3, int(float(value)).to_bytes(1, 'big', signed=True))
                                                                                                   # Строка (убираем кавычки)
    if (value.startswith('"') and value.endswith('"')) or \
        (value.startswith("'") and value.endswith("'")):
        value = value[1:-1].strip()
    encoded = value.encode('utf-8')
    return (4, len(encoded).to_bytes(1, 'big') + encoded)
def listLength(t, gaps, gapI):
    s = 0
    start = 0
    for i in range(len(gapI)):
        if gapI[i] == t:
            start = i
            break
    ethalon = gaps[start]
    while start < len(gaps):
        if gaps[start] < ethalon:
            break
        if gaps[start] == ethalon:
            s += 1
        start += 1
    return s
def dictLength(t, gaps, gapI):
    s = 1
    start = 0
    for i in range(len(gapI)):
        if gapI[i] == t:
            start = i
            break
    ethalon = gaps[start]+2
    while start+1 < len(gaps):
        if gaps[start+1] == ethalon:
            s += 1
        else:
            break
        start += 1
    return s
def yamlToBinary(yamlFile):
    lines = yamlFile.split('\n')
    binary_data = bytearray()
    i = 0
    binary_data.extend(b'\x06')
    binary_data.extend(b'\x01')
    listGaps = [len(lines[j]) - len(lines[j].lstrip()) for j in range(len(lines)) if lines[j].lstrip().startswith('-')]
    listGapIndexes = [j for j in range(len(lines)) if lines[j].lstrip().startswith('-')]
    dictGaps = [len(lines[j]) - len(lines[j].lstrip()) for j in range(len(lines))]
    dictGapIndexes = [j for j in range(len(lines))]


    while i < len(lines):

        line = lines[i].lstrip()
        if not line or line.startswith('#'):
            i += 1
            continue

        if line.lstrip().startswith('-'):
            binary_data.extend(b'\x06')
            binary_data.extend(dictLength(i, dictGaps, dictGapIndexes).to_bytes(1, 'big', signed=True))

            key, value = line.split(': ', 1)
            value = value.strip()
            binary_data.extend(len(key[2:]).to_bytes(1, 'big', signed=True))
            binary_data.extend(key[2:].encode('utf-8'))

            type, val = parseValue(value)
            binary_data.extend(type.to_bytes(1, 'big', signed=True))
            binary_data.extend(val)
            i += 1
            continue

        key, value = line.split(':', 1)
        binary_data.extend(len(key).to_bytes(1, 'big', signed = True))
        binary_data.extend(key.encode('utf-8'))

        if not value:
            #если массив, то в нем словари
            if lines[i+1].lstrip().startswith('-'):
                binary_data.extend(b'\x05')
                i += 1
                binary_data.extend(listLength(i, listGaps, listGapIndexes).to_bytes(1, 'big', signed=True))
            #если нет массива и после ":" ничего нет -> словарь
            else:
                binary_data.extend(b'\x06')
                i += 1
                binary_data.extend(dictLength(i, dictGaps, dictGapIndexes).to_bytes(1, 'big', signed=True))
            continue

        else:
            type, val = parseValue(value)
            binary_data.extend(type.to_bytes(1, 'big', signed = True))
            binary_data.extend(val)

        i += 1

    return binary_data

start = time.perf_counter()

for i in range(100):
    with open('Schedule.yaml', 'r', encoding='utf-8') as fileIn:
        with open('Schedule.bin', 'wb') as fileOut:
            show = yamlToBinary(fileIn.read())
            fileOut.write(show)

end = time.perf_counter()

print(f"Time of running: {end - start:.8f} seconds.")
#0.11516850