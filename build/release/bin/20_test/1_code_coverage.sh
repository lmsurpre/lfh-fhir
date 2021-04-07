#!/usr/bin/env bash

set -eu -o pipefail

###############################################################################
# (C) Copyright IBM Corp. 2020, 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

# verify and generate code coverage jacoco.exec and xml files

# fhir-examples
export BUILD_PROFILES=" $(jq -r '.build[] | select(.type == "fhir-examples").profiles | map(.) | join(",")' build/release/config/release.json)"
mvn -T2C test jacoco:report-aggregate -f fhir-examples -P "${BUILD_PROFILES}"

# fhir-tools
export BUILD_PROFILES=" $(jq -r '.build[] | select(.type == "fhir-tools").profiles | map(.) | join(",")' build/release/config/release.json)"
mvn -T2C test jacoco:report-aggregate -f fhir-tools -P "${BUILD_PROFILES}"

# fhir-parent
export BUILD_PROFILES=" $(jq -r '.build[] | select(.type == "fhir-parent").profiles | map(.) | join(",")' build/release/config/release.json)"
mvn -T2C test jacoco:report-aggregate -f fhir-parent -P "${BUILD_PROFILES}"

mkdir -p build/release/workarea/release_files/test_coverage
mkdir -p build/release/workarea/release_files/logs
release-test-coverage-and-logs.tgz

# EOF