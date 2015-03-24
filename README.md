# drivemarks

Drivemarks is a bookmarklet that helps you to store your bookmarks on  Google Drive. It enables you to manage, search, share your bookmarks the way you manage your regular files and use Google Drive to launch your bookmarks.

![Screenshot](https://googledrive.com/host/0ByfSjdPVs9MZbkhjeUhMYzRTeEE/drivemarks.png)

Drivemarks runs on Google App Engine. In order to fork a drivemarks app engine instance, follow the instructions below:

* Fork the repo: `$ clone git@github.com:googledrive/drivemarks.git`
* Edit `war\WEB-INF\client_credentials.json` with your credentials created on the [API Console](https://code.google.com/apis/console).
* Enable Drive API and SDK under the Services tab on the API Console.
* Edit details for your app on Drive SDK section on the API Console.
* Run, test and deploy!
