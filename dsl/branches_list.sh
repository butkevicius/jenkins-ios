#!/bin/bash
git for-each-ref --format='%(refname:short)' refs/remotes/origin/ \
| grep -v -e "origin/jenkins-seed$" -e "origin/HEAD$" -e "origin/master$" \
| sort > branches.txt
