# Runtime-only image: the JAR is built once by .github/workflows/release.yml
# and copied in. Not multi-stage on purpose - pom.xml resolves dependency
# version ranges at build time, so a second `mvn package` inside the image
# could resolve different dependencies than the build whose output is
# published as the release asset, leaving the two silently out of sync.
FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.title="openaire-cris-validator" \
      org.opencontainers.image.description="Validator for OpenAIRE Guidelines for CRIS Managers" \
      org.opencontainers.image.source="https://github.com/EuroCRIS/openaire-cris-validator" \
      org.opencontainers.image.licenses="Apache-2.0"

# The image redistributes the binary, so the licence travels with it. The
# JAR's own META-INF/NOTICE is the merged NOTICE of its dependencies and does
# not cover the OpenAIRE schemas that the assembly embeds.
COPY LICENSE NOTICE-schemas /opt/
COPY dist/validator.jar /opt/validator.jar

# The validator writes fetched OAI-PMH responses to a RELATIVE "data"
# directory, so give it a writable working directory. Mount a volume on
# /work/data to keep them.
WORKDIR /work

# Note: there is no usage message - invoked without an endpoint URL the tool
# fails with a NullPointerException from URI.create(null).
ENTRYPOINT ["java", "-jar", "/opt/validator.jar"]
