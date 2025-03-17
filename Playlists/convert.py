from pathlib import Path

for playlist in Path(__file__).parent.resolve().iterdir():
    if playlist.suffix != '.m3u': continue
    
    with open(playlist, 'r', encoding='utf-8', errors='ignore') as file:
        lines = file.readlines()
    lines.insert(0, '# shuffle\n')
    with open(playlist.with_suffix('.txt'), 'w', encoding='utf-8') as file:
        file.writelines(lines)