# EV Charger Map App

## Backend

Author: Jakub Zieliński

EV Charger map is developed as a Bachelor of Engineering Thesis for Appplied Computer Science studies at Cracow
University of Technology. Following repository is a backend side of the app, currently focusing on REST API to fetch
chargers and manage systems users. it should contain the entire app as the project grows and the right documentation.

### Technology
- Java JDK 21 LTS
- Java Spring Boot

### REST API

### Getting Started
To run the app you need to configure the environment first, starting with .env file.

```.dotenv
DB_NAME=evcm
DB_USER=your_postgres_user
DB_PASSWORD=your_postgres_user_password
DB_PORT=5432

OPEN_CHARGE_API_KEY=open_charge_api_generated_key

JWT_SECRET_KEY=jwt_secret_key
JWT_EXP_TIME=3600000
```
