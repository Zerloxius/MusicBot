@echo off
:: Change to the script's directory
cd /d "%~dp0"

:: Activate the virtual environment
call venv\Scripts\activate
python potoken-generator.py --oneshot
pause
deactivate