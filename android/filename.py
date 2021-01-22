import os

directory = r'app/src/main/assets/archive/'
cnt = 0
for filename in os.listdir(directory):
    cnt = cnt+1
    if filename.endswith(".png"):
        os.rename(os.path.join(directory, filename), os.path.join(directory, 'object' + str(cnt) + '.png'))
    else:
        continue
