# AuthAPI

AuthAPI is a general purpose account information API written in java, based on Spring.
It's main features are:

  - Creating accounts with secure passwords.
  - Generating secure login tokens.
  - E-mail account verifitcaion.
  - Retrieving account information based on tokens.

The general usage of this API is the ability to have a secure, solid SSO system.

# Installation:

Download the latest build [here.](https://bitbucket.org/Noviakaz/authapi-service/downloads/)
AuthAPI uses Java (JRE) 8. You can download the latest version of Java 8 on [Oracle website.](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

1. Extract the downloaded files to any folder using [WinRar](https://www.winrar.nl/)
2. Excecute the provided accounts.sql on your SQL server.
3. Edit database.properties to match your database configuration.
4. Run the application using launch.bat.
5. The application will close telling you to edit the newly generated config.ini.
6. Edit config.ini to your liking and run the application again.
7. Verify the application is running by going to http://localhost:8080

# Overview of API links

Dillinger is currently extended with the following plugins. Instructions on how to use them in your own application are linked below.

| Link | Function | Parameters | Example |
| ------ | ------ | ------ | ------ |
| /create | Create a new account.  | email, name, password, birthday, gender | http://localhost:8080/create?email=example@auth.apil&name=examplek&password=example&birthday=01011800&gender=0
| /login | Creates a token for a given account | name, password | http://localhost:8080/login?name=example&password=example
| /account | Returns all the information on a requested account. | token | http://localhost:8080/account?token=ABCDEFG
| /verify | Verifies a new account based on the code sent to the account's e-mail address. | email, code | http://localhost:8080/verify?email=example@auth.api&code=1234

# /create

Creates a new Account by specified parameters. A new account can only be (succesfully) created once every 15 minutes using the same IP. This to prevent spamming. If account creation is successful a verification code will be sent to the provided email address.

**URL EXAMPLE**
```
http://localhost:8080/create?email=example@auth.apil&name=examplek&password=example&birthday=01011800&gender=0
```
**PARAMETERS**
| Parameter | Value | Optional |
| ------ | ------ | ------ |
| email | A valid email address in the format name@service.com. | no
| name | UTF-8 formatted string between 5-13 characters long. | no
| password | UTF-8 formatted string between 5-13 characters long. | no
| birthday | A valid date formatted as ddmmyyyy with minimum being 01-01-1800 and year maximum 9999. | no
| gender | A valid number that's either 0 (male) or 1 (female) | no

**RESPONSE FORMAT**
```JSON
{
  "value": 1,
  "response": ""
}
```
| Parameter | Type | Content
| ------ | ------ | ------ |
| value | number | Response code with possibilities: 0 = Unknown failure, 1 = Banned account, 2 = Blocked account, 3 = Success, 4 = Service unavailable at the time, 5 = Incorrect account information, 6 = Not yet verified account.
| response | string | Message further explaining the returned value.

# /login

Logs in an account if the provided details are correct and creates returns a unique token that's valid for 15 minutes.

**URL EXAMPLE**
```
http://localhost:8080/login?name=example&password=example
```
**PARAMETERS**
| Parameter | Value | Optional |
| ------ | ------ | ------ |
| name | UTF-8 formatted string between 5-13 characters long. (Account name or email) | no
| password | UTF-8 formatted string between 5-13 characters long. | no

**RESPONSE FORMAT**
```JSON
{
  "response": 1,
  "name": "example",
  "token": "ABCDEFG"
}
```
| Parameter | Type | Content
| ------ | ------ | ------ |
| response | number | Response code with possibilities: 0 = Success, 1 = Failed, 2 = Blocked since 15 minutes between account creation has not yet passed, 3 = Account already exists but hasn't yet been verified.
| name | string | Account name (not email).
| token | string | Uniquely generated token associated with logged in account.

# /account

Retreives account information by token if the account is logged in and the token is (still) valid.

**URL EXAMPLE**
```
http://localhost:8080/account?token=ABCDEFG
```
**PARAMETERS**
| Parameter | Value | Optional |
| ------ | ------ | ------ |
| token | any string | yes

**RESPONSE FORMAT**
```JSON
{
  "id": 0,
  "verified": true,
  "name": "example",
  "token": "ABCDEF",
  "email": "example@auth.api",
  "ip": "127.0.0.1",
  "state": 0,
  "admin": 0,
  "gender": 0,
  "created": "1800-01-01",
  "loaded": 1513352872109,
  "history": "1800-01-01",
  "birthday": "1800-01-01"
}
```
| Parameter | Type | Content
| ------ | ------ | ------ |
| id | number | Account's ID. (-1 if no account was found)
| verified | boolean | True if account is verified.
| name | string | Account's name.
| token | string | Account's current token.
| email | string | Account's email address.
| ip | string | Account's last known IP address.
| state | number | Account's current state.
| admin | number | Account's administration level.
| gender | number | Account's gender.
| created | string | Date the account was created.
| loaded | number | The time the account was loaded at in ms.
| history | string | The date the account last logged in on.
| birthday | string | The Account's birthday.

# /verfiy

Verifies an account based on email and verification code.

**URL EXAMPLE**
```
http://localhost:8080/verify?email=example@auth.api&code=1234
```
**PARAMETERS**
| Parameter | Value | Optional |
| ------ | ------ | ------ |
| email | Account e-mail address to be verified. | no
| code | 4 Digit code that was sent to the Account's e-mail address. | no

**RESPONSE FORMAT**
```JSON
{
  "message": ""
}
```
| Parameter | Type | Content
| ------ | ------ | ------ |
| message | string | Verification result message.

Possible message results:
1. "No verification process was found for the supplied email."
2. "The verification was successful!"
3. "The verification failed due to an unknown reason."
4. "The supplied code was incorrect."

# TODO
1. Update accounts latest known IP address.
2. Request reverification once account's IP has changed (2 factor auth)
3. Update account state upon login.
4. Invalidate tokens/ codes after 15 minutes (AKA create timed function executions)

