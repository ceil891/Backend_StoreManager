import psycopg2

DATABASE_URL = "postgresql://neondb_owner:npg_mtK9WClUfQ2u@ep-bold-rain-aoqv1tl8.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require"

try:
    print("Connecting to Neon PostgreSQL...")
    conn = psycopg2.connect(DATABASE_URL, connect_timeout=10)
    print("Connected successfully!")
    cur = conn.cursor()

    cur.execute("""
        SELECT column_name, data_type, is_nullable, column_default 
        FROM information_schema.columns 
        WHERE table_name = 'n8n_chat_histories_ric_qlbh';
    """)
    rows = cur.fetchall()
    print("Columns in n8n_chat_histories_ric_qlbh:")
    for r in rows:
        print(r)

    if rows:
        print("Altering table to SET DEFAULT 'message' on column type...")
        cur.execute('ALTER TABLE n8n_chat_histories_ric_qlbh ALTER COLUMN "type" SET DEFAULT \'message\';')
        cur.execute('ALTER TABLE n8n_chat_histories_ric_qlbh ALTER COLUMN "type" DROP NOT NULL;')
        conn.commit()
        print("SUCCESS: Successfully set DEFAULT 'message' and dropped NOT NULL on column type!")
    else:
        print("Table n8n_chat_histories_ric_qlbh not found in this database.")

    cur.close()
    conn.close()
except Exception as e:
    print("Error:", repr(e))
