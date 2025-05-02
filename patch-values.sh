#!/bin/bash

BASE_DIR="argo/apps"

for file in "$BASE_DIR"/*/values.yaml; do
    [ -e "$file" ] || continue
    echo "Patching $file"

    yq eval '
      .service.type = "ClusterIP" |
      .service.port = 80
    ' -i "$file"
done
