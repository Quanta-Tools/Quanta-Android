#!/usr/bin/env bash
# expand-settings.sh: read settings.xml, replace ${env.VAR} with real $VAR

INPUT="${1:-~/.m2/settings.xml}"
# 1) Rewrite ${env.FOO} → ${FOO}
#    sed 's/\$\{env\.VAR\}/\${VAR}/g'
# 2) Pipe into envsubst to do the actual substitution
sed -E 's/\$\{env\.([A-Za-z_][A-Za-z0-9_]*)\}/\${\1}/g' "$INPUT" | envsubst
