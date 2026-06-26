#!/bin/bash

SONAR_HOST="https://sonarcloud.io"
SONAR_PROJECT_KEY="${SONAR_PROJECT_KEY}"
SONAR_TOKEN="${SONAR_TOKEN}"

COVERAGE=$(curl -s -u "$SONAR_TOKEN:" \
  "$SONAR_HOST/api/measures/component?component=$SONAR_PROJECT_KEY&metricKeys=coverage" \
  | grep -o '"value":"[^"]*"' | cut -d'"' -f4)

if [ -z "$COVERAGE" ]; then
  COVERAGE="0"
fi

cat <<EOF
# HELP sonarcloud_test_coverage Test coverage percentage from SonarCloud
# TYPE sonarcloud_test_coverage gauge
sonarcloud_test_coverage $COVERAGE
EOF
