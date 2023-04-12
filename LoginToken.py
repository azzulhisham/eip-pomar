import sys
import base64
import psycopg2     #pip install psycopg2
import datetime
import jwt          #pip install pyjwt


#response object
objPayload = {
    "name": "",
    "email": "",
    "tenant": "",
    "accessToken": "",
    "role": "",
    "expire": "",
    "error": ""
}

#module functions blocks
#access db
def getUserDetail(password):
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
        query = 'SELECT "users"."UserName","users"."EmailAddress","users"."Password","tenants"."TenancyName","roles"."Name" as "UserRole" \n'
        query += 'FROM users \n'
        query += 'JOIN tenants on "tenants"."Id" = "users"."TenantId" \n'
        query += 'JOIN user_roles on "user_roles"."TenantId" = "tenants"."Id" and "user_roles"."UserId" = "users"."Id" \n'
        query += 'JOIN roles on "roles"."Id" = "user_roles"."Id" \n'
        query += 'WHERE "users"."IsDeleted" = false AND "tenants"."IsDeleted" = false AND "roles"."IsDeleted" = false \n'
        query += 'AND "users"."IsActive" = true AND "tenants"."IsActive" = true \n'
        query += 'AND ("users"."EmailAddress" = \'{0}\' OR "users"."UserName" = \'{0}\') AND "users"."Password" = \'{1}\' AND "tenants"."TenancyName" = \'{2}\';'

        pg_cursor.execute(query.format(objPayload['email'], password, objPayload['tenant']))

        #query result
        resultCount = pg_cursor.rowcount

        if (resultCount > 0):
            now = datetime.datetime.now()
            dt = datetime.timedelta(365)

            for row in pg_cursor:
                objPayload['name'] = row[0]
                objPayload['role'] = row[4]
                objPayload['expire'] = str(now + dt)

        pg_conn.close()
    except:
        resultCount = -1
        pg_conn.close()

    return resultCount



# to get the command line arguments
argv = (sys.argv)

if (len(argv) > 1):
    try:
        objPayload['email'] = argv[1]
        password = argv[2]
        objPayload['tenant'] = argv[3]

        # encode password to base64
        enc = base64.b64encode(bytes(password, 'utf-8'))
        # convert bytes to string
        password = enc.decode('utf-8')	

        ret = getUserDetail(password)

        if (ret > 0):
            tokenPayload = {
                "name": "",
                "email": "",
                "tenant": "",
                "role": "",
                "expire": ""
            }

            tokenPayload['name'] = objPayload['name']
            tokenPayload['email'] = objPayload['email']
            tokenPayload['tenant'] = objPayload['tenant']
            tokenPayload['role'] = objPayload['role']
            tokenPayload['expire'] = objPayload['expire']

            jwtToken = jwt.encode(tokenPayload, "pinc.my", algorithm="HS256")
            objPayload['accessToken'] = jwtToken

        elif(ret == 0):
            objPayload['error'] = 'User not found!'
        else:
            objPayload['error'] = 'Database Error!'

    except:
        objPayload['error'] = 'Invalid Request!'

else:
    objPayload['error'] = 'Invalid Request!'


print(objPayload)


#SELECT "Users"."UserName","Users"."EmailAddress","Users"."Password","Tenants"."TenancyName","Roles"."Name" as "UserRole"
#FROM public."Users"
#JOIN public."Tenants" on "Tenants"."Id" = "Users"."TenantId"
#JOIN public."UserRoles" on "UserRoles"."TenantId" = "Tenants"."Id" and "UserRoles"."UserId" = "Users"."Id"
#JOIN public."Roles" on "Roles"."Id" = "UserRoles"."Id"
#WHERE "Users"."IsDeleted" = false AND "Tenants"."IsDeleted" = false AND "Roles"."IsDeleted" = false 
#AND "Users"."IsActive" = true AND "Tenants"."IsActive" = true 
#AND ("Users"."EmailAddress" = 'cgiis_user' OR "Users"."UserName" = 'cgiis_user') AND "Users"."Password" = 'Q0dpaXMyMDIyMDQk' AND "Tenants"."TenancyName" = 'admin'


