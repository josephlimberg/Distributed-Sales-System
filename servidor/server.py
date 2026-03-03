import os
from flask import Flask, request, jsonify
import mysql.connector
import random
import datetime
from twilio.rest import Client

app = Flask(__name__)

# Función para obtener la conexión a la BD por cada petición (Práctica real)
def get_db_connection():
    return mysql.connector.connect(
        host=os.environ.get("DB_CLOUD_HOST", "localhost"),
        user=os.environ.get("DB_CLOUD_USER", "usuario_nube"),
        password=os.environ.get("DB_CLOUD_PASS", "password_nube"),
        database=os.environ.get("DB_CLOUD_NAME", "grupoJoseph_cloud")
    )

def enviar_sms(telefono, token):
    # En producción real, estas variables vienen de .env
    account_sid = os.environ.get('TWILIO_SID')
    auth_token = os.environ.get('TWILIO_TOKEN')
    twilio_phone = os.environ.get('TWILIO_PHONE')
    
    if account_sid and auth_token:
        try:
            client = Client(account_sid, auth_token)
            client.messages.create(
                body=f"Multimarkets: Su token de autorizacion es {token}. Expira en 5 minutos.",
                from_=twilio_phone,
                to=telefono
            )
        except Exception as e:
            print(f"Error enviando SMS real: {e}")
    else:
        print(f"[SIMULACION SMS] - TOKEN {token} enviado a {telefono}")

@app.route('/generar_voucher', methods=['POST'])
def generar_voucher():
    data = request.json
    db = get_db_connection()
    cursor = db.cursor()
    
    try:
        token = str(random.randint(100000, 999999))
        expiracion = datetime.datetime.now() + datetime.timedelta(minutes=5)
        
        query = """
            INSERT INTO cabecera_venta 
            (id_tienda, fecha, total, id_cliente, token, token_expiracion) 
            VALUES (%s, NOW(), %s, %s, %s, %s)
        """
        values = (data['id_tienda'], data['total'], data['id_cliente'], token, expiracion)
        cursor.execute(query, values)
        id_venta = cursor.lastrowid
        db.commit()
        
        enviar_sms(data['telefono_cliente'], token)
        
        return jsonify({'id_venta': id_venta, 'mensaje': 'Voucher y token generados'})
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        cursor.close()
        db.close()

@app.route('/validar_token', methods=['POST'])
def validar_token():
    data = request.json
    db = get_db_connection()
    cursor = db.cursor(dictionary=True)
    
    try:
        query = "SELECT token_expiracion, token_validado FROM cabecera_venta WHERE id_venta = %s AND token = %s"
        cursor.execute(query, (data['id_venta'], data['token']))
        result = cursor.fetchone()
        
        if not result:
            return jsonify({'valido': False, 'error': 'Token incorrecto.'})
            
        if result['token_validado']:
            return jsonify({'valido': False, 'error': 'El token ya fue utilizado.'})
        
        if datetime.datetime.now() > result['token_expiracion']:
            return jsonify({'valido': False, 'error': 'Token expirado. Solicite uno nuevo.'})
        
        update_query = "UPDATE cabecera_venta SET token_validado = 1 WHERE id_venta = %s"
        cursor.execute(update_query, (data['id_venta'],))
        db.commit()
        
        return jsonify({'valido': True})
    finally:
        cursor.close()
        db.close()

# Endpoint extra para manejar el escenario de fallo: "Fallo en el envío del SMS"
@app.route('/reenviar_token', methods=['POST'])
def reenviar_token():
    data = request.json
    db = get_db_connection()
    cursor = db.cursor()
    try:
        nuevo_token = str(random.randint(100000, 999999))
        nueva_expiracion = datetime.datetime.now() + datetime.timedelta(minutes=5)
        
        query = "UPDATE cabecera_venta SET token = %s, token_expiracion = %s WHERE id_venta = %s"
        cursor.execute(query, (nuevo_token, nueva_expiracion, data['id_venta']))
        db.commit()
        
        enviar_sms(data['telefono_cliente'], nuevo_token)
        return jsonify({'mensaje': 'Token reenviado con éxito'})
    finally:
        cursor.close()
        db.close()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)