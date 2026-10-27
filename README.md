# 🚀 REST API Automation Framework | Java + Rest Assured + TestNG

An API automation testing project built using **Java 17, Rest Assured, and TestNG** to validate RESTful APIs using the [Restful Booker API](https://restful-booker.herokuapp.com).

## 🛠️ Tech Stack
- **Language:** Java 17
- **API Automation:** Rest Assured
- **Test Framework:** TestNG
- **Build Tool:** Maven
- **Validation:** Hamcrest matchers & JSON Schema validation
- **IDE:** Eclipse

## ✅ Test Scenarios Covered
- Retrieve all booking IDs — `GET`
- Create a new booking — `POST`
- Retrieve booking details and validate JSON schema — `GET`
- Update an existing booking — `PUT`
- Partially update booking details — `PATCH`
- Validate invalid booking ID behavior
- Delete a booking and verify deletion — `DELETE`

## 📂 Project Structure
```text
RestAssuredAPIAutomation/
├── src/
│   └── test/
│       ├── java/
│       │   └── com/qa/api/tests/
│       │       └── BookingAPITests.java
│       └── resources/
│           └── booking-schema.json
├── testng.xml
├── pom.xml
└── README.md
```

## ▶️ How to Run
1. Install **JDK 17** and **Maven**.
2. Clone this repository:
   ```bash
   git clone YOUR_GITHUB_REPOSITORY_URL
   ```
3. Open the project in Eclipse or another Java IDE.
4. Run the tests using Maven:
   ```bash
   mvn clean test
   ```
   Or run `testng.xml` as a **TestNG Suite** in Eclipse.

## 🎯 Project Goal
To practice API automation, HTTP methods, authentication, response validation, JSON Schema validation, and negative testing using a reusable Java-based test setup.

**Note:** This project uses a public demo API. Test results may vary depending on the service's availability and behavior.

