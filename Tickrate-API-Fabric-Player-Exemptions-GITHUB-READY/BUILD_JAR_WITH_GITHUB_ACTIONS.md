# Build the Fabric jar with GitHub Actions

This project includes a workflow at `.github/workflows/build.yml` that builds the mod using Java 17 and Gradle.

## Steps

1. Create a new GitHub repository.
2. Upload all files from this project into the repository, including the hidden `.github` folder.
3. Open the repository on GitHub.
4. Go to **Actions**.
5. Select **Build Fabric Mod**.
6. Click **Run workflow**.
7. After it finishes, open the completed workflow run.
8. Download the artifact named `tickrateapi-fabric-jars`.
9. Inside that ZIP, use the normal `.jar` from `build/libs`; do not use `-sources`, `-dev`, or `-javadoc` jars.

If the workflow fails, copy the build log and send it back for fixes.
