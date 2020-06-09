# hatchtrack-sensor-android

Android Studio project for the Hatchtrack Sensor app. All development, testing, debugging, building is done through [Android Studio](https://developer.android.com/studio).


# SSL Cert 

Step 1).
- 1a. Download certs folder and "cd" to directory holding "db-hatchtrack-com.pem" and run "sudo chmod 400 db-hatchtrack-com.pem" then
- 1b. ssh -i db-hatchtrack-com.pem ubuntu@ec2-52-12-165-107.us-west-2.compute.amazonaws.com
- 1c. sudo certbot renew
Step 2).
- 2a. Symbolic links to certs are located at: cd /etc/letsencrypt/live/db.hatchtrack.com/
- 2b. Use "ls -l" to show linked files and permissions.
Step 3).
- 3a. If problem persists, use "certbot revoke --cert-path /etc/letsencrypt/archive/db.hatchtrack.com/cert1.pem"
- 3b. To generate new cert: "sudo letsencrypt certonly --standalone -w /etc/letsencrypt -d db.hatchtrack.com -d www.db.hatchtrack.com"

