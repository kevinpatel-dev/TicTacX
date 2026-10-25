import sqlite3
from django.shortcuts import render, redirect
from django.conf import settings

def home(request):
    db_path = settings.DATABASES['default']['NAME']
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT id, player_name, game_mode, result, played_at FROM matches ORDER BY id DESC")
    matches = cursor.fetchall()
    conn.close()

    context = {
        'matches': matches
    }
    return render(request, 'dashboard/index.html', context)

def reset_data(request):
    if request.method == "POST":
        db_path = settings.DATABASES['default']['NAME']
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        # Delete all rows and reset the ID counter back to 1
        cursor.execute("DELETE FROM matches")
        cursor.execute("DELETE FROM sqlite_sequence WHERE name='matches'")
        conn.commit()
        conn.close()
    return redirect('home')