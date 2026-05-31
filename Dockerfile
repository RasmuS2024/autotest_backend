FROM gradle:8.7-jdk17-alpine

WORKDIR /app
COPY . .

RUN chmod +x gradlew

CMD sh -c "./gradlew clean test allureReport --no-daemon && \
          echo 'Allure report is ready on http://localhost:80' && \
          python3 -m http.server 80 --directory build/allure-report"
