#!/usr/bin/env bash
# check_deploy_status.sh - Check the status of deployments in the Central Publisher Portal

# Find open repositories
echo "Searching for repositories..."
REPOSITORY_RESPONSE=$(curl -s -X GET \
    -H "Authorization: Bearer $(printf "$MAVEN_USERNAME:$MAVEN_PASSWORD" | base64)" \
    "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?state=open")

echo "Repository search response: $REPOSITORY_RESPONSE"

# Extract repository key if available
REPO_KEY=$(echo "$REPOSITORY_RESPONSE" | grep -o '"key":"[^"]*"' | head -1 | cut -d':' -f2 | tr -d '"')

if [ -n "$REPO_KEY" ]; then
    echo "Found repository key: $REPO_KEY"

    # Trigger automatic publishing for this repository
    echo "Requesting automatic publishing..."
    curl -s -X POST \
        -H "Authorization: Bearer $(printf "$MAVEN_USERNAME:$MAVEN_PASSWORD" | base64)" \
        "https://ossrh-staging-api.central.sonatype.com/manual/upload/repository/$REPO_KEY?publishing_type=automatic"

    echo "Publishing request sent. Check https://central.sonatype.com/publishing/deployments for status."
else
    echo "No open repositories found. Check the Central Publisher Portal manually."
    echo "Visit: https://central.sonatype.com/publishing/deployments"
fi
