from pathlib import Path

for f in [f for f in Path(__file__).parent.resolve().iterdir() if f.suffix == '.m3u']:
    with open(f, 'r', encoding='utf-8', errors='ignore') as file:
        lines = file.readlines()
    lines.insert(0, '# shuffle\n')
    with open(f.with_suffix('.txt'), 'w', encoding='utf-8') as file:
        file.writelines(lines)