#!/bin/sh
# append (#5) to first line if not present
file="$1"
tmp="$file.tmp"
awk 'NR==1{ if ($0 !~ /\(#5\)/) { $0=$0 "(#5)" } print; next } { print }' "$file" > "$tmp" && mv "$tmp" "$file"
