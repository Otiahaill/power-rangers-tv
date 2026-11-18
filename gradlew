#!/bin/bash
# Gradle wrapper script for both local and CI environments

# Try to find gradle
if [ -n "$GRADLE_HOME" ] && [ -x "$GRADLE_HOME/bin/gradle" ]; then
    exec "$GRADLE_HOME/bin/gradle" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
elif [ -x "/data/user/0/com.androidharness.app/files/linux/bin/gradle" ]; then
    # Android Harness environment
    exec /data/user/0/com.androidharness.app/files/linux/bin/gradle "$@"
else
    echo "Error: Gradle not found. Please install Gradle or set GRADLE_HOME."
    exit 1
fi
