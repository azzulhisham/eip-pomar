import json
import sys
import psycopg2
from pyais import *


#access db
def getPositionByMMSI(mmsi):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'select * from ais_position where mmsi={0} '
       
        pg_cursor.execute(query.format(mmsi))

        #query result
        resultCount = pg_cursor.rowcount

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount


def getStaticByMMSI(mmsi):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'select * from ais_static where mmsi={0} '
       
        pg_cursor.execute(query.format(mmsi))

        #query result
        resultCount = pg_cursor.rowcount

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount


def insertPositionData(posData):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'INSERT INTO ais_position (mmsi, heading, raim, accuracy, lon, turn, speed, maneuver, second, radio, repeat, msg_type, course, lat, spare_1, status, updated_on) values ' 
        query += '({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, \'{14}\', {15}, now())'

        pg_cursor.execute(query.format(posData['mmsi'], posData['heading'], posData['raim'], posData['accuracy'], posData['lon'], posData['turn'], posData['speed'], posData['maneuver'], posData['second'], posData['radio'], posData['repeat'], posData['msg_type'], posData['course'], posData['lat'], posData['spare_1'], posData['status']))

        #query result
        resultCount = pg_cursor.rowcount

        pg_conn.commit()

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount


def insertStaticData(shipData):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'INSERT INTO ais_static (mmsi, epfd, destination, to_port, imo, ship_type, ais_version, minute, month, hour, draught, dte, repeat, callsign, msg_type, to_bow, to_starboard, day, shipname, spare_1, to_stern, updated_on) values ' 
        query += '({0}, {1}, \'{2}\', {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, \'{13}\', {14}, {15}, {16}, {17}, \'{18}\', \'{19}\', {20}, now())'

        pg_cursor.execute(query.format(shipData['mmsi'], shipData['epfd'], shipData['destination'], shipData['to_port'], shipData['imo'], shipData['ship_type'], shipData['ais_version'], shipData['minute'], shipData['month'], shipData['hour'], shipData['draught'], shipData['dte'], shipData['repeat'], shipData['callsign'], shipData['msg_type'], shipData['to_bow'], shipData['to_starboard'], shipData['day'], shipData['shipname'], shipData['spare_1'], shipData['to_stern']))

        #query result
        resultCount = pg_cursor.rowcount

        pg_conn.commit()

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount


def updatePositionData(posData):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'UPDATE ais_position SET heading={0}, raim={1}, accuracy={2}, lon={3}, turn={4}, speed={5}, ' 
        query += 'maneuver={6}, second={7}, radio={8}, repeat={9}, course={10}, lat={11}, spare_1=\'{12}\', status={13}, updated_on=now() ' 
        query += 'WHERE mmsi={14}'

        pg_cursor.execute(query.format(posData['heading'], posData['raim'], posData['accuracy'], posData['lon'], posData['turn'], posData['speed'], posData['maneuver'], posData['second'], posData['radio'], posData['repeat'], posData['course'], posData['lat'], posData['spare_1'], posData['status'], posData['mmsi']))

        #query result
        resultCount = pg_cursor.rowcount

        pg_conn.commit()

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount

def updateStaticData(shipData):
    resultCount = 0

    try:
        # connect to Postgres DB
        pg_conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="ais_secure",
            user="postgres",
            password="postgres")

        pg_cursor = pg_conn.cursor()

        #query
        query = 'UPDATE ais_static SET epfd={0}, destination=\'{1}\', to_port={2}, imo={3}, ship_type={4}, ais_version={5}, minute={6}, month={7}, hour={8}, ' 
        query += 'draught={9}, dte={10}, repeat={11}, callsign=\'{12}\', to_bow={13}, to_starboard={14}, day={15}, shipname=\'{16}\', spare_1=\'{17}\', to_stern={18}, updated_on=now() ' 
        query += 'WHERE mmsi={19}'

        pg_cursor.execute(query.format(shipData['epfd'], shipData['destination'], shipData['to_port'], shipData['imo'], shipData['ship_type'], shipData['ais_version'], shipData['minute'], shipData['month'], shipData['hour'], shipData['draught'], shipData['dte'], shipData['repeat'], shipData['callsign'], shipData['to_bow'], shipData['to_starboard'], shipData['day'], shipData['shipname'], shipData['spare_1'], shipData['to_stern'], shipData['mmsi']))

        #query result
        resultCount = pg_cursor.rowcount

        pg_conn.commit()

        pg_cursor.close()
        pg_conn.close()
    except:
        resultCount = -1

        pg_cursor.close()
        pg_conn.close()

    return resultCount


# to get the command line arguments
argv = (sys.argv)

if (len(argv) > 1):
    aisMsg = argv[1]

    if(len(argv) >= 3):
        # # Decode a multipart message using decode
        #"!NMVDM,2,1,9,A,57tLB;`00003Tm8?P00dn1@DpLLth0000000000o3`006t0Ht00000000000,0*78",
        #"!NMVDM,2,2,9,A,00000000000,2*26",
        parts = [
            argv[1],
            argv[2]
        ]

        decoded = decode(*parts)
        shipDet = json.loads(decoded.to_json())

        print(shipDet)
        rec = getStaticByMMSI(shipDet['mmsi'])

        if shipDet['msg_type'] == 5 :
            if rec <= 0 :
                print(insertStaticData(shipDet))
            else:
                print(updateStaticData(shipDet))
    else:
        ##!NMVDM,1,1,8,A,37tCid@P00W;6>v31M@>4?w<2000,0*5A
        decoded = decode(aisMsg)
        posData = json.loads(decoded.to_json())

        print(posData)
        rec = getPositionByMMSI(posData['mmsi'])

        if posData['msg_type'] == 3 :
            if rec <= 0 :
                print(insertPositionData(posData))
            else:
                print(updatePositionData(posData))
            


