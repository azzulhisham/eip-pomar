import sys
import base64
import datetime
import jwt


validation = {
    "result": "",
    "message": ""
}

# to get the command line arguments
argv = (sys.argv)

if (len(argv) > 1):
    token = argv[1]
    tokenEmptySpaceIndex = token.index(' ')
    strBearer = token[0:tokenEmptySpaceIndex].lower()
    strToken = token[tokenEmptySpaceIndex:len(token)].strip()

    if (strBearer == 'bearer'):
        try:
            objJwt = (jwt.decode(strToken, "pinc.my", algorithms=["HS256"]))
            now = datetime.datetime.now()
            #dt = datetime.timedelta(300)
            expire = datetime.datetime.strptime(objJwt['expire'], "%Y-%m-%d %H:%M:%S.%f")

            if (expire < now):
                validation['result'] = 'false'
                validation['message'] = 'Token expired!'
            else:
                validation['result'] = 'true'
                validation['message'] = ''

        except:
            validation['result'] = 'false'
            validation['message'] = 'Token not valid!'
    else:
        validation['result'] = 'false'
        validation['message'] = 'Token not valid!'

else:
    validation['result'] = 'false'
    validation['message'] = 'Token not exists!'


print(validation)