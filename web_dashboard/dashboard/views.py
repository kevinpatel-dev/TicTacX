import sqlite3
from django.shortcuts import render
from django.conf import settings

def home(request):
    db_path = settings.DATABASES['default']['NAME']
    
    # Query match records saved by Java
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT id, player_name, game_mode, result, played_at FROM matches ORDER BY id DESC")
    matches = cursor.fetchall()
    conn.close()

    context = {
        'project_title': 'TicTacX — Advanced Game Dashboard',
        'matches': matches
    }
    return render(request, 'dashboard/index.html', context)